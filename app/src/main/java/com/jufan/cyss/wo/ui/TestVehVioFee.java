package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.JtgzfwHttp;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by cyjss on 2015/3/12.
 */
public class TestVehVioFee extends BaseUNIActivity {

    @BindView(id = R.id.vioFeeList)
    private ListView vioFeeList;
    private JSONArray wzxx;
    private Set<String> checkSet = new HashSet<String>();

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_test_vehviofee);
    }

    @Override
    protected void initWidget() {
        setupActionBar("违章缴费", ActionBarType.BACK);
        showLoading();
        Groundy.create(JtgzfwHttp.class).callback(new SearchCanFeeVio()).arg("code", "S24029").arg("json", "{}").queueUsing(this);
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText("缴费");
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSet.isEmpty()) {
                    ViewInject.longToast("请先选择违章");
                    return;
                }
                String xhs = "";
                for (String xh : checkSet) {
                    xhs += xh + ",";
                }
                xhs = xhs.substring(0, xhs.length() - 1);
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("xh", xhs);
                    Groundy.create(JtgzfwHttp.class).callback(new HandleFeeTn()).arg("code", "P24024").arg("json", obj.toString()).queueUsing(TestVehVioFee.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private class SearchCanFeeVio {
        @OnSuccess(JtgzfwHttp.class)
        public void onSuccess(@Param("data") String data) {
            hideLoading();
            try {
                JSONObject json = new JSONObject(data);
                wzxx = json.getJSONArray("wzxx");
                vioFeeList.setAdapter(new TestVehVioFeeAdapter());
            } catch (JSONException e) {
            }
        }

        @OnFailure(JtgzfwHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            ViewInject.longToast(desc);
            hideLoading();
        }
    }

    private class HandleFeeTn {
        @OnSuccess(JtgzfwHttp.class)
        public void onSuccess(@Param("data") String data) {
            hideLoading();
            try {
                JSONObject json = new JSONObject(data);
                String tn = json.getString("tn");
                UPPayAssistEx.startPayByJAR(TestVehVioFee.this, PayActivity.class, null, null,
                        tn, "01");
            } catch (JSONException e) {
            }
        }

        @OnFailure(JtgzfwHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            ViewInject.longToast(desc);
            hideLoading();
        }
    }

    private class TestVehVioFeeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return wzxx.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return wzxx.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject jobj = (JSONObject) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(TestVehVioFee.this, R.layout.list_viofee_item, null);
            }
            TextView wfdz = (TextView) convertView.findViewById(R.id.wfdz);
            TextView wfsj = (TextView) convertView.findViewById(R.id.wfsj);
            TextView fkje = (TextView) convertView.findViewById(R.id.fkje);
            TextView wfjf = (TextView) convertView.findViewById(R.id.wfjf);
            CheckBox feeCheck = (CheckBox) convertView.findViewById(R.id.feeCheck);
            feeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String xh = buttonView.getTag().toString();
                    if (isChecked) {
                        checkSet.add(xh);
                    } else {
                        checkSet.remove(xh);
                    }
                }
            });
            try {
                String xh = jobj.getString("xh");
                wfdz.setText(jobj.getString("wfdz"));
                wfsj.setText(jobj.getString("wfsj"));
                fkje.setText(jobj.getString("fkje"));
                wfjf.setText(jobj.getString("wfjf"));
                feeCheck.setTag(xh);
                feeCheck.setChecked(checkSet.contains(xh));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }
}
