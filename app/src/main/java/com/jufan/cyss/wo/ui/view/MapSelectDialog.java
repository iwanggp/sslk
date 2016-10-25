package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.Login;
import com.jufan.cyss.wo.ui.R;
import com.jufan.cyss.wo.ui.RoadMapFragment;
import com.nineoldandroids.animation.Animator;

import org.kymjs.aframe.ui.ViewInject;

/**
 * Created by cyjss on 2015/1/31.
 */
public class MapSelectDialog extends Dialog implements View.OnClickListener {

    private Button selectBtn;

    private Button lkBtn;
    private Button sgBtn;
    private Button xqBtn;
    private Button fdBtn;
    private RoadMapFragment roadMapFragment;
    private TrafficDialog td;

    private final int duration = 300;

    public MapSelectDialog(Context context, Button selectBtn, RoadMapFragment roadMapFragment) {
        super(context, R.style.selectDialog);
        setContentView(R.layout.dialog_map_select);
        this.selectBtn = selectBtn;
        this.roadMapFragment = roadMapFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.lkBtn = (Button) findViewById(R.id.lkBtn);
        this.sgBtn = (Button) findViewById(R.id.sgBtn);
        this.xqBtn = (Button) findViewById(R.id.xqBtn);
        this.fdBtn = (Button) findViewById(R.id.fdBtn);
        this.lkBtn.setOnClickListener(this);
        this.sgBtn.setOnClickListener(this);
        this.xqBtn.setOnClickListener(this);
        this.fdBtn.setOnClickListener(this);

        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = window.getWindowManager().getDefaultDisplay().getWidth() / 2 - GlobalUtil.dip2px(getContext(), 42);
        lp.y = window.getWindowManager().getDefaultDisplay().getHeight() / 2 - GlobalUtil.dip2px(getContext(), 260);
        Log.d("MapSelectDialog", "===>" + lp.width + "," + lp.height + "," + lp.x + "," + lp.y + "," + window.getWindowManager().getDefaultDisplay().getWidth() + "," + window.getWindowManager().getDefaultDisplay().getHeight());
        window.setAttributes(lp);
    }

    @Override
    public void dismiss() {
        RotateAnimation rotateAnimation = new RotateAnimation(45, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(duration);
        selectBtn.startAnimation(rotateAnimation);

        YoYo.with(Techniques.SlideOutDown).duration(duration).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                MapSelectDialog.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(lkBtn);
        YoYo.with(Techniques.SlideOutDown).duration(duration).playOn(sgBtn);
        YoYo.with(Techniques.SlideOutDown).duration(duration).playOn(xqBtn);
        YoYo.with(Techniques.SlideOutDown).duration(duration).playOn(fdBtn);
    }

    @Override
    public void show() {
        super.show();
        YoYo.with(Techniques.SlideInUp).duration(duration).playOn(lkBtn);
        YoYo.with(Techniques.SlideInUp).duration(duration).playOn(sgBtn);
        YoYo.with(Techniques.SlideInUp).duration(duration).playOn(xqBtn);
        YoYo.with(Techniques.SlideInUp).duration(duration).playOn(fdBtn);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (GlobalUtil.isLogin()) {
            td = new TrafficDialog(getContext(), Integer.parseInt(v.getTag().toString()), roadMapFragment);
            td.show();
        } else {
            Intent i = new Intent(getContext(), Login.class);
            getContext().startActivity(i);
            ViewInject.longToast("请先登录");
        }
    }

    public TrafficDialog getTrafficDialog() {
        return td;
    }
}
