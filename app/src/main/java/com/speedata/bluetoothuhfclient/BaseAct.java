package com.speedata.bluetoothuhfclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by 张明_ on 2017/8/16.
 * Email 741183142@qq.com
 */

public class BaseAct extends Activity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                openAct(this, DeviceScanActivity.class);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    public void openAct(Context packageContext, Class<?> cls){
        Intent intent=new Intent(packageContext,cls);
        startActivity(intent);
    }
}
