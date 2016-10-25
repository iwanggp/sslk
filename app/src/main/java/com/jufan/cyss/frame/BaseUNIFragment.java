package com.jufan.cyss.frame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.AVAnalytics;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;
import org.kymjs.aframe.ui.AnnotateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyjss on 2014/12/21.
 */
public abstract class BaseUNIFragment extends Fragment implements View.OnClickListener {

    protected BaseUNIApplication application;
    private final String LOG_MSG = "BaseUNIFragment";
    protected List<JSONObject> pushList = new ArrayList<JSONObject>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflaterView(inflater, container, savedInstanceState);
        application = (BaseUNIApplication) getActivity().getApplication();
        AnnotateUtil.initBindView(this, view);
        initWidget(view);
        widgetResume();
        return view;
    }

    protected View inflaterView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return null;
    }

    public abstract void widgetResume();

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_MSG, "onStart=========>" + getPageName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getPageName()); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        Log.d(LOG_MSG, "onPause=========>" + getPageName());
        AVAnalytics.onFragmentEnd(getPageName());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getPageName()); //统计页面
        Log.d(LOG_MSG, "onResume=========>" + getPageName());
        AVAnalytics.onFragmentStart(getPageName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_MSG, "onDestroy=========>" + getPageName());
    }

    protected void widgetClick(View v) {
    }

    public void executeReceiver(JSONObject json) {
    }

    public void addPushJson(JSONObject obj) {
        synchronized (pushList) {
            this.pushList.add(obj);
        }
    }

    @Override
    public void onClick(View v) {
        widgetClick(v);
    }

    /**
     * 获取页面名，可在子类中重写，默认返回类名
     *
     * @return
     */
    public void initWidget(View parentView) {
    }

    protected String getPageName() {
        return this.getClass().getSimpleName();
    }

}
