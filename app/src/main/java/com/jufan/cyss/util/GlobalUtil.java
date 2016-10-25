package com.jufan.cyss.util;

import android.content.Context;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.ViewInject;

/**
 * Created by cyjss on 2014/12/30.
 */
public class GlobalUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void setString(TextView tv, JSONObject json, String key) {
        String val = "";
        try {
            val = json.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tv.setText(val);
    }

    public static boolean isLogin() {
        AVUser user = AVUser.getCurrentUser();
        return user != null;
    }

    public static void showNetworkError() {
        ViewInject.longToast("网络异常，请检查网络连接");
    }
}
