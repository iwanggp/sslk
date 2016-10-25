package com.jufan.cyss.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.wo.ui.Login;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.ViewInject;

/**
 * Created by cyjss on 2015/4/2.
 */

/**
 * type:
 * 0: 对地图marker的赞
 * 1: 对地图marker评论的赞
 * 2: 对社区论坛话题的赞
 * 3: 对话题评论的赞
 * 4: 对电子眼吐槽的赞
 */

public class LikeUtil {
    private final static String TABLE_NAME = "LikeRecord";

    public static void countQuery(final TextView tv, String attachId, final Object data) {
        if (data != null) {
            if (data instanceof JSONObject) {
                JSONObject json = (JSONObject) data;
                if (json.has("like_count")) {
                    try {
                        tv.setText(json.getString("like_count"));
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (data instanceof AVObject) {
                AVObject avObject = (AVObject) data;
                if (avObject.has("like_count")) {
                    tv.setText(avObject.getString("like_count"));
                    return;
                }
            }
        }
        AVQuery query = new AVQuery<AVObject>(TABLE_NAME);
        query.whereEqualTo("attachId", attachId);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                tv.setText("" + i);
                if (data instanceof JSONObject) {
                    JSONObject json = (JSONObject) data;
                    try {
                        json.put("like_count", "" + i);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                } else if (data instanceof AVObject) {
                    AVObject avObject = (AVObject) data;
                    avObject.put("like_count", "" + i);
                }
            }
        });
    }

    public static void addLike(final View icon, final TextView tv, final String attachId, final int type, Context ctx) {
        if (GlobalUtil.isLogin()) {
            icon.setEnabled(false);
            AVQuery<AVObject> query = new AVQuery<AVObject>(TABLE_NAME);
            query.whereEqualTo("attachId", attachId);
            query.whereEqualTo("user", AVUser.getCurrentUser());
            query.whereEqualTo("type", type);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int i, AVException e) {
                    icon.setEnabled(true);
                    if (e == null) {
                        if (i == 0) {
                            AVObject like = new AVObject(TABLE_NAME);
                            like.put("attachId", attachId);
                            like.put("user", AVUser.getCurrentUser());
                            like.put("type", type);
                            like.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        int count = Integer.parseInt(tv.getText().toString()) + 1;
                                        tv.setText("" + count);
                                        tv.setTag("" + count);
                                    } else {
                                        GlobalUtil.showNetworkError();
                                    }
                                }
                            });
                        } else {
                            ViewInject.longToast("您已赞过该评论");
                        }
                    } else {
                        GlobalUtil.showNetworkError();
                    }
                }
            });
        } else {
            Intent intent = new Intent(ctx, Login.class);
            ctx.startActivity(intent);
        }
    }
}
