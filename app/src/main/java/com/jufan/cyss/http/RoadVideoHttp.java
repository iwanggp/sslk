package com.jufan.cyss.http;

import android.util.Log;

import com.jufan.cyss.util.HttpUtil;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cyjss on 2015/3/2.
 */
public class RoadVideoHttp extends GroundyTask {
    private static final String LOG_MSG = "RoadVideoHttp";
    public static final String REQ_VIDEO_SEARCH_URL = "http://222.137.116.89/sslk/video/interface";
    public static final String REQ_GET_ALL_VIDEO_URL = "http://222.137.116.89/sslk/video/all";

    @Override
    protected TaskResult doInBackground() {
        String url = getStringArg("url");
        String json = getStringArg("json");
        String tag = getStringArg("tag", "");
        JSONObject backJobj = null;
        JSONObject backHead = null;
        try {
            JSONObject jobj = new JSONObject(json);
            backJobj = HttpUtil.callRemoteService(url, "", jobj);
            backHead = backJobj.getJSONObject("head");
            String resCode = backHead.getString("response_code");
            String resDesc = backHead.getString("response_desc");
            if (!"000000".equals(resCode)) {
                return failed().add("code", resCode).add("desc", resDesc).add("tag", tag);
            }
        } catch (JSONException e) {
            Log.d(LOG_MSG, "", e);
            return failed().add("code", "111000").add("desc", "请求json参数格式错误").add("tag", tag);
        } catch (Exception e) {
            Log.d(LOG_MSG, "", e);
            return failed().add("code", "111000").add("desc", "网络异常，请稍后重试").add("tag", tag);
        }
        return succeeded().add("data", backJobj.toString()).add("tag", tag);
    }
}
