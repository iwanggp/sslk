package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.JtgzfwHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cyjss on 2015/1/31.
 */
public class MyVio extends BaseUNIActivity {

    @BindView(id = R.id.year)
    private TextView year;
    @BindView(id = R.id.vioList)
    private KJListView vioList;
    private JSONArray wzxx;
    @BindView(id = R.id.goFeeBtn, click = true)
    private Button goFeeBtn;
    @BindView(id = R.id.vioCheckNum)
    private TextView vioCheckNum;
    @BindView(id = R.id.vioFeeNum)
    private TextView vioFeeNum;
    @BindView(id = R.id.feeTipContainer)
    private RelativeLayout feeTipContainer;
    @BindView(id = R.id.vioTip)
    private TextView vioTip;

    private VioListAdapter adapter;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_my_vio);
    }

    @Override
    protected void initWidget() {
        setupActionBar("我的违章", ActionBarType.BACK);
        this.adapter = new VioListAdapter();
        Intent intent = getIntent();
        String hphm = intent.getStringExtra("hphm");
        String hpzl = intent.getStringExtra("hpzl");
        String clsbdh = intent.getStringExtra("clsbdh");
        if (hphm.length() == 5) {
            hphm = "A" + hphm;
        }

        Simple simple = Simple.getByKey("vio");
        try {
            JSONObject vio = new JSONObject(simple.value);
            JSONObject veh = new JSONObject(vio.getString(hphm + hpzl));
            setupActionBar("我的违章[豫" + hphm + "]", ActionBarType.BACK);

            if (veh.has("update_time")) {
                Date updateTime = DateUtil.detailFormat(veh.getString("update_time"));
                if (updateTime != null && new Date().getTime() - updateTime.getTime() < 1000 * 60 * 60) {
                    wzxx = veh.getJSONArray("wzxx");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            simple.delete();
            ViewInject.longToast("数据损坏，请重新请求");
            finish();
        }
        vioList.setPullLoadEnable(false);
        vioList.setPullRefreshEnable(false);
        vioList.getFooterView().setVisibility(View.GONE);
        vioList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                try {
                    JSONObject json = wzxx.getJSONObject(firstVisibleItem);
                    String wfsj = json.getString("wfsj");
                    String yearStr = wfsj.substring(0, 4) + "年";
                    year.setText(yearStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        Groundy.create(JtgzfwHttp.class).callback(new SearchCanFeeVio()).arg("code", "S24029").arg("json", "{}").queueUsing(this);
        if (wzxx == null) {
            showLoading();
            try {
                JSONObject obj = new JSONObject();
                obj.put("hphm", hphm.substring(1));
                obj.put("clsbdh", clsbdh);
                obj.put("hpzl", hpzl);
                Groundy.create(JtgzfwHttp.class).callback(this).arg("code", "S50100").arg("json", obj.toString()).queueUsing(this);
            } catch (Exception e) {
            }
        } else {
            if (wzxx.length() != 0) {
                initFeeBtn();
                vioList.setAdapter(adapter);
            } else {
                setNoVioAdapter();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //若未登录
        if (GlobalUtil.isLogin()) {
            vioTip.setVisibility(View.GONE);
        } else {
            vioTip.setVisibility(View.VISIBLE);
        }
    }

    private void initFeeBtn() {
        final Button right = (Button) findViewById(R.id.rightBtn);
//        right.setVisibility(View.VISIBLE);
        right.setText("缴费");
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mode = adapter.getMode();
                if (mode == 0) {
                    right.setText("取消");
                    feeTipContainer.setVisibility(View.VISIBLE);
                    adapter.setMode(1);
                } else {
                    right.setText("缴费");
                    feeTipContainer.setVisibility(View.GONE);
                    Set<Integer> set = adapter.getCheckSet();
                    int vioCount = set.size();
                    int fee = 0;
                    for (Integer index : set) {
                        try {
                            JSONObject json = wzxx.getJSONObject(index);
                            fee += Integer.parseInt(json.getString("fkje"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    vioCheckNum.setText("" + vioCount);
                    vioFeeNum.setText("" + fee);
                    adapter.setMode(0);
                }
            }
        });
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.goFeeBtn:
                Set<Integer> set = adapter.getCheckSet();
                break;
        }
    }

    @OnSuccess(JtgzfwHttp.class)
    public void onSuccess(@Param("data") String data) {
        hideLoading();
        JSONObject json = null;
        try {
            json = new JSONObject(data);
            json.put("update_time", DateUtil.detailDateStr(new Date()));
            data = json.toString();
            wzxx = json.getJSONArray("wzxx");
            if (wzxx.length() != 0) {
                initFeeBtn();
            }
        } catch (JSONException e) {
        }
        Simple simple = Simple.getByKey("vio");
        try {
            JSONObject obj = new JSONObject();
            if (StringUtils.isEmpty(simple.value)) {
                simple.value = obj.toString();
            } else {
                obj = new JSONObject(simple.value);
            }
            obj.put(json.getString("hphm") + json.getString("hpzl"), data);
            simple.value = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        simple.save();
        if (wzxx.length() == 0) {
            setNoVioAdapter();
        } else {
            vioList.setAdapter(adapter);
        }
    }

    private void setNoVioAdapter() {
        vioList.setAdapter(new NoDataAdapter(this, "没有违章，继续保持"));
    }

    @OnFailure(JtgzfwHttp.class)
    public void onFailure(@Param("code") String code, @Param("desc") String desc) {
        ViewInject.longToast(desc);
        hideLoading();
    }

    private class SearchCanFeeVio {
        @OnSuccess(JtgzfwHttp.class)
        public void onSuccess(@Param("data") String data) {
            hideLoading();
            try {
                JSONObject json = new JSONObject(data);
                wzxx = json.getJSONArray("wzxx");
            } catch (JSONException e) {
            }
        }

        @OnFailure(JtgzfwHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            ViewInject.longToast(desc);
            hideLoading();
        }
    }

    private class VioListAdapter extends BaseAdapter {

        private int mode = 0;

        private Set<Integer> feeCheckSet = new HashSet<Integer>();

        public VioListAdapter() {
        }

        @Override
        public int getCount() {
            return wzxx.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return wzxx.get(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Set<Integer> getCheckSet() {
            return feeCheckSet;
        }

        public void setMode(int mode) {
            this.mode = mode;
            notifyDataSetChanged();
        }

        public int getMode() {
            return mode;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            JSONObject json = (JSONObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(MyVio.this).inflate(R.layout.list_vio_item, null);
                holder = new Holder();
                holder.wfdzTv = (TextView) convertView.findViewById(R.id.wfdz);
                holder.dateTv = (TextView) convertView.findViewById(R.id.date);
                holder.timeTv = (TextView) convertView.findViewById(R.id.time);
                holder.wfxwTv = (TextView) convertView.findViewById(R.id.wfxw);
                holder.wfjcTv = (TextView) convertView.findViewById(R.id.wfjc);
                holder.fkjeTv = (TextView) convertView.findViewById(R.id.fkje);
                holder.wfjfTv = (TextView) convertView.findViewById(R.id.wfjf);
                holder.vioThumbIv = (ImageView) convertView.findViewById(R.id.vioThumb);
                holder.vioThumbIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalUtil.isLogin()) {
                            Intent i = new Intent(MyVio.this, ImageZoom.class);
                            i.putExtra("xh", v.getTag().toString());
                            startActivity(i);
                        } else {
                            Intent i = new Intent(MyVio.this, Login.class);
                            startActivity(i);
                        }
                    }
                });
                holder.feeCheckCb = (CheckBox) convertView.findViewById(R.id.feeCheck);
                holder.feeCheckCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int index = Integer.parseInt(buttonView.getTag().toString());
                        if (isChecked) {
                            feeCheckSet.add(index);
                        } else {
                            if (feeCheckSet.contains(index)) {
                                feeCheckSet.remove(index);
                            }
                        }
                        int vioCount = feeCheckSet.size();
                        int fee = 0;
                        for (Integer i : feeCheckSet) {
                            try {
                                JSONObject json = wzxx.getJSONObject(i);
                                fee += Integer.parseInt(json.getString("fkje"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        vioCheckNum.setText("" + vioCount);
                        vioFeeNum.setText("" + fee);
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            String wfsj = "";
            String yearStr = "";
            String date = "";
            String time = "";
            String wfxw = "";
            try {
                wfsj = json.getString("wfsj");
                wfxw = json.getString("wfxw");
                yearStr = wfsj.substring(0, 4);
                date = wfsj.substring(5, 7) + "-" + wfsj.substring(8, 10);
                time = wfsj.substring(11);
                String url = HttpUtil.getReqVioThumbUrl(json.getString("xh"));
                ImageLoader.getInstance().displayImage(url, holder.vioThumbIv, HttpUtil.DefaultOptions);
                holder.vioThumbIv.setTag(json.getString("xh"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            GlobalUtil.setString(holder.wfdzTv, json, "wfdz");
            holder.dateTv.setText(date);
            holder.timeTv.setText(wfsj);
            GlobalUtil.setString(holder.wfxwTv, json, "wfxw");
            GlobalUtil.setString(holder.wfjcTv, json, "wfjc");
            GlobalUtil.setString(holder.fkjeTv, json, "fkje");

            String wfjf = "0";
            if (Integer.parseInt(yearStr) >= 2013) {
                wfjf = wfxw.substring(1, 2);
            }
            holder.wfjfTv.setText(wfjf);

            if (mode == 0) {
                holder.feeCheckCb.setVisibility(View.GONE);
            } else if (mode == 1) {
                holder.feeCheckCb.setVisibility(View.VISIBLE);
                holder.feeCheckCb.setTag(position);
                holder.feeCheckCb.setChecked(feeCheckSet.contains(position));
            }
            return convertView;
        }
    }

    private class Holder {
        public TextView wfdzTv;
        public TextView dateTv;
        public TextView timeTv;
        public TextView wfxwTv;
        public TextView wfjcTv;
        public TextView fkjeTv;
        public TextView wfjfTv;
        public ImageView vioThumbIv;
        public CheckBox feeCheckCb;
    }
}
