package com.speedata.bluetoothuhfclient;

import android.content.Context;

import java.util.List;

import xyz.reginer.baseadapter.BaseAdapterHelper;
import xyz.reginer.baseadapter.CommonRvAdapter;

/**
 * Created by 张明_ on 2017/8/17.
 * Email 741183142@qq.com
 */

public class RVAdapter extends CommonRvAdapter<RVBean> {
    public RVAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    public RVAdapter(Context context, int layoutResId, List<RVBean> data) {
        super(context, layoutResId, data);
    }

    @Override
    public void convert(BaseAdapterHelper helper, RVBean item, int position) {
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_address,item.getAddress());
    }
}
