package com.jufan.cyss.wo.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.WeatherHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.BitmapUtil;
import com.jufan.cyss.util.DateUtil;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Date;

/**
 * Created by cyjss on 2015/3/12.
 */
public class Weather extends BaseUNIActivity {

    @BindView(id = R.id.todayImg)
    private ImageView todayImg;
    @BindView(id = R.id.city)
    private TextView city;
    @BindView(id = R.id.temperature)
    private TextView temperature;
    @BindView(id = R.id.wind)
    private TextView wind;
    @BindView(id = R.id.advice)
    private TextView advice;
    @BindView(id = R.id.uvIndex)
    private TextView uvIndex;
    @BindView(id = R.id.washIndex)
    private TextView washIndex;
    @BindView(id = R.id.travelIndex)
    private TextView travelIndex;
    @BindView(id = R.id.exerciseIndex)
    private TextView exerciseIndex;
    @BindView(id = R.id.futureDayGrid)
    private GridView futureDayGrid;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_weather);
    }

    @Override
    protected void initWidget() {
        setupActionBar("天气预报", ActionBarType.BACK);
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
        showLoading();
        Groundy.create(WeatherHttp.class).callback(new WeatherSync()).queueUsing(this);
    }

    private void showWeather(JSONObject obj) {
        try {
            JSONObject res = obj.getJSONObject("result");
            JSONObject today = res.getJSONObject("today");
            JSONArray future = res.getJSONArray("future");
            String fa = today.getString("fa");
            todayImg.setImageBitmap(BitmapUtil.getWeather(this, Integer.parseInt(fa)));
            String[] temp = today.getString("temperature").split("~");
            temperature.setText(temp[0] + "℃~" + temp[1] + "℃");
            wind.setText(today.getString("weather") + "," + today.getString("wind"));
            advice.setText(today.getString("dressing_index") + "," + today.getString("dressing_advice"));
            uvIndex.setText(today.getString("uv_index"));
            washIndex.setText(today.getString("wash_index"));
            travelIndex.setText(today.getString("travel_index"));
            exerciseIndex.setText(today.getString("exercise_index"));
            futureDayGrid.setAdapter(new WeatherFutureAdapter(future));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class WeatherSync {
        @OnSuccess(WeatherHttp.class)
        public void onSuccess(@Param("data") String data) {
            hideLoading();
            try {
                JSONObject json = new JSONObject(data);
                json.put("update_time", DateUtil.detailDateStr(new Date()));
                showWeather(json);
                Simple s = Simple.getByKey("weather_sync");
                s.value = json.toString();
                s.save();
            } catch (JSONException e) {
            }
        }

        @OnFailure(WeatherHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            hideLoading();
            ViewInject.longToast(desc);
        }
    }

    private class WeatherFutureAdapter extends BaseAdapter {

        private JSONArray dataSource;

        public WeatherFutureAdapter(JSONArray array) {
            this.dataSource = array;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            try {
                return dataSource.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject obj = (JSONObject) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(Weather.this, R.layout.grid_weather_item, null);
            }
            TextView futureDay = (TextView) convertView.findViewById(R.id.futureDay);
            TextView weather = (TextView) convertView.findViewById(R.id.weather);
            ImageView weatherImg = (ImageView) convertView.findViewById(R.id.weatherImg);
            TextView temperature = (TextView) convertView.findViewById(R.id.temperature);
            Date now = new Date();
            now.setDate(now.getDate() + 1 + position);
            futureDay.setText(DateUtil.getWeekOfDate(now));
            try {
                weather.setText(obj.getString("weather"));
                String fa = obj.getString("fa");
                weatherImg.setImageBitmap(BitmapUtil.getWeather(Weather.this, Integer.parseInt(fa)));
                String temperatureStr = obj.getString("temperature");
                String[] temperatureArr = temperatureStr.split("~");
                temperature.setText(temperatureArr[0] + "℃~" + temperatureArr[1] + "℃");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }
}
