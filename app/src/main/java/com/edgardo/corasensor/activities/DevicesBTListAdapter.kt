package com.edgardo.corasensor.activities

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.edgardo.corasensor.R
import kotlinx.android.synthetic.main.row_devices_bt.view.*
import java.util.ArrayList

class DevicesBTListAdapter(
        context: Context,
        val resourceId: Int,
        val devices: ArrayList<BluetoothDevice>
) : ArrayAdapter<BluetoothDevice>(context, resourceId, devices) {

    var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        convertView = layoutInflater.inflate(resourceId, null)

        val device = devices[position]

        if (device != null) {
            convertView.device_name.text = device.name
            convertView.device_addr.text = device.address

        }

        return convertView
    }

}