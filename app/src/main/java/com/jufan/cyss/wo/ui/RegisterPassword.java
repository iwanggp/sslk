package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

/**
 * Created by cyjss on 2014/12/24.
 */
public class RegisterPassword extends BaseUNIActivity {

    @Required(order = 1, message = "昵称不可为空")
    @TextRule(order = 6, maxLength = 15, minLength = 4, message = "昵称在4~15位之间")
    @BindView(id = R.id.nickNameEt)
    private EditText nickNameEt;
    @Required(order = 2, message = "密码不可为空")
    @TextRule(order = 3, message = "密码大于5位小于16位", minLength = 6, maxLength = 15)
    @BindView(id = R.id.passwordEt)
    private EditText passwordEt;
    @Required(order = 4, message = "确认密码不可为空")
    @TextRule(order = 5, message = "确认密码大于5位小于16位", minLength = 6, maxLength = 15)
    @BindView(id = R.id.passwordAgainEt)
    private EditText passwordAgainEt;
    private String phoneNum;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_register_password);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        this.phoneNum = getIntent().getStringExtra("phone_num");
        Log.d("", "====>" + this.phoneNum);
        setupActionBar("密码设置", ActionBarType.BACK);
        setRightBtn("完成", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickNameEt.setText(nickNameEt.getText().toString().trim());
                validate();
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        if (!passwordEt.getText().toString().equals(passwordAgainEt.getText().toString())) {
            passwordAgainEt.setError("请确认输入密码一致");
            return;
        }
        showLoading();
        final String password = passwordEt.getText().toString();
        final AVUser user = new AVUser();
        user.setMobilePhoneNumber(phoneNum);
        user.setUsername(nickNameEt.getText().toString());
        user.setPassword(password);
        user.put("mobilePhoneVerified", true);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e != null) {
                    ViewInject.longToast(e.getCode() + ":注册失败,请稍后再试");
                    hideLoading();
                } else {
                    user.loginByMobilePhoneNumberInBackground(phoneNum, password, new LogInCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            hideLoading();
                            if (e != null) {
                                setResult(200);
                            } else {
                                setResult(100);
                            }
                            finish();
                        }
                    });
                }
            }
        });
    }
}
