package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.AroundMap;
import com.jufan.cyss.wo.ui.R;

import java.util.Objects;

/**
 * Created by cyjss on 2015/3/4.
 */
public class AroundDialog extends Dialog implements AdapterView.OnItemClickListener {

    private GridView aroundGridView;

    private AroundAdapter adapter;
    private Button cancelBtn;

    private final Object[] Around = {
            "加油站", R.drawable.ic_near_jiayouzhan, "加油站",
            "酒店", R.drawable.ic_near_jiudian, "酒店",
            "洗车", R.drawable.ic_near_xiche, "洗车",
            "银行", R.drawable.ic_near_yinhang, "银行",
            "停车场", R.drawable.ic_near_tingchechang, "停车场",
            "汽车维修", R.drawable.ic_near_qicheweixiu, "汽车维修",
            "汽车美容", R.drawable.ic_near_qichemeirong, "汽车美容"
    };

    public AroundDialog(Context context) {
        super(context, R.style.selectDataDialog);
        setContentView(R.layout.dialog_around);
        this.aroundGridView = (GridView) findViewById(R.id.aroundGridView);
        this.cancelBtn = (Button) findViewById(R.id.cancelBtn);
        this.aroundGridView.setOnItemClickListener(this);

        this.adapter = new AroundAdapter();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = GlobalUtil.dip2px(getContext(), 400);
        lp.width = window.getWindowManager().getDefaultDisplay().getWidth();
        lp.x = 0;
        lp.y = window.getWindowManager().getDefaultDisplay().getHeight() - lp.height;
        window.setAttributes(lp);

        this.aroundGridView.setAdapter(this.adapter);
        this.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position * 3;
        Intent i = new Intent(getContext(), AroundMap.class);
        i.putExtra("title", Around[index + 2].toString());
        getContext().startActivity(i);
        dismiss();
    }

    private class AroundAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Around.length / 3;
        }

        @Override
        public Object getItem(int position) {
            int index = position * 3;
            return new Object[]{
                    Around[index], Around[index + 1], Around[index + 2]
            };
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object[] item = (Object[]) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.grid_around_item, null);
            }
            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
            TextView tv = (TextView) convertView.findViewById(R.id.icDesc);
            iv.setImageResource((int) item[1]);
            tv.setText(item[0].toString());
            return convertView;
        }
    }
}
