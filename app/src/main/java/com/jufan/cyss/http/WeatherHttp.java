package com.jufan.cyss.http;

import android.util.Log;

import com.jufan.cyss.util.HttpUtil;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cyjss on 2015/3/13.
 */
public class WeatherHttp extends GroundyTask {
    private final String WEATHER_URL = "http://woklk.avosapps.com/api/weather";
    private final String LOG_MSG = "WeatherHttp";

    @Override
    protected TaskResult doInBackground() {
        JSONObject backJobj = new JSONObject();
        try {
            backJobj = HttpUtil.callRemoteService(WEATHER_URL, "", new JSONObject());
            if (backJobj.getInt("error_code") != 0) {
                return failed().add("code", "111000").add("desc", "请求错误，暂时无法访问天气服务器");
            }
        } catch (JSONException e) {
            Log.d(LOG_MSG, "", e);
            return failed().add("code", "111000").add("desc", "请求json参数格式错误");
        } catch (Exception e) {
            Log.d(LOG_MSG, "", e);
            return failed().add("code", "111000").add("desc", "网络异常，请稍后重试");
        }
        return succeeded().add("data", backJobj.toString());
    }
}
