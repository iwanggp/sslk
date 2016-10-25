package com.jufan.cyss.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.jufan.cyss.bean.User;

import java.util.Date;

/**
 * Created by cyjss on 2014/12/23.
 */
public class SimpleStorageUtil {

    private static final String SIMPLE_STORAGE_NAME = "SSN";

    public static final String CURRENT_USERNAME_TAG = "username";
    public static final String CURRENT_PASSWORD_TAG = "password";
    public static final String CURRENT_XM_TG = "xm";
    public static final String CURRENT_AVATAR_TG = "avatar";
    public static final String CURRENT_LAST_LOGIN_TAG = "last_login_tag";

    public static void saveKeyValue(Context ctx, String[] keys, String[] values) {
        SharedPreferences spf = ctx.getSharedPreferences(SIMPLE_STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        for (int i = 0; i < keys.length; i++) {
            editor.putString(keys[i], values[i]);
        }
        editor.commit();
    }

    public static String getValue(Context ctx, String key) {
        String val = null;
        SharedPreferences spf = ctx.getSharedPreferences(SIMPLE_STORAGE_NAME, Context.MODE_PRIVATE);
        val = spf.getString(key, null);
        return val;
    }

    public static void removeCurrentUser(Context ctx) {
        SharedPreferences spf = ctx.getSharedPreferences(SIMPLE_STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.remove(CURRENT_USERNAME_TAG);
        editor.remove(CURRENT_PASSWORD_TAG);
        editor.remove(CURRENT_LAST_LOGIN_TAG);
        editor.remove(CURRENT_AVATAR_TG);
        editor.remove(CURRENT_XM_TG);
        editor.commit();
    }

    public static void saveUserInfo(Context ctx, String username, String xm, String avatar, String pwd) {
        saveKeyValue(ctx, new String[]{
                CURRENT_USERNAME_TAG, CURRENT_XM_TG, CURRENT_PASSWORD_TAG, CURRENT_AVATAR_TG, CURRENT_LAST_LOGIN_TAG
        }, new String[]{
                username, xm, pwd, avatar, DateUtil.detailDateStr(new Date())
        });
    }

    public static User getCurrentUserInfo(Context ctx) {
        User u = new User();
        u.setUsername(getValue(ctx, CURRENT_USERNAME_TAG));
        u.setXm(getValue(ctx, CURRENT_XM_TG));
        u.setPassword(getValue(ctx, CURRENT_PASSWORD_TAG));
        u.setAvatar(getValue(ctx, CURRENT_AVATAR_TG));
        u.setLastLoginTime(DateUtil.detailFormat(getValue(ctx, CURRENT_LAST_LOGIN_TAG)));
        return u;
    }

}
