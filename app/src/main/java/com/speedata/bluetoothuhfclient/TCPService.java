package com.speedata.bluetoothuhfclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

/**
 * Created by 张明_ on 2017/10/10.
 * Email 741183142@qq.com
 */

public class TCPService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedXmlUtil = SharedXmlUtil.getInstance(this);
        //启动服务器监听线程
        if (mServerSocketThread == null) {
            mServerSocketThread = new ServerSocketThread();
            mServerSocketThread.start();
        }
        EventBus.getDefault().register(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventMsg(MsgEvent msgEvent) {
        String type = msgEvent.getType();
        Object msg = msgEvent.getMsg();
        if ("ReceiveData".equals(type)) {
            sendMsg(String.valueOf(msg));
        }
    }

    private ServerSocket serverSocket = null;//创建ServerSocket对象
    private Socket clicksSocket = null;//连接通道，创建Socket对象
    private InputStream inputstream = null;//创建输入数据流
    private OutputStream outputStream = null;//创建输出数据流
    private ServerSocketThread mServerSocketThread = null;
    private SharedXmlUtil sharedXmlUtil = null;
    private String result = "";
    private ReceiveThread mReceiveThread;
    byte[] buf = new byte[1024];


    /**
     * 服务器监听线程
     */
    private class ServerSocketThread extends Thread {

        @Override
        public void run()//重写Thread的run方法
        {
            try {
                if (serverSocket == null) {
                    serverSocket = new ServerSocket(6117);//监听port端口，这个程序的通信端口就是port了
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!interrupted()) {
                try {
//                    SystemClock.sleep(10);
                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                    clicksSocket = serverSocket.accept();
                    inputstream = clicksSocket.getInputStream();
                    InetAddress inetAddress = clicksSocket.getInetAddress();
                    //启动接收线程
                    String hostAddress = inetAddress.getHostAddress() + "已连接\n";
                    EventBus.getDefault().post(new MsgEvent("TCPConnect", hostAddress));
                    LogToFileUtils.write("TCP连接成功："+hostAddress);
//                    int openDevStatus = sharedXmlUtil.read("openDevStatus", -1);
//                    if (openDevStatus != 0) {
//                        result = "N:ERR,0,RFID_COMM_OPEN_ERROR" + "\r\n";
//                        sendMsg(result);
//                    }
                    if (mReceiveThread == null) {
                        mReceiveThread = new ReceiveThread();
                        mReceiveThread.start();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 接收线程
     */
    private class ReceiveThread extends Thread//继承Thread
    {
        @Override
        public void run()//重写run方法
        {
            while (!isInterrupted()) {
                try {
                    final int len = inputstream.read(buf);
                    if (len < 0) {
                        mReceiveThread.interrupt();
                        mReceiveThread = null;

                        InetAddress inetAddress = clicksSocket.getInetAddress();
                        String hostAddress = inetAddress.getHostAddress() + "已断开\n";
                        EventBus.getDefault().post(new MsgEvent("TCPConnect", hostAddress));
                        LogToFileUtils.write("TCP连接断开："+hostAddress);
                        continue;
                    }
                    String receiveStr = new String(buf, 0, len);
//                    LogToFileUtils.write(receiveStr);
                    receiveStr = receiveStr.replace("\r", "").replace("\\\n", "").replace("\n", "")
                            .replace("<CR><LF>", "");
//                    EventBus.getDefault().post(new MsgEvent("tcp_receiver", receiveStr));
                    EventBus.getDefault().post(new MsgEvent("TCP_MSG", receiveStr));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    //发送信息
    public void sendMsg(String msg) {
        try {
//            LogToFileUtils.write(msg);
            EventBus.getDefault().post(new MsgEvent("tcp_send", msg));
            //获取输出流
            outputStream = clicksSocket.getOutputStream();
            //发送数据
            outputStream.write(msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            mServerSocketThread.interrupt();
            mServerSocketThread = null;
            if (mReceiveThread != null) {
                mReceiveThread.interrupt();
                mReceiveThread = null;
            }
            if (inputstream != null) {
                inputstream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (clicksSocket != null) {
                clicksSocket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
