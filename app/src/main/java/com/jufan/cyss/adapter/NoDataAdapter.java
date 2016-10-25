package com.jufan.cyss.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jufan.cyss.wo.ui.R;

import org.kymjs.aframe.utils.StringUtils;

/**
 * Created by cyjss on 2015/3/14.
 */
public class NoDataAdapter extends BaseAdapter {

    private Context ctx;
    private String tip;

    public NoDataAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public NoDataAdapter(Context ctx, String tip) {
        this.ctx = ctx;
        this.tip = tip;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(ctx, R.layout.list_no_data_item, null);
        if (!StringUtils.isEmpty(this.tip)) {
            TextView tv = (TextView) convertView.findViewById(R.id.noDataTip);
            tv.setText(tip);
        }
        return convertView;
    }
}
