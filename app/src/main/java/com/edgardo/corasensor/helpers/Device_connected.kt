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