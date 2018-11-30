// HearAssistent
//
//Copyright (C) 2018 - ITESM
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.


/**
 * This code is based on a existing project of "mitchtabian" and modified to this project necessities
 * Source code : https://github.com/mitchtabian/Sending-and-Receiving-Data-with-Bluetooth
 */

package com.edgardo.corasensor.networkUtility

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.edgardo.corasensor.R
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit


class BluetoothConnectionService(internal var context: Context) {

    private val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var acceptThread: AcceptThread? = null

    private var connectThread: ConnectThread? = null
    private var device: BluetoothDevice? = null
    lateinit var progressDialogConnection: ProgressDialog

    private var connectedThread: ConnectedThread? = null

    private var emmitter: ObservableEmitter<String>? = null
    val _tag = "BTConnServer"

    companion object {

        const val connectionName = "scanApp"

        private val uuidConnection = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    init {
        start()
    }


    /**
     * Function to initialize and accept the connection, creates the connection thread
     */
    private inner class AcceptThread : Thread() {


        private var serverSocket: BluetoothServerSocket? = null

        init {
            try {
                serverSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(connectionName, uuidConnection)
//                serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(connectionName, MY_UUID_INSECURE)
                Log.d(_tag, "AcceptThread: Setting up Server using: $uuidConnection")
            } catch (e: IOException) {
                Log.e(_tag, "AcceptThread: IOException: " + e.message)
            }

        }

        override fun run() {
            Log.d(_tag, "run: AcceptThread Running.")

            var socket: BluetoothSocket? = null

            try {
                socket = serverSocket!!.accept()

            } catch (e: IOException) {
                Log.e(_tag, "AcceptThread: IOException: " + e.message)
            }

            if (socket != null) {
                connected(socket, device)
            }

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
     * Start connection with the fa87c0d0-afac-11de-8a39-0800200c9a66s
     */
    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
        private var btSocket: BluetoothSocket? = null

        init {
            Log.d(_tag, "ConnectThread: started.")
            this@BluetoothConnectionService.device = device


            try {
//                btSocket = device.createRfcommSocketToServiceRecord(uuid)
                btSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)

            } catch (e: IOException) {
                Log.e(_tag, "ConnectThread: Could not create socket connection " + e.message)
            }
        }

        override fun run() {


            // Cancel discovery mode
            btAdapter.cancelDiscovery()

            // Check if socket is initialized
            if (btSocket == null) {
                Log.e(_tag, "socket: is null")
            }
            try {

                btSocket!!.connect()

            } catch (e: IOException) {
                // Close the socket
                try {
                    // Close connection
                    btSocket!!.close()
                    Log.d(_tag, "run: Closed Socket")
                } catch (e1: IOException) {
                    Log.e(_tag, "run: Error closing socket " + e1.message)
                }
                // Show error
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Error connecting", Toast.LENGTH_SHORT).show()
                    (context as Activity).finish()
                }

            }

            val btSocketAux = btSocket

            if (btSocketAux != null) {
                connected(btSocketAux, device)
            }
        }

        fun cancel() {
            try {
                Log.d(_tag, "cancel: Closing Client Socket.")
                btSocket!!.close()
            } catch (e: IOException) {
                Log.e(_tag, "cancel: Error closing socket " + e.message)
            }

        }
    }


    /**
     * Start services
     */
    @Synchronized
    fun start() {
        Log.d(_tag, "start")

        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
    }
    @Synchronized
    fun disconnect(){
        connectThread!!.cancel()
    }


    fun startClient(device: BluetoothDevice, uuid: UUID): Observable<String> {
        Log.d(_tag, "startClient: Started.")

        // Progress for connection
        progressDialogConnection = ProgressDialog.show(context, "Connecting Bluetooth", "Please Wait...", true)

        connectThread = ConnectThread(device, uuid)
        connectThread!!.start()

        return Observable.create {
            emmitter = it
        }
    }

    /**
     *
     * Maintaining the BT Connection, Send and receive data
     */
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inStream: InputStream?
//        private val outStream: OutputStream?

        init {
            var tmpIn: InputStream? = null

            // Hide progress bar on connection
            try {
                progressDialogConnection.dismiss()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }


            try {
                tmpIn = socket.inputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }

            inStream = tmpIn
        }

        // Listen to data incoming
        override fun run() {
            // Buffer for data
            val buffer = ByteArray(1024)
            var bytes: Int


            // Listen till no more data
            while (true) {
                // Read from the InputStream
                try {
                    //bytes = inStream!!.read(buffer)
                    // parse data
                    val incomingMessage = inStream!!.bufferedReader().readLine()
                    //val incomingMessage = String(buffer, 0, bytes)
                    Log.d(_tag, "LOG " + incomingMessage)

                    emmitter?.onNext(incomingMessage)



                } catch (e: IOException) {
                    Log.e(_tag, "write: Error reading Input Stream. " + e.message)
                    // Show error
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "Connection end..", Toast.LENGTH_SHORT).show()

                        (context as Activity).finish()
                    }

                    break
                }

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

    private fun connected(socket: BluetoothSocket, device: BluetoothDevice?) {
        Log.d(_tag, "connected: Starting.")

        // Start thread  manage
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()
    }

}
























