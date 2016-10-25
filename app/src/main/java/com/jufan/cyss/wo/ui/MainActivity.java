package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.service.CustomPushService;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends BaseUNIActivity {

    public static final int REQUEST_TRAFFIC_LOAD_IMAGE = 201;
    public static final int REQUEST_TRAFFIC_CAPTURE_IMAGE = 301;
    public static final int REQUEST_COMMUNITY_ADD_POST = 400;

    @BindView(id = R.id.tab_widget_item1, click = true)
    private LinearLayout mapRb;
    @BindView(id = R.id.tab_widget_item1_img)
    private ImageView mapImg;
    @BindView(id = R.id.tab_widget_item1_tv)
    private TextView mapTv;

    @BindView(id = R.id.tab_widget_item2, click = true)
    private LinearLayout communityRb;
    @BindView(id = R.id.tab_widget_item2_img)
    private ImageView communityImg;
    @BindView(id = R.id.tab_widget_item2_tv)
    private TextView communityTv;

    @BindView(id = R.id.tab_widget_item3, click = true)
    private LinearLayout meRb;
    @BindView(id = R.id.tab_widget_item3_img)
    private ImageView meImg;
    @BindView(id = R.id.tab_widget_item3_tv)
    private TextView meTv;

    @BindView(id = R.id.tab_widget_item4, click = true)
    private LinearLayout moreRb;
    @BindView(id = R.id.tab_widget_item4_img)
    private ImageView moreImg;
    @BindView(id = R.id.tab_widget_item4_tv)
    private TextView moreTv;

    private int nowIndex = -1;
    private TabBean[] tabs = null;
    private int exitTimes = 0;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initWidget() {
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
        String deviceToken = UmengRegistrar.getRegistrationId(this);
        UmengUpdateAgent.update(this);
        Log.d("Logo", "device token ====>" + deviceToken);
        mPushAgent.setPushIntentServiceClass(CustomPushService.class);
        AVUser user = AVUser.getCurrentUser();
        if (user != null && !StringUtils.isEmpty(deviceToken)) {
            user.put("deviceToken", deviceToken);
            user.saveInBackground();
        }

        // 保存 installation 到服务器
//        PushService.setDefaultPushCallback(this, MainActivity.class);
//        PushService.subscribe(this, "public", MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class);
        AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    String installId = AVInstallation.getCurrentInstallation().getInstallationId();
                    Log.d("success installId:", installId);
                } else {
                    AVInstallation.getCurrentInstallation().saveInBackground();
                }
            }
        });
        application.setMainActivity(this);
        setupActionBar("欢迎使用");
        TabBean tab1 = new TabBean();
        tab1.iv = mapImg;
        tab1.tv = mapTv;
        tab1.fragment = new RoadMapFragment();
        tab1.normalImg = R.drawable.ic_map_camera;
        tab1.checkedImg = R.drawable.ic_map_camera_checked;
        TabBean tab2 = new TabBean();
        tab2.iv = communityImg;
        tab2.tv = communityTv;
        tab2.fragment = new CommunityFragment();
        tab2.normalImg = R.drawable.ic_community;
        tab2.checkedImg = R.drawable.ic_community_checked;
        TabBean tab3 = new TabBean();
        tab3.iv = meImg;
        tab3.tv = meTv;
        tab3.fragment = new MeFragment();
        tab3.normalImg = R.drawable.ic_personal;
        tab3.checkedImg = R.drawable.ic_personal_checked;
        TabBean tab4 = new TabBean();
        tab4.iv = moreImg;
        tab4.tv = moreTv;
        tab4.fragment = new MoreFragment();
        tab4.normalImg = R.drawable.ic_more;
        tab4.checkedImg = R.drawable.ic_more_checked;
        tabs = new TabBean[]{tab1, tab2, tab3, tab4};
        changeFragment(0);
    }

    public void changeFragment(int index) {
        if (index >= 0 && index < tabs.length) {
            if (index == nowIndex) {
                return;
            }
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            if (nowIndex != -1) {
                TabBean nowTb = tabs[nowIndex];
                nowTb.iv.setImageResource(nowTb.normalImg);
                nowTb.tv.setTextColor(0xff808080);
                transaction.hide(nowTb.fragment);
            }
            TabBean tb = tabs[index];
            tb.iv.setImageResource(tb.checkedImg);
            tb.tv.setTextColor(0xffff6633);

            if (!tb.isAddFlag) {
                tb.isAddFlag = true;
                transaction.add(R.id.mainContainer, tb.fragment, tb.fragment.getClass().getSimpleName());
            }
            transaction.show(tb.fragment);
            transaction.commit();
            manager.executePendingTransactions();
            try {
                tb.fragment.widgetResume();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            nowIndex = index;
        }
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.tab_widget_item1:
                changeFragment(0);
                break;
            case R.id.tab_widget_item2:
                changeFragment(1);
                break;
            case R.id.tab_widget_item3:
                changeFragment(2);
                break;
            case R.id.tab_widget_item4:
                changeFragment(3);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (exitTimes == 0) {
                ViewInject.longToast("再按一下退出程序");
                exitTimes++;
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        exitTimes = 0;
                        timer.cancel();
                    }
                }, 2000);
                return false;
            } else {
                return super.onKeyDown(keyCode, e);
            }
        }
        return super.onKeyDown(keyCode, e);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            tabs[nowIndex].fragment.widgetResume();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getPackageName(), "resultCode===>" + resultCode);
        Log.d(getPackageName(), "requestCode===>" + requestCode);
        if (resultCode == RESULT_OK) {
            if (REQUEST_TRAFFIC_LOAD_IMAGE == requestCode) {
                tabs[0].fragment.onActivityResult(requestCode, resultCode, data);
            } else if (REQUEST_TRAFFIC_CAPTURE_IMAGE == requestCode) {
                tabs[0].fragment.onActivityResult(requestCode, resultCode, data);
            } else if (REQUEST_COMMUNITY_ADD_POST == requestCode) {
                tabs[1].fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void executeReceiver(JSONObject json) {
        try {
            if (json.has("_fragment_")) {
                String fragmentStr = json.getString("_fragment_");
                for (TabBean tb : tabs) {
                    if (tb.fragment.getClass().getSimpleName().equals(fragmentStr)) {
                        tb.fragment.executeReceiver(json);
                        break;
                    }
                }
            } else {
                tabs[nowIndex].fragment.executeReceiver(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addPushJson(JSONObject json) {
        try {
            if (json.has("_fragment_")) {
                String fragmentStr = json.getString("_fragment_");
                for (TabBean tb : tabs) {
                    if (tb.fragment.getClass().getSimpleName().equals(fragmentStr)) {
                        tb.fragment.addPushJson(json);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TabBean {
        public ImageView iv;
        public TextView tv;
        public BaseUNIFragment fragment;
        public int normalImg;
        public int checkedImg;
        public boolean isAddFlag = false;
    }
}
