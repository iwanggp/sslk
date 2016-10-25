package com.jufan.cyss.http;

import android.util.Log;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.service.CustomPushService;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.MD5;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;
import com.umeng.message.UmengRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cyjss on 2015/3/17.
 */
public class UMPushHttp extends GroundyTask {

    private final static String URL = "http://125.46.83.214/sslk/video/api/push";
    public static final String REQ_LOGIN = "http://125.46.83.214/sslk/video/login/app";
    public static final int EXECUTE_IN_TOP_ACTIVITY = 0;
    public static final int SHOW_IN_NOTIFICATION = 1;

    @Override
    protected TaskResult doInBackground() {
        String json = getStringArg("json");
        String type = getStringArg("type");
        String deviceToken = UmengRegistrar.getRegistrationId(getContext());
        JSONObject backJobj = null;
        try {
            JSONObject sendObj = new JSONObject(json);
            sendObj.put("_from_", deviceToken);
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", type);
            Log.d("UMPushHttp", sendObj.toString());

            json = URLEncoder.encode(sendObj.toString(), "utf-8");
            map.put("json", json);
            if ("unicast".equals(type) || "listcast".equals(type)) {
                map.put("device_tokens", URLEncoder.encode(getStringArg("device_tokens", ""), "utf-8"));
            }
            String res = HttpUtil.doPost(URL, map);
            if (res == null) {
                return failed().add("code", "111000").add("desc", "网络异常，请稍后重试");
            } else {
                backJobj = new JSONObject(res);
                if (backJobj.has("ret") && "SUCCESS".equals(backJobj.getString("ret"))) {
                } else {
                    JSONObject data = backJobj.getJSONObject("data");
                    return failed().add("code", data.getString("error_code")).add("desc", "返回参数格式错误");
                }
            }
        } catch (JSONException e) {
            Log.e("UMPushHttp", "", e);
            return failed().add("code", "111000").add("desc", "返回json参数格式错误");
        } catch (Exception e) {
            Log.e("UMPushHttp", "", e);
            return failed().add("code", "111000").add("desc", "网络异常，请稍后重试");
        }
        Log.d("UMPushHttp", "推送成功");
        return succeeded().add("data", backJobj.toString());
    }

    public static JSONObject getCustomObj(String activity, String fragment) {
        return getCustomObj(activity, fragment, EXECUTE_IN_TOP_ACTIVITY, null);
    }

    public static JSONObject getCustomObj(BaseUNIActivity activity, BaseUNIFragment fragment) {
        return getCustomObj(activity.getClass().getSimpleName(), fragment.getClass().getSimpleName(), EXECUTE_IN_TOP_ACTIVITY, null);
    }

    public static JSONObject getCustomObj(String activity, String fragment, int action) {
        return getCustomObj(activity, fragment, action, null);
    }

    public static JSONObject getCustomObj(String activity, String fragment, int action, Map<String, String> args) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("_activity_", activity);
            obj.put("_action_", action);
            if (fragment != null) {
                obj.put("_fragment_", fragment);
            }
            if (args != null) {
                JSONObject argsObj = new JSONObject();
                for (String key : args.keySet()) {
                    argsObj.put(key, args.get(key));
                }
                obj.put("_args_", argsObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
