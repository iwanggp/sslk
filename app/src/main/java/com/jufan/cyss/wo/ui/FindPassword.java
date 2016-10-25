package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.SimpleStorageUtil;
import com.mobsandgeeks.saripaar.annotation.TextRule;

import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Date;

/**
 * Created by cyjss on 2015/3/28.
 */
public class FindPassword extends BaseUNIActivity {
    @TextRule(order = 1, message = "请输入正确的手机号", minLength = 11, maxLength = 11)
    @BindView(id = R.id.phoneNumEt)
    private EditText phoneNumEt;
    @BindView(id = R.id.checkNum)
    private EditText checkNum;
    @BindView(id = R.id.getCheckNum, click = true)
    private Button getCheckNum;
    @BindView(id = R.id.pwdEt)
    private EditText pwdEt;
    @BindView(id = R.id.pwdVerifyEt)
    private EditText pwdVerifyEt;

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
        setContentView(R.layout.activity_find_password);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        setupActionBar("找回密码", ActionBarType.BACK);
        setRightBtn("重置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///验证验证码
                String checkNumStr = checkNum.getText().toString().trim();
                phoneNum = phoneNumEt.getText().toString();
                String pwd = pwdEt.getText().toString();
                String pwdVerify = pwdVerifyEt.getText().toString();
                if (StringUtils.isEmpty(phoneNum)) {
                    phoneNumEt.setError("手机号不可为空");
                    return;
                }
                if (StringUtils.isEmpty(pwd)) {
                    pwdEt.setError("重置密码不可为空");
                    return;
                }
                if (pwd.length() < 6 || pwd.length() > 20) {
                    pwdEt.setError("重置密码在6~20位之间");
                    return;
                }
                if (!pwd.equals(pwdVerify)) {
                    pwdVerifyEt.setError("两次密码不一致");
                    return;
                }
                showLoading();
                AVUser.resetPasswordBySmsCodeInBackground(checkNumStr, pwd, new UpdatePasswordCallback() {
                    @Override
                    public void done(AVException e) {
                        hideLoading();
                        if (e != null) {
                            checkNum.setError("验证码错误");
                            ViewInject.longToast("验证码错误");
                        } else {
                            ViewInject.longToast("重置密码成功");
                            finish();
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
                    if (i == 0) {
                        hideLoading();
                        getCheckNum.setEnabled(true);
                        ViewInject.longToast("该手机号未注册");
                    } else {
                        AVUser.requestPasswordResetBySmsCodeInBackground(phoneNum, new RequestMobileCodeCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e != null) {
                                    ViewInject.longToast(e.getCode() + ":error");
                                    getCheckNum.setEnabled(true);
                                } else {
                                    SimpleStorageUtil.saveKeyValue(FindPassword.this, new String[]{
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

}
