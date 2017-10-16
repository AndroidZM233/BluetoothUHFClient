package com.speedata.bluetoothuhfclient;

import android.bluetooth.BluetoothDevice;

/**
 * Created by 张明_ on 2017/8/17.
 * Email 741183142@qq.com
 */

public class RVBean {
    private String name;
    private String address;
    private BluetoothDevice bluetoothDevice;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
