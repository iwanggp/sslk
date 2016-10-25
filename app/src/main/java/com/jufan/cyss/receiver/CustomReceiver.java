package com.jufan.cyss.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.util.DateUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.KJActivityManager;

import java.util.Date;

/**
 * Created by cyjss on 2015/3/16.
 */
public class CustomReceiver extends BroadcastReceiver {
    private static final String LOG_MSG = "CustomReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            Log.d(LOG_MSG, action);
            if (action.equals("com.jufan.cyss.wo.ui.action")) {
                Log.d(LOG_MSG, "" + intent.getExtras().getString("com.avos.avoscloud.Data"));
                JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
                BaseUNIActivity activity = (BaseUNIActivity) KJActivityManager.create().topActivity();

                if (json.has("_activity_")) {
                    String activityStr = json.getString("_activity_");
                    if (activityStr.equals(activity.getClass().getSimpleName())) {
                        activity.executeReceiver(json);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_MSG, "", e);
        } catch (Exception e) {
            Log.e(LOG_MSG, "", e);
        }
    }


}
