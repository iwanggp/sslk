package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.JtgzfwHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cyjss on 2015/1/31.
 */
public class BindCar extends BaseUNIActivity {

    @TextRule(order = 1, minLength = 5, maxLength = 5, message = "请输入后5位")
    @BindView(id = R.id.hphm)
    private EditText hphm;
    @BindView(id = R.id.hpzlGroup)
    private RadioGroup hpzl;
    @Required(order = 2, message = "车辆识别代号不可为空")
    @BindView(id = R.id.clsbdh)
    private EditText clsbdh;

    private String hphmStr;
    private String clsbdhStr;
    private String hpzlStr;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_bind_car);
    }

    @Override
    protected void initWidget() {
        setupActionBar("绑定车辆", ActionBarType.BACK);
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText("绑定");
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        showLoading();
        hphmStr = hphm.getText().toString().trim().toUpperCase();
        clsbdhStr = clsbdh.getText().toString().trim().toUpperCase();
        RadioButton hpzlRb = (RadioButton) findViewById(hpzl.getCheckedRadioButtonId());
        hpzlStr = hpzlRb.getTag().toString();
        hphm.setText(hphmStr);
        clsbdh.setText(clsbdhStr);
        try {
            JSONObject obj = new JSONObject();
            obj.put("hphm", hphmStr);
            obj.put("clsbdh", clsbdhStr);
            obj.put("hpzl", hpzlStr);
            Groundy.create(JtgzfwHttp.class).callback(this).arg("code", "S50100").arg("json", obj.toString()).queueUsing(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnSuccess(JtgzfwHttp.class)
    public void onSuccess(@Param("data") String data) {
        JSONObject json = null;
        try {
            json = new JSONObject(data);
            json.put("update_time", DateUtil.detailDateStr(new Date()));
            data = json.toString();
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

        AVUser user = AVUser.getCurrentUser();
        if (user == null) {
            hideLoading();
            setResult(RESULT_OK);
            finish();
        } else {
            try {
                JSONObject obj = new JSONObject();
                obj.put("hphm", hphmStr);
                obj.put("hpzl", hpzlStr);
                obj.put("sjhm", user.getMobilePhoneNumber());
                obj.put("flag", true);
                Groundy.create(JtgzfwHttp.class).callback(new BindJtgzfwUser()).arg("code", "P24028").arg("json", obj.toString()).queueUsing(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnFailure(JtgzfwHttp.class)
    public void onFailure(@Param("code") String code, @Param("desc") String desc) {
        ViewInject.longToast(desc);
        hideLoading();
    }

    private class BindJtgzfwUser {
        @OnSuccess(JtgzfwHttp.class)
        public void onSuccess(@Param("data") String data) {
            String username = null;
            try {
                JSONObject backObj = new JSONObject(data);
                username = backObj.getString("username");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (username != null) {
                AVUser user = AVUser.getCurrentUser();
                AVObject bindVeh = new AVObject("BindVeh");
                bindVeh.put("hphm", hphmStr);
                bindVeh.put("clsbdh", clsbdhStr);
                bindVeh.put("hpzl", hpzlStr);
                bindVeh.put("userId", user.getObjectId());
                bindVeh.put("user", user);
                bindVeh.put("jtgzfwUsername", username);
                bindVeh.put("mobilePhoneNumber", user.getMobilePhoneNumber());
                bindVeh.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            GlobalUtil.showNetworkError();
                        }
                        hideLoading();
                    }
                });
            }
        }

        @OnFailure(JtgzfwHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            ViewInject.longToast(desc);
            hideLoading();
        }
    }
}
