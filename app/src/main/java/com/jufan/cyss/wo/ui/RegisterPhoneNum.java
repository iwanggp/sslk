package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.SignUpCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.SimpleStorageUtil;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Date;

/**
 * Created by cyjss on 2014/12/24.
 */
public class RegisterPhoneNum extends BaseUNIActivity {
    @TextRule(order = 1, message = "请输入正确的手机号", minLength = 11, maxLength = 11)
    @BindView(id = R.id.phoneNumEt)
    private EditText phoneNumEt;
    @BindView(id = R.id.checkNum)
    private EditText checkNum;
    @BindView(id = R.id.getCheckNum, click = true)
    private Button getCheckNum;
    private int gapTime = 120;

    private String phoneNum;

    private Handler handler = new Handler();
    private Runnable tikTokThread = new Runnable() {
        @Override
        public void run() {
            if (gapTime > 0) {
                gapTime--;
                getCheckNum.setText("请接收验证码(" + gapTime + ")");
                handler.postDelayed(tikTokThread, 1000);
            } else {
                gapTime = 120;
                getCheckNum.setEnabled(true);
                getCheckNum.setText("获取验证码");
            }
        }
    };

    private final static String checkNumKey = "checkNumKey";

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_register_phonenum);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setupActionBar("注册", ActionBarType.BACK);
        setRightBtn("下一步", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                ///验证验证码
                String checkNumStr = checkNum.getText().toString().trim();
                phoneNum = phoneNumEt.getText().toString();
                if (StringUtils.isEmpty(phoneNum)) {
                    phoneNumEt.setError("手机号不可为空");
                    return;
                }
                AVOSCloud.verifySMSCodeInBackground(checkNumStr, phoneNum, new AVMobilePhoneVerifyCallback() {
                    @Override
                    public void done(AVException e) {
                        hideLoading();
                        if (e != null) {
                            checkNum.setError("验证码错误");
                        } else {
                            Intent i = new Intent(RegisterPhoneNum.this, RegisterPassword.class);
                            i.putExtra("phone_num", phoneNum);
                            startActivityForResult(i, 0);
                        }
                    }
                });
            }
        });
        String checkDate = SimpleStorageUtil.getValue(this, checkNumKey);
        if (!StringUtils.isEmpty(checkDate)) {
            Date time = DateUtil.detailFormat(checkDate);
            Date now = new Date();
            long gap = now.getTime() - time.getTime();
            if (gap < 120 * 1000) {
                gapTime = (int) (120 * 1000 - gap) / 1000;
                handler.postDelayed(tikTokThread, 0);
                getCheckNum.setEnabled(false);
            }
        }
    }

    @Override
    public void onValidationSucceeded() {
        showLoading();
        ///获取验证码
        getCheckNum.setEnabled(false);
        phoneNum = phoneNumEt.getText().toString();
        AVQuery<AVObject> query = new AVQuery<AVObject>("_User");
        query.whereEqualTo("mobilePhoneNumber", phoneNum);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    if (i > 0) {
                        hideLoading();
                        getCheckNum.setEnabled(true);
                        ViewInject.longToast("该手机号已注册");
                    } else {
                        AVOSCloud.requestSMSCodeInBackgroud(phoneNum, "沃看路况", "注册", 20, new RequestMobileCodeCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e != null) {
                                    Log.e("RegisterPhoneNum", "", e);
                                    ViewInject.longToast(e.getCode() + ":error");
                                } else {
                                    SimpleStorageUtil.saveKeyValue(RegisterPhoneNum.this, new String[]{
                                            "checkNumKey"
                                    }, new String[]{
                                            DateUtil.detailDateStr(new Date())
                                    });
                                    handler.postDelayed(tikTokThread, 0);
                                }
                                hideLoading();
                            }
                        });
                    }
                } else {
                    hideLoading();
                    getCheckNum.setEnabled(true);
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.getCheckNum:
                validate();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            setResult(resultCode);
            finish();
        } else if (resultCode == 200) {
            setResult(resultCode);
            finish();
        }
    }
}
