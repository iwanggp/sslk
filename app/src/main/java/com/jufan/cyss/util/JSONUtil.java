package com.jufan.cyss.util;

import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by cyjss on 2014/12/27.
 */
public class JSONUtil {
    public static void setText(TextView tv, String key, JSONObject json, String nullText, String afterStr) {
        if (json.has(key)) {
            try {
                tv.setText(json.get(key).toString() + afterStr);
            } catch (Exception ex) {
                tv.setText(nullText + afterStr);
            }
        } else {
            tv.setText(nullText + afterStr);
        }
    }

    public static void setText(TextView tv, String key, JSONObject json) {
        setText(tv, key, json, "无");
    }

    public static void setText(TextView tv, String key, JSONObject json, String nullText) {
        setText(tv, key, json, nullText, "");
    }

}

