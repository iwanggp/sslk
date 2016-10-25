package com.jufan.cyss.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jufan.cyss.wo.ui.R;

/**
 * Created by cyjss on 2015/3/14.
 */
public class LoadingAdapter extends BaseAdapter {

    private Context ctx;

    public LoadingAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(ctx, R.layout.list_loading_item, null);
        return convertView;
    }
}
