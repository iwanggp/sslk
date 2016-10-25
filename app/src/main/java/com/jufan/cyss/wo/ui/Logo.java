package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;

import org.kymjs.aframe.ui.BindView;

/**
 * Created by cyjss on 2015/1/28.
 */
public class Logo extends BaseUNIActivity {

    @BindView(id = R.id.logo)
    private ImageView logo;
    @BindView(id = R.id.logoTip)
    private ImageView logoTip;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_logo);
    }

    @Override
    protected void initWidget() {

        YoYo.with(Techniques.FadeInUp).duration(800).playOn(logo);
        YoYo.with(Techniques.FadeInDown).duration(800).playOn(logoTip);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.FadeOutUp).duration(900).playOn(logo);
                YoYo.with(Techniques.FadeOutDown).duration(900).playOn(logoTip);
            }
        }, 2200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Logo.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 2900);
        ImageLoader.getInstance().clearDiskCache();
    }
}
