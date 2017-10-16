package com.speedata.bluetoothuhfclient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * Created by 张明_ on 2017/8/17.
 * Email 741183142@qq.com
 */

public class MyApplication extends Application {
    private static MyApplication m_application; // 单例
    private volatile BluetoothSocket clientSocket = null;
    private volatile int connectCount = 0;

    public BluetoothSocket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(BluetoothSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m_application = this;
        LogToFileUtils.isDebug = true;
        LogToFileUtils.init(this);
    }

    public void checkConnected() {
        ReConnectThread reConnectThread = new ReConnectThread();
        reConnectThread.start();
    }

    class ReConnectThread extends Thread {
        @Override
        public void run() {
            super.run();
            String address = SharedXmlUtil.getInstance(getApplicationContext()).read("address", "");
            if (!TextUtils.isEmpty(address)) {
                BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                BluetoothSocket clientSocket = null;
                try {
                    clientSocket = bluetoothDevice
                            .createRfcommSocketToServiceRecord(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
                    clientSocket.connect();
                    setClientSocket(clientSocket);
                    EventBus.getDefault().post(new MsgEvent("connectSuccess",
                            bluetoothDevice.getName() + "蓝牙自动连接成功\n"));
                    EventBus.getDefault().post(new MsgEvent("ReceiveData", "N:CON,0,1\r\n"));
                    LogToFileUtils.write("N:CON,0,1\r\n");
//                    connectCount = 0;
                } catch (IOException e) {
                    e.printStackTrace();
//                    connectCount++;
//                    if (connectCount < 4) {
////                        EventBus.getDefault().post(new MsgEvent("connectSuccess",
////                                connectCount + "蓝牙自动连接失败,再次连接中...\n"));
//                        checkConnected();
//                    } else {
//                        EventBus.getDefault().post(new MsgEvent("connectFailed",
//                                "蓝牙自动连接失败\n"));
//                        connectCount = 0;
//                    }
                    BluetoothSocket myAppClientSocket = getClientSocket();
                    if (myAppClientSocket != null) {
                        try {
                            myAppClientSocket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        clientSocket = null;
    }

    public static MyApplication getInstance() {
        return m_application;
    }


}
