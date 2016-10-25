package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.service.CustomPushService;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;

import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

/**
 * Created by cyjss on 2015/1/28.
 */
public class Login extends BaseUNIActivity {
    @BindView(id = R.id.register, click = true)
    private Button register;
    @BindView(id = R.id.loginBtn, click = true)
    private Button loginBtn;
    @TextRule(order = 1, message = "请输入正确的手机号", minLength = 11, maxLength = 11)
    @BindView(id = R.id.phoneNum)
    private EditText phoneNum;
    @Required(order = 2, message = "请输入密码")
    @TextRule(order = 3, message = "密码长度在6~15位", minLength = 6, maxLength = 15)
    @BindView(id = R.id.password)
    private EditText password;
    @BindView(id = R.id.forgetPwd, click = true)
    private TextView forgetPwd;

    private final int LOGIN_SUCCESS = 100;
    private final int LOGIN_FAIL = 200;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void initWidget() {
        setupActionBar("欢迎使用");
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                Intent registerIntent = new Intent(this, RegisterPhoneNum.class);
                startActivityForResult(registerIntent, 0);
//                startActivity(registerIntent);
                break;
            case R.id.loginBtn:
                validate();
                break;
            case R.id.forgetPwd:
                Intent resetPwdIntent = new Intent(this, FindPassword.class);
                startActivity(resetPwdIntent);
                break;
        }
    }

    @Override
    public void onValidationSucceeded() {
        showLoading();
        AVUser.loginByMobilePhoneNumberInBackground(phoneNum.getText().toString(), password.getText().toString(), new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e != null) {
                    phoneNum.setError("用名密码错误");
                } else {
                    registerDeviceToken();
                    finish();
                }
                hideLoading();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == LOGIN_SUCCESS) {
            registerDeviceToken();
            finish();
        } else if (resultCode == LOGIN_FAIL) {

        }
    }

    private void registerDeviceToken() {
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
        String deviceToken = UmengRegistrar.getRegistrationId(this);
        Log.d("Logo", "device token ====>" + deviceToken);
        mPushAgent.setPushIntentServiceClass(CustomPushService.class);
        AVUser user = AVUser.getCurrentUser();
        if (user != null && !StringUtils.isEmpty(deviceToken)) {
            user.put("deviceToken", deviceToken);
            user.saveInBackground();
        }
    }
}
