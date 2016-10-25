package com.jufan.cyss.frame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.dd.processbutton.iml.ActionProcessButton;
import com.jufan.cyss.http.RoadVideoHttp;
import com.jufan.cyss.http.SimpleHttp;
import com.jufan.cyss.http.UMPushHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.wo.ui.R;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.telly.groundy.Groundy;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;

import org.json.JSONObject;
import org.kymjs.aframe.ui.AnnotateUtil;
import org.kymjs.aframe.ui.KJActivityManager;
import org.kymjs.aframe.ui.activity.I_KJActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by cyjss on 2015/1/28.
 */
public abstract class BaseUNIActivity extends ActionBarActivity implements Validator.ValidationListener, View.OnClickListener, I_KJActivity {

    private ActionProcessButton loadingBtn;
    private int mProgress = 0;
    private Random random = new Random();
    private boolean isLoading = false;
    private int loadingTimes = 0;

    protected List<JSONObject> pushList = new ArrayList<JSONObject>();

    protected BaseUNIApplication application;

    public enum ActionBarType {
        DEFAULT, BACK, COMMUNITY
    }

    /**
     * 验证工具
     */
    protected Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        application = (BaseUNIApplication) getApplication();
        application.addActivity(this);
        super.onCreate(savedInstanceState);     //此处要注意调用顺序，activity会先调用子类的 initWidget函数
        setRootView();
        AnnotateUtil.initBindView(this);
        initWidget();
        PushAgent.getInstance(this).onAppStart();
        KJActivityManager.create().addActivity(this);
        Groundy.create(SimpleHttp.class).callback(this).arg("_url", UMPushHttp.REQ_LOGIN)
                .arg("device_token", UmengRegistrar.getRegistrationId(this))
                .arg("device_activity", getClass().getSimpleName())
                .queueUsing(this);
    }

    protected void setRightBtn(String text, View.OnClickListener click) {
        View v = findViewById(R.id.rightBtn);
        if (v != null) {
            Button rightBtn = (Button) v;
            v.setVisibility(View.VISIBLE);
            rightBtn.setText(text);
            rightBtn.setOnClickListener(click);
        }
    }

    /**
     * listened widget's click method
     */
    @Override
    public void widgetClick(View v) {
    }

    @Override
    public void onClick(View v) {
        widgetClick(v);
    }

    @Override
    public void setRootView() {
    }

    @Override
    public void initialize() {
    }

    protected void initWidget() {
    }

    public void executeReceiver(JSONObject json) {
    }

    public void addPushJson(JSONObject obj) {
        synchronized (pushList) {
            this.pushList.add(obj);
        }
    }

    /**
     * 加载默认的页面头
     */
    public void setupActionBar(String title, ActionBarType type) {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            switch (type) {
                case DEFAULT:
                    actionBar.setCustomView(R.layout.actionbar_default);
                    break;
                case BACK:
                    actionBar.setCustomView(R.layout.actionbar_back);
                    ImageView backBtn = (ImageView) findViewById(R.id.barBackBtn);
                    backBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    break;
                case COMMUNITY:
                    actionBar.setCustomView(R.layout.actionbar_community);
                    break;
            }
            loadingBtn = (ActionProcessButton) findViewById(R.id.loadingBtn);
            loadingBtn.setMode(ActionProcessButton.Mode.ENDLESS);
            loadingBtn.setText(title);
            loadingBtn.setCompleteText(title);
            loadingBtn.setLoadingText(title);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            Toolbar parent = (Toolbar) actionBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
    }

    public void setupActionBar(String title) {
        setupActionBar(title, ActionBarType.DEFAULT);
    }

    public void showLoading() {
        if (loadingBtn == null || isLoading) {
            return;
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mProgress == -1) {
                    mProgress = 0;
                    return;
                }
                mProgress += 10;
                loadingBtn.setProgress(mProgress);
                if (mProgress < 100) {
                    handler.postDelayed(this, random.nextInt(1000));
                } else {
                }
                if (mProgress > 80) {
                    loadingTimes++;
                    mProgress = 0;
                }
                if (loadingTimes > 8) {
                    hideLoading();
                }
            }
        }, random.nextInt(1000));
        isLoading = true;
    }

    public void hideLoading() {
        loadingTimes = 0;
        mProgress = -1;
        loadingBtn.setProgress(100);
        isLoading = false;
    }


    protected void validate() {
        //如果为空就先创建
        if (validator == null) {
            validator = new Validator(this);
            validator.setValidationListener(this);
        }
        validator.validate();
    }

    @Override
    public void onValidationSucceeded() {

    }

    /**
     * 默认的验证失败回调
     *
     * @param failedView The {@link android.view.View} that did not pass validation.
     * @param failedRule The failed {@link com.mobsandgeeks.saripaar.Rule} associated with the {@link android.view.View}.
     */
    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();

        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
        MobclickAgent.onResume(this);          //统计时长
        AVAnalytics.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName()); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
        AVAnalytics.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KJActivityManager.create().finishActivity(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        AVAnalytics.trackAppOpened(intent);
    }
}

