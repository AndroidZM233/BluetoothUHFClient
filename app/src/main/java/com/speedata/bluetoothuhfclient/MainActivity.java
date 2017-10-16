package com.speedata.bluetoothuhfclient;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseAct implements View.OnClickListener {

    private BluetoothSocket transferSocket;
    private TextView mTvShowInfo;
    private EditText mEtP;
    private Button mBtnP;
    private EditText mEtCount;
    private EditText mEtRssi;
    private EditText mEtRssiCount;
    private Button mBtnC;
    private Button mBtnS;
    private Button mBtnI;
    private Button mBtnG;
    private Button mBtnR;
    private Button mBtnDisconnect;
    private InputStream inputStream;
    private listenForMessagesThread listenForMessagesThread;
    private OutputStream outStream;
    private String clickBtn;
    private Timer timer;
    private EditText et_period;
    private EditText et_duration;
    private Button mBtnE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BanBottomBarUtils.recent(false, this);
        BanBottomBarUtils.back(false, this);
        BanBottomBarUtils.home(false, this);
        BanBottomBarUtils.upmenu(false, this);
        initView();
        EventBus.getDefault().register(this);
        MyApplication.getInstance().checkConnected();

        timer = new Timer();
        MyConnectTimerTask myConnectTimerTask = new MyConnectTimerTask();
        timer.schedule(myConnectTimerTask, 1000, 1 * 1000);

        Intent tcpIntent = new Intent(this, TCPService.class);
        tcpIntent.setPackage("com.speedata.bluetoothuhfclient");
        startService(tcpIntent);
    }


    private class MyConnectTimerTask extends TimerTask {

        @Override
        public void run() {
            String address = SharedXmlUtil.getInstance(getApplicationContext()).read("address", "");
            if (!TextUtils.isEmpty(address)) {
                sendMessage(transferSocket, "testConnect");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventMsg(MsgEvent msgEvent) {
        String type = msgEvent.getType();
        Object msg = msgEvent.getMsg();
        if ("connectSuccess".equals(type)) {
            transferSocket = MyApplication.getInstance().getClientSocket();
            if (transferSocket != null) {
                connectToServerSocket(transferSocket);
//                EventBus.getDefault().post(new MsgEvent("ReceiveData", "N:CON,0,1\r\n"));
//                LogToFileUtils.write("N:CON,0,1\r\n");
                mTvShowInfo.setText(msg + "");
                mBtnC.setEnabled(true);
                mBtnDisconnect.setEnabled(true);
                mBtnG.setEnabled(true);
                mBtnI.setEnabled(true);
                mBtnP.setEnabled(true);
                mBtnR.setEnabled(true);
                mBtnS.setEnabled(true);
            }

        } else if ("ReceiveData".equals(type)) {
            String msgStr = msg.toString();
            LogToFileUtils.write(msgStr + "\r\n");
            String substring = msgStr.substring(0, 2);
            String dataSub = msgStr.substring(2, msgStr.length() - 1);
            String data = dataSub.replace("\r", "").replace("\n", "");
            if ("N:".equals(substring)) {
                showNInfo(data);
                return;
            }
            switch (clickBtn) {
                case "P":
                    if ("1:".equals(substring)) {
                        Toast.makeText(this, "功率设置成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "功率设置失败" + data, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "C":
                    if ("1:".equals(substring)) {
                        Toast.makeText(this, "参数设置成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "参数设置失败" + data, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "S":
                    if ("1:".equals(substring)) {
                        Toast.makeText(this, "参数保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "参数保存失败" + data, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "E":
                    if ("1:".equals(substring)) {
                        Toast.makeText(this, "停止成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "停止失败" + data, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "I":
                    if ("1:".equals(substring)) {
                        String[] split = data.split(",");
                        String[] split0 = split[0].split("=");
                        String[] split1 = split[1].split("=");
                        String[] split2 = split[2].split("=");
                        String[] split3 = split[3].split("=");
                        String[] split4 = split[4].split("=");
                        String[] split5 = split[5].split("=");
                        String[] split6 = split[6].split("=");
                        String[] split7 = split[7].split("=");
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("读写器参数：\n");
                        stringBuilder.append("最大读取标签数量：").append(split0[1]).append("\n");
                        stringBuilder.append("过滤低于设置信号强度值：").append(split1[1]).append("\n");
                        stringBuilder.append("过滤低于单次读取标签数量：").append(split2[1]).append("\n");
                        stringBuilder.append("TAG周期性上报间隔：").append(split3[1]).append("ms\n");
                        stringBuilder.append("读标签持续时间：").append(split4[1]).append("ms\n");
                        stringBuilder.append("读写器是否已成功连接：").append(split5[1]).append("\n");
                        stringBuilder.append("天线的ID：").append(split6[1]).append("\n");
                        stringBuilder.append("天线的功率：").append(split7[1]).append("\n");
                        mTvShowInfo.setText(stringBuilder + "");
                    } else {
                        Toast.makeText(this, "查询读写器参数失败" + data, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "G":
                    if ("1:".equals(substring)) {
                        String[] split = data.split(",");
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("传感器状态：\n");
                        stringBuilder.append("端口数：").append(split[1]).append("\n");
                        if ("1".equals(split[2])) {
                            stringBuilder.append("端口1：高电平\n");
                        } else {
                            stringBuilder.append("端口1：低电平\n");
                        }
                        if ("1".equals(split[3])) {
                            stringBuilder.append("端口2：高电平\n");
                        } else {
                            stringBuilder.append("端口2：低电平\n");
                        }
                        mTvShowInfo.setText(stringBuilder + "");
                    } else {
                        Toast.makeText(this, "查询传感器状态失败" + data, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } else if ("showMsg".equals(type)) {
            Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show();
            mTvShowInfo.setText("连接断开，重新连接中...\n");
        } else if ("TCP_MSG".equals(type)) {
            sendMessage(transferSocket, String.valueOf(msg));
        } else if ("TCPConnect".equals(type)) {
            Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show();
        } else if ("connectFailed".equals(type)) {
            mTvShowInfo.setText("请先连接蓝牙设备\n");
            mBtnC.setEnabled(false);
            mBtnDisconnect.setEnabled(false);
            mBtnG.setEnabled(false);
            mBtnI.setEnabled(false);
            mBtnP.setEnabled(false);
            mBtnR.setEnabled(false);
            mBtnS.setEnabled(false);
        }
    }

    //展示读取的信息
    private void showNInfo(String s) {
        String[] dataSplit = s.split(",");
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("读取信息：\n");
        if ("0".equals(dataSplit[3])) {
            stringBuffer.append("无标签信息");
        } else {
            int count = Integer.parseInt(dataSplit[3]);
            for (int i = 0; i < count; i++) {
                int i1 = i + 1;
                int i2 = i * 4;
                stringBuffer.append("标签" + i1 + ":").append(dataSplit[4 + i2])
                        .append("  RSSI:").append(dataSplit[5 + i2])
                        .append(" 天线ID：").append(dataSplit[6 + i2])
                        .append("  次数:").append(dataSplit[7 + i2]).append("\n");
            }
            stringBuffer.append(dataSplit[8 + (count - 1) * 4]);
        }


//        if ("1".equals(dataSplit[3])) {
//            stringBuffer.append("标签1:").append(dataSplit[4]).append("  RSSI:").append(dataSplit[5]).append(" 天线ID：").append(dataSplit[6])
//                    .append("  次数:").append(dataSplit[6]).append("\n");
//            stringBuffer.append(dataSplit[7]);
//        } else if ("2".equals(dataSplit[3])) {
//            stringBuffer.append("标签1:").append(dataSplit[4]).append("  RSSI:").append(dataSplit[5])
//                    .append("  次数:").append(dataSplit[6]).append("\n");
//            stringBuffer.append("标签2:").append(dataSplit[7]).append("  RSSI:").append(dataSplit[8])
//                    .append("  次数:").append(dataSplit[9]).append("\n");
//            stringBuffer.append(dataSplit[10]);
//        } else if ("3".equals(dataSplit[3])) {
//            stringBuffer.append("标签1:").append(dataSplit[4]).append("  RSSI:").append(dataSplit[5])
//                    .append("  次数:").append(dataSplit[6]).append("\n");
//            stringBuffer.append("标签2:").append(dataSplit[7]).append("  RSSI:").append(dataSplit[8])
//                    .append("   次数:：").append(dataSplit[9]).append("\n");
//            stringBuffer.append("标签3:").append(dataSplit[10]).append("  RSSI:").append(dataSplit[11])
//                    .append("  次数:").append(dataSplit[12]).append("\n");
//            stringBuffer.append(dataSplit[13]);
//        }
        mTvShowInfo.setText(stringBuffer + "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            transferSocket = MyApplication.getInstance().getClientSocket();
            if (transferSocket == null) {
                mTvShowInfo.setText("请先连接蓝牙设备\n");
                mBtnC.setEnabled(false);
                mBtnDisconnect.setEnabled(false);
                mBtnG.setEnabled(false);
                mBtnI.setEnabled(false);
                mBtnP.setEnabled(false);
                mBtnR.setEnabled(false);
                mBtnS.setEnabled(false);
            } else {
                mBtnC.setEnabled(true);
                mBtnDisconnect.setEnabled(true);
                mBtnG.setEnabled(true);
                mBtnI.setEnabled(true);
                mBtnP.setEnabled(true);
                mBtnR.setEnabled(true);
                mBtnS.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (transferSocket == null) {
                mTvShowInfo.setText("请先连接蓝牙设备\n");
            }
        }

    }

    private void initView() {
        mTvShowInfo = (EditText) findViewById(R.id.tv_show_info);
        mTvShowInfo.setOnClickListener(this);
        mTvShowInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        mEtP = (EditText) findViewById(R.id.et_p);
        mEtP.setOnClickListener(this);
        mBtnP = (Button) findViewById(R.id.btn_p);
        mBtnP.setOnClickListener(this);
        mEtCount = (EditText) findViewById(R.id.et_count);
        mEtCount.setOnClickListener(this);
        mEtRssi = (EditText) findViewById(R.id.et_rssi);
        mEtRssi.setOnClickListener(this);
        mEtRssiCount = (EditText) findViewById(R.id.et_rssi_count);
        mEtRssiCount.setOnClickListener(this);
        mBtnC = (Button) findViewById(R.id.btn_c);
        mBtnC.setOnClickListener(this);
        mBtnS = (Button) findViewById(R.id.btn_s);
        mBtnS.setOnClickListener(this);
        mBtnI = (Button) findViewById(R.id.btn_i);
        mBtnI.setOnClickListener(this);
        mBtnG = (Button) findViewById(R.id.btn_g);
        mBtnG.setOnClickListener(this);
        mBtnR = (Button) findViewById(R.id.btn_r);
        mBtnR.setOnClickListener(this);
        mBtnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        mBtnDisconnect.setOnClickListener(this);
        et_period = (EditText) findViewById(R.id.et_period);
        et_duration = (EditText) findViewById(R.id.et_duration);
        mBtnE = (Button) findViewById(R.id.btn_e);
        mBtnE.setOnClickListener(this);
    }

    private void connectToServerSocket(BluetoothSocket bluetoothSocket) {
        try {
            // Start listening for messages.
            inputStream = bluetoothSocket.getInputStream();
            if (listenForMessagesThread == null) {
                listenForMessagesThread = new listenForMessagesThread();
                listenForMessagesThread.start();
            }

        } catch (IOException e) {
            Log.e("BLUETOOTH", "Blueooth client I/O Exception", e);
        }
    }

    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    private class listenForMessagesThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                try {
                    int bytesRead = -1;
                    bytesRead = inputStream.read(buffer);
                    if (bytesRead != -1) {
                        String receiveStr = new String(buffer, 0, bytesRead);
                        EventBus.getDefault().post(new MsgEvent("ReceiveData", receiveStr));
                    } else {
                        listenForMessagesThread.interrupt();
                        listenForMessagesThread = null;
                        MyApplication.getInstance().checkConnected();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendMessage(BluetoothSocket socket, String message) {
        try {
            outStream = socket.getOutputStream();
            outStream.write(message.getBytes());
        } catch (Exception e) {
//            mTvShowInfo.setText("连接断开，请重新连接");
            EventBus.getDefault().post(new MsgEvent("showMsg", "连接断开，重新连接中..."));
            MyApplication.getInstance().checkConnected();
            EventBus.getDefault().post(new MsgEvent("ReceiveData", "N:CON,0,0\r\n"));
            LogToFileUtils.write("N:CON,0,0\r\n");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_p:
                clickBtn = "P";
                String P = mEtP.getText().toString();
                int parseInt = 0;
                try {
                    parseInt = Integer.parseInt(P);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "功率范围在10-30之间", Toast.LENGTH_SHORT).show();
                }
                if (parseInt >= 10 && parseInt <= 30) {
                    sendMessage(transferSocket, "P:" + P);
                } else {
                    Toast.makeText(this, "功率范围在10-30之间", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn_c:
                clickBtn = "C";
                String countStr = mEtCount.getText().toString();
                String rssiStr = mEtRssi.getText().toString();
                String rssiCountStr = mEtRssiCount.getText().toString();
                String period = et_period.getText().toString();
                String duration = et_duration.getText().toString();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("C:");
                if (TextUtils.isEmpty(countStr)) {
                    stringBuffer.append(",");
                } else {
                    stringBuffer.append(countStr + ",");
                }

                if (TextUtils.isEmpty(rssiStr)) {
                    stringBuffer.append(",");
                } else {
                    stringBuffer.append(rssiStr + ",");
                }

                if (TextUtils.isEmpty(rssiCountStr)) {
                    stringBuffer.append(",");
                } else {
                    stringBuffer.append(rssiCountStr + ",");
                }

                if (TextUtils.isEmpty(period)) {
                    stringBuffer.append(",");
                } else {
                    stringBuffer.append(period + ",");
                }

                if (!TextUtils.isEmpty(duration)) {
                    stringBuffer.append(duration);
                }
                sendMessage(transferSocket, String.valueOf(stringBuffer));
                break;
            case R.id.btn_s:
                clickBtn = "S";
                sendMessage(transferSocket, "S:");
                break;
            case R.id.btn_i:
                clickBtn = "I";
                sendMessage(transferSocket, "I:");
                break;
            case R.id.btn_g:
                clickBtn = "G";
                sendMessage(transferSocket, "G:");
                break;
            case R.id.btn_e:
                clickBtn = "E";
                sendMessage(transferSocket, "E:");
                break;
            case R.id.btn_r:
                sendMessage(transferSocket, "R:");
                break;
            case R.id.btn_disconnect:
                if (listenForMessagesThread != null) {
                    listenForMessagesThread.interrupt();
                    listenForMessagesThread = null;
                }
                try {
                    if (transferSocket != null) {
                        transferSocket.close();
                        transferSocket = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MyApplication.getInstance().setClientSocket(null);
                Toast.makeText(this, "蓝牙断开成功", Toast.LENGTH_SHORT).show();
//                SharedXmlUtil.getInstance(this).write("address", "");
                mTvShowInfo.setText("请先连接蓝牙设备\n");
                mBtnC.setEnabled(false);
                mBtnDisconnect.setEnabled(false);
                mBtnG.setEnabled(false);
                mBtnI.setEnabled(false);
                mBtnP.setEnabled(false);
                mBtnR.setEnabled(false);
                mBtnS.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        timer.cancel();
        try {
            if (listenForMessagesThread != null) {
                listenForMessagesThread.interrupt();
                listenForMessagesThread = null;
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
            if (transferSocket != null) {
                transferSocket.close();
                transferSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
