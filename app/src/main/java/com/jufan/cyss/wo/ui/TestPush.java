package com.jufan.cyss.wo.ui;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.JtgzfwHttp;
import com.jufan.cyss.http.UMPushHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.MD5;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;
import com.umeng.message.UmengRegistrar;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by cyjss on 2015/3/17.
 */
public class TestPush extends BaseUNIActivity {

    @BindView(id = R.id.testBtn, click = true)
    private Button testBtn;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_test_vehviofee);
    }

    @Override
    public void widgetClick(View v) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void initWidget() {
        setupActionBar("测试推送", ActionBarType.BACK);
//        String deviceToken = UmengRegistrar.getRegistrationId(this);
//        JSONObject sendObj = UMPushHttp.getCustomObj(getClass().getSimpleName(), null, UMPushHttp.SHOW_IN_NOTIFICATION);
//        try {
//            sendObj.put("text", "jj地点!@#bb");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Groundy.create(UMPushHttp.class).arg("type", "broadcast").arg("json", sendObj.toString()).queueUsing(this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject obj = new JSONObject();
//                try {
//                    obj.put("channel", "XHS");
//                    obj.put("road_key", "JSLYFL");
//                    obj.put("timestamp", "" + System.currentTimeMillis());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String signUrl = "http://125.46.83.214/sslk/video/api";
//                try {
//                    Log.d("-==-=>?", "http://222.137.116.89/sslk/video/api?sign=" + MD5.encrypt32(signUrl + obj.toString() + "XHS"));
//                    String ret = HttpUtil.doPost("http://125.46.83.214/sslk/video/api?sign=" + MD5.encrypt32(signUrl + obj.toString() + "XHS"), obj.toString());
//                    Log.d("-==-=>?", ret);
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }

    @Override
    public void executeReceiver(JSONObject json) {
        Log.d("TestPush", "--==>" + json.toString());
    }

}
