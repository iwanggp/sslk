package com.jufan.cyss.http;

import android.util.Log;

import com.jufan.cyss.util.HttpUtil;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cyjss on 2015/4/3.
 */
public class SimpleHttp extends GroundyTask {
    private static final String LOG_MSG = "SimpleHttp";

    @Override
    protected TaskResult doInBackground() {
        String url = getStringArg("_url");
        String tag = getStringArg("_tag", "");
        String type = getStringArg("_type");
        if (StringUtils.isEmpty(type)) {
            type = "post";
        }
        String res = "";
        try {
            Map<String, String> params = new HashMap<String, String>();
            for (String key : getArgs().keySet()) {
                if (key.equals("_url") || key.equals("_tag") || key.equals("_type")) {
                    continue;
                }
                params.put(key, getStringArg(key));
            }
            if ("post".equals(type)) {
                res = HttpUtil.doPost(url, params);
            } else if ("get".equals(type)) {
                res = HttpUtil.doGet(url, params);
            }
        } catch (Exception e) {
            Log.d(LOG_MSG, "", e);
            return failed().add("code", "111000").add("desc", "网络异常，请稍后重试").add("tag", tag);
        }
        Log.d(LOG_MSG, "==>" + res);
        return succeeded().add("data", res).add("tag", tag);
    }
}
