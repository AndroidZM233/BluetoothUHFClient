//PS：在目前Android手机中，是不支持在飞行模式下开启蓝牙的。如果蓝牙已经开启，那么蓝牙的开关状态会随着飞行模式的状态而发生改变
package com.speedata.bluetoothuhfclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import xyz.reginer.baseadapter.CommonRvAdapter;

public class DeviceScanActivity extends BaseAct implements CommonRvAdapter.OnItemClickListener{

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private RVAdapter mAdapter;
    private List<RVBean> mList = new ArrayList<RVBean>();
    private LinearLayoutManager layoutManager;
    private static final int REQUEST_ENABLE_BT = 1;
    private RecyclerView rv_content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
        getActionBar().setTitle("设备搜索");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        initRV();
        //开始搜索
        startDiscovery();
        addBoundDevice(mBluetoothAdapter);
    }
    private void initRV() {
        mAdapter = new RVAdapter(DeviceScanActivity.this, R.layout.item_info, mList);
        rv_content.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        layoutManager = new LinearLayoutManager(this);
        rv_content.setLayoutManager(layoutManager);
        rv_content.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                //开始搜索
                startDiscovery();
                Log.d("ZM", "onOptionsItemSelected: scan");
                break;
            case R.id.menu_stop:
                stopDiscovery();
                Log.d("ZM", "onOptionsItemSelected: stop");
                break;
        }
        return true;
    }

    //开始扫描
    private void startDiscovery() {
        // TODO Auto-generated method stub
        registerReceiver(discoveryReceiver, new IntentFilter(
                BluetoothDevice.ACTION_FOUND));
        mList.clear();
        boolean startDiscovery = mBluetoothAdapter.startDiscovery();
        if (startDiscovery){
            mScanning=true;
        }else {
            mScanning=false;
        }
        invalidateOptionsMenu();

    }
    //停止扫描
    private void stopDiscovery(){
        mBluetoothAdapter.cancelDiscovery();
        mScanning=false;
        //刷新Menu
        invalidateOptionsMenu();
    }

    BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            RVBean rvBean=new RVBean();
            BluetoothDevice bluetoothDevice = (BluetoothDevice) arg1
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            rvBean.setBluetoothDevice(bluetoothDevice);
            rvBean.setName(arg1.getStringExtra(BluetoothDevice.EXTRA_NAME));
            rvBean.setAddress(bluetoothDevice.getAddress());
            mList.add(rvBean);
            mAdapter.notifyDataSetChanged();
        }
    };
    //得到已绑定的设备
    private void addBoundDevice(BluetoothAdapter bAdapter) {
        Set<BluetoothDevice> set = bAdapter.getBondedDevices();
        Log.d("test", "set:" + set.size());
        for (BluetoothDevice device : set) {
            RVBean rvBean=new RVBean();
            rvBean.setName(device.getName());
            rvBean.setAddress(device.getAddress());
            rvBean.setBluetoothDevice(device);
            mList.add(rvBean);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDiscovery();
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        rv_content = (RecyclerView) findViewById(R.id.rv_content);
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, View view, int position) {
        final BluetoothDevice bluetoothDevice = mList.get(position).getBluetoothDevice();
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("连接中...");
        progressDialog.show();
        stopDiscovery();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothSocket clientSocket = bluetoothDevice
                            .createRfcommSocketToServiceRecord(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
                    clientSocket.connect();
                    MyApplication.getInstance().setClientSocket(clientSocket);
                    EventBus.getDefault().post(new MsgEvent("connectSuccess",
                            bluetoothDevice.getName()+"蓝牙连接成功\n"));
                    progressDialog.dismiss();
                    SharedXmlUtil.getInstance(getApplicationContext()).write("address",bluetoothDevice.getAddress());
                    DeviceScanActivity.this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DeviceScanActivity.this, "连接失败",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).start();

    }

}