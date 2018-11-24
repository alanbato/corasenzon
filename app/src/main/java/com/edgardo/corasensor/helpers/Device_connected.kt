package com.edgardo.corasensor.helpers

public class Device_connected() {

    val instance: Device_connected? = null


    private var device_name: String? = null
    private var device_addr: String? = null
    private var device_connected: String? = null

    public fun setData(name: String, device_addr: String, device_connected: String) {
        this.device_name = name
        this.device_addr = device_addr
        this.device_connected = device_connected
    }
}