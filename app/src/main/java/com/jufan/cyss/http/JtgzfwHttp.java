package com.jufan.cyss.http;

import android.util.Log;

import com.avos.avoscloud.AVUser;
import com.jufan.cyss.util.HttpUtil;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.utils.StringUtils;

/**
 * Created by cyjss on 2015/2/2.
 */
public class JtgzfwHttp extends GroundyTask {

    private static final String LOG_MSG = "JtgzfwHttp";

    @Override
    protected TaskResult doInBackground() {
        String code = getStringArg("code");
        String json = getStringArg("json");
        String tag = getStringArg("tag", "");
        String url = getStringArg("_url", "");
        JSONObject backJobj = null;
        JSONObject backHead = null;
        try {
            AVUser user = AVUser.getCurrentUser();
            JSONObject jobj = new JSONObject(json);
            if (StringUtils.isEmpty(url)) {
                backJobj = HttpUtil.callRemoteService(code, jobj);
            } else {
                Log.d(LOG_MSG, "＝＝》" + url);
                backJobj = HttpUtil.callRemoteService(url, code, jobj);
            }
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
