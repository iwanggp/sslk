package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.http.JtgzfwHttp;
import com.jufan.cyss.http.WeatherHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.BitmapUtil;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.view.AroundDialog;
import com.jufan.cyss.wo.ui.view.DataSelectDialog;
import com.jufan.cyss.wo.ui.view.ShareDialog;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Date;

/**
 * Created by cyjss on 2015/1/29.
 */
public class MoreFragment extends BaseUNIFragment {

    @BindView(id = R.id.feedback, click = true)
    private RelativeLayout feedback;
    @BindView(id = R.id.aboutUs, click = true)
    private RelativeLayout aboutUs;
    @BindView(id = R.id.aroundBuilding, click = true)
    private RelativeLayout aroundBuilding;
    @BindView(id = R.id.update, click = true)
    private RelativeLayout update;
    @BindView(id = R.id.weatherContainer, click = true)
    private RelativeLayout weatherContainer;
    @BindView(id = R.id.share, click = true)
    private RelativeLayout sharerContainer;
    @BindView(id = R.id.weatherThumb)
    private ImageView weatherThumb;
    @BindView(id = R.id.weatherLoading)
    private ProgressBar weatherLoading;

    @Override
    protected View inflaterView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_more, null);

    }

    @Override
    public void widgetResume() {
        application.getMainActivity().setupActionBar("更多");
        Simple s = Simple.getByKey("weather_sync");
        if (StringUtils.isEmpty(s.value)) {
            syncWeather();
        } else {
            try {
                JSONObject weatherObj = new JSONObject(s.value);
                Date updateDate = DateUtil.detailFormat(weatherObj.getString("update_time"));
                if (System.currentTimeMillis() - updateDate.getTime() > 1000 * 60 * 10) {
                    syncWeather();
                } else {
                    showWeather(weatherObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                syncWeather();
            }
        }
    }

    private void syncWeather() {
        showWeatherLoading();
        Groundy.create(WeatherHttp.class).callback(new WeatherSync()).queueUsing(getActivity());
    }

    private void showWeather(JSONObject obj) {
        try {
            hideWeatherLoading();
            JSONObject res = obj.getJSONObject("result");
            JSONObject today = res.getJSONObject("today");
            String fa = today.getString("fa");
            weatherThumb.setImageBitmap(BitmapUtil.getWeather(getActivity(), Integer.parseInt(fa)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showWeatherLoading() {
        weatherLoading.setVisibility(View.VISIBLE);
        weatherThumb.setVisibility(View.GONE);
    }

    private void hideWeatherLoading() {
        weatherLoading.setVisibility(View.GONE);
        weatherThumb.setVisibility(View.VISIBLE);
    }

    private class WeatherSync {
        @OnSuccess(WeatherHttp.class)
        public void onSuccess(@Param("data") String data) {
            hideWeatherLoading();
            try {
                JSONObject json = new JSONObject(data);
                json.put("update_time", DateUtil.detailDateStr(new Date()));
                showWeather(json);
                Simple s = Simple.getByKey("weather_sync");
                s.value = json.toString();
                s.save();
            } catch (JSONException e) {
                weatherThumb.setVisibility(View.GONE);
            }
        }

        @OnFailure(WeatherHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            weatherLoading.setVisibility(View.GONE);
            weatherThumb.setVisibility(View.GONE);
            ViewInject.longToast(desc);
        }
    }

    @Override
    public void initWidget(View parentView) {

    }

    @Override
    public void widgetClick(View v) {

        switch (v.getId()) {
            case R.id.feedback:
                Intent feedbackIntent = new Intent(getActivity(), FeedBack.class);
                startActivity(feedbackIntent);
                break;
            case R.id.profileContainer:
                break;
            case R.id.aboutUs:
                break;
            case R.id.aroundBuilding:
                AroundDialog aroundDialog = new AroundDialog(getActivity());
                aroundDialog.show();
                break;

            case R.id.update:
                application.getMainActivity().showLoading();
                UmengUpdateAgent.setUpdateOnlyWifi(false);
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        switch (updateStatus) {
                            case UpdateStatus.Yes: // has update
                                UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                                break;
                            case UpdateStatus.No: // has no update
                                Toast.makeText(getActivity(), "已经是最新版本", Toast.LENGTH_SHORT).show();
                                break;
                            case UpdateStatus.NoneWifi: // none wifi
                                Toast.makeText(getActivity(), "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
                                break;
                            case UpdateStatus.Timeout: // time out
                                Toast.makeText(getActivity(), "连接超时，请重新再试", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        application.getMainActivity().hideLoading();
                    }
                });
                UmengUpdateAgent.update(getActivity());
                break;
            case R.id.weatherContainer:
                Intent weatherIntent = new Intent(getActivity(), Weather.class);
                startActivity(weatherIntent);
                break;
            case R.id.share:
                ShareDialog shareDialog = new ShareDialog(getActivity());
                shareDialog.show();
                break;
        }
    }
}
