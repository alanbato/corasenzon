/**
 * This code is based on a existing project of "mitchtabian" and modified to this project necessities
 * Source code : https://github.com/mitchtabian/Sending-and-Receiving-Data-with-Bluetooth
 */

package com.edgardo.corasensor.networkUtility

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.UUID


class BluetoothConnectionService(internal var context: Context) {

    private val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var acceptThread: AcceptThread? = null

    private var mConnectThread: ConnectThread? = null
    private var device: BluetoothDevice? = null
    private var deviceUUID: UUID? = null
    lateinit var progressDialogConnection: ProgressDialog

    private var connectedThread: ConnectedThread? = null
    val _tag = "BTConnServer"

    companion object {

        const val connectionName = "scanApp"

        private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    }

    init {
        start()
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread : Thread() {

        // The local server socket
        private val serverSocket: BluetoothServerSocket?

        init {
            var btServerSocket: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                btServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(connectionName, MY_UUID_INSECURE)
                Log.d(_tag, "AcceptThread: Setting up Server using: $MY_UUID_INSECURE")
            } catch (e: IOException) {
                Log.e(_tag, "AcceptThread: IOException: " + e.message)
            }

            serverSocket = btServerSocket
        }

        override fun run() {
            Log.d(_tag, "run: AcceptThread Running.")

            var socket: BluetoothSocket? = null

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(_tag, "run: RFCOM server socket start.....")

                socket = serverSocket!!.accept()

                Log.d(_tag, "run: RFCOM server socket accepted connection.")

            } catch (e: IOException) {
                Log.e(_tag, "AcceptThread: IOException: " + e.message)
            }

            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket, device)
            }

            Log.i(_tag, "END mAcceptThread ")
        }

        fun cancel() {
            Log.d(_tag, "cancel: Canceling AcceptThread.")
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                Log.e(_tag, "cancel: Close of AcceptThread ServerSocket failed. " + e.message)
            }

        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
        private var mmSocket: BluetoothSocket? = null

        init {
            Log.d(_tag, "ConnectThread: started.")
            this@BluetoothConnectionService.device = device
            deviceUUID = uuid
        }

        override fun run() {
            var btSocket: BluetoothSocket? = null
            Log.i(_tag, "RUN mConnectThread ")

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(_tag, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: $MY_UUID_INSECURE")
                btSocket = device!!.createRfcommSocketToServiceRecord(deviceUUID)
            } catch (e: IOException) {
                Log.e(_tag, "ConnectThread: Could not create InsecureRfcommSocket " + e.message)
            }

            mmSocket = btSocket

            // Always cancel discovery because it will slow down a connection
            btAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()

                Log.d(_tag, "run: ConnectThread connected.")
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                    Log.d(_tag, "run: Closed Socket.")
                } catch (e1: IOException) {
                    Log.e(_tag, "mConnectThread: run: Unable to close connection in socket " + e1.message)
                }

                Log.d(_tag, "run: ConnectThread: Could not connect to UUID: $MY_UUID_INSECURE")
            }

            val mmSocket = mmSocket

            //will talk about this in the 3rd video
            if (mmSocket != null) {
                connected(mmSocket, device)
            }
        }

        fun cancel() {
            try {
                Log.d(_tag, "cancel: Closing Client Socket.")
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(_tag, "cancel: close() of mmSocket in Connectthread failed. " + e.message)
            }

        }
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(_tag, "start")

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
    }

    /**
     *
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     */

    fun startClient(device: BluetoothDevice, uuid: UUID) {
        Log.d(_tag, "startClient: Started.")

        // Progress for connection
        progressDialogConnection = ProgressDialog.show(context, "Connecting Bluetooth", "Please Wait...", true)

        mConnectThread = ConnectThread(device, uuid)
        mConnectThread!!.start()
    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     */
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inStream: InputStream?
        private val outStream: OutputStream?

        init {
            Log.d(_tag, "ConnectedThread: Starting.")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            //dismiss the progressdialog when connection is established
            try {
                progressDialogConnection.dismiss()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }


            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }

            inStream = tmpIn
            outStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(1024)  // buffer store for the stream

            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = inStream!!.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    Log.d(_tag, "InputStream: $incomingMessage")
                } catch (e: IOException) {
                    Log.e(_tag, "write: Error reading Input Stream. " + e.message)
                    break
                }

            }
        }

        // Send data to other devices
        fun write(bytes: ByteArray) {
            val text = String(bytes, Charset.defaultCharset())
            Log.d(_tag, "write: Writing to outputstream: $text")
            try {
                outStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e(_tag, "write: Error writing to output stream. " + e.message)
            }

        }

        // Shutdown the connection
        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
            }

        }
    }

    private fun connected(mmSocket: BluetoothSocket, mmDevice: BluetoothDevice?) {
        Log.d(_tag, "connected: Starting.")

        // Start the thread to manage the connection and perform transmissions
        connectedThread = ConnectedThread(mmSocket)
        connectedThread!!.start()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Create temporary object
        val r: ConnectedThread

        // Synchronize a copy of the ConnectedThread
        Log.d(_tag, "write: Write Called.")
        //perform the write
        connectedThread!!.write(out)
    }
    fun read(){

    }



}
























