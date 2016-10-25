package com.jufan.cyss.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.UMPushHttp;
import com.jufan.cyss.wo.ui.MainActivity;
import com.jufan.cyss.wo.ui.R;
import com.umeng.message.UTrack;
import com.umeng.message.UmengBaseIntentService;
import com.umeng.message.UmengRegistrar;
import com.umeng.message.entity.UMessage;

import org.android.agoo.client.BaseConstants;
import org.json.JSONObject;
import org.kymjs.aframe.ui.KJActivityManager;

import java.net.URLDecoder;
import java.util.Iterator;

/**
 * Created by cyjss on 2015/3/17.
 */
public class CustomPushService extends UmengBaseIntentService {
    private static final String LOG_TAG = CustomPushService.class.getName();

// 如果需要打开Activity，请调用Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)；否则无法打开Activity。

    @Override
    protected void onMessage(Context context, Intent intent) {
        super.onMessage(context, intent);
        try {
            String message = intent.getStringExtra(BaseConstants.MESSAGE_BODY);
            UMessage msg = new UMessage(new JSONObject(message));
            UTrack.getInstance(context).trackMsgClick(msg);

            JSONObject customObj = new JSONObject(URLDecoder.decode(msg.custom, "utf-8"));
            Log.d(LOG_TAG, "" + customObj.toString());
            String from = customObj.getString("_from_");
            int action = customObj.getInt("_action_");
            String activityStr = customObj.getString("_activity_");
            Log.d(LOG_TAG, "" + activityStr);
            BaseUNIActivity activity = (BaseUNIActivity) KJActivityManager.create().topActivity();
            if (action == UMPushHttp.EXECUTE_IN_TOP_ACTIVITY) {
                if (activityStr.equals(activity.getClass().getSimpleName())) {
                    activity.executeReceiver(customObj);
                } else {
                    activity.addPushJson(customObj);
                }
            } else if (action == UMPushHttp.SHOW_IN_NOTIFICATION) {
                String deviceToken = UmengRegistrar.getRegistrationId(context);
                if (from.equals(deviceToken)) {
                    return;
                }
                String text = customObj.getString("text");

                Intent resultIntent = new Intent(getBaseContext(), Class.forName(getPackageName() + "." + activityStr));
                if(customObj.has("_args_")) {
                    JSONObject args = customObj.getJSONObject("_args_");
                    Iterator<String> keys = args.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        resultIntent.putExtra(key, args.getString(key));
                    }
                }
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(this, 0, resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(getResources().getString(R.string.app_name))
                                .setContentText(text)
                                .setTicker(text);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setAutoCancel(true);
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);

                int mNotificationId = 10010;
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
    }
}
