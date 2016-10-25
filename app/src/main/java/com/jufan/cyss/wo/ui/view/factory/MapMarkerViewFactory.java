package com.jufan.cyss.wo.ui.view.factory;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.model.Marker;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.LikeUtil;
import com.jufan.cyss.wo.ui.Login;
import com.jufan.cyss.wo.ui.MainActivity;
import com.jufan.cyss.wo.ui.R;
import com.jufan.cyss.wo.ui.RoadMapFragment;
import com.jufan.cyss.wo.ui.RoadVideo;
import com.jufan.cyss.wo.ui.view.CommentDialog;
import com.jufan.cyss.wo.ui.view.ElectronicEyesListView;
import com.jufan.cyss.wo.ui.view.EmojiTextView;
import com.jufan.cyss.wo.ui.view.MarkerCommentListView;
import com.jufan.cyss.wo.ui.view.MarkerImageGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by cyjss on 2015/4/2.
 */
public class MapMarkerViewFactory {
    public static View createMarkerView(final Context context, final AVObject avObj, final Marker marker, final RoadMapFragment roadMapFragment, final MainActivity mainActivity) {
        AVUser user = avObj.getAVUser("user");
        View view = View.inflate(context, R.layout.map_marker_window, null);
        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView time = (TextView) view.findViewById(R.id.time);
        ImageView hideBtn = (ImageView) view.findViewById(R.id.hideBtn);
        EmojiTextView etv = (EmojiTextView) view.findViewById(R.id.msgContent);
        MarkerImageGridView imgGrid = (MarkerImageGridView) view.findViewById(R.id.imgGrid);
        MarkerCommentListView markerCommentListView = (MarkerCommentListView) view.findViewById(R.id.commentList);
        LinearLayout likeContainer = (LinearLayout) view.findViewById(R.id.likeContainer);
        LinearLayout commentContainer = (LinearLayout) view.findViewById(R.id.commentContainer);
        final TextView likeCount = (TextView) view.findViewById(R.id.likeCount);
        final TextView commentCount = (TextView) view.findViewById(R.id.commentCount);
        LikeUtil.countQuery(likeCount, avObj.getObjectId(), null);
        AVQuery query = new AVQuery<>("MapMarkerComment");
        query.whereEqualTo("attachObj", avObj);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    commentCount.setText("" + i);
                } else {
                    Log.e("", "", e);
                    GlobalUtil.showNetworkError();
                }
            }
        });
        imgGrid.setDataSource(avObj.getList("images"));

        etv.setEmojiText(avObj.getString("text"));
        username.setText(user.getUsername());
        if (avObj.getCreatedAt() != null) {
            time.setText(DateUtil.getShortTimeDesc(avObj.getCreatedAt()));
        } else {
            time.setText(DateUtil.getShortTimeDesc(new Date()));
        }
        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker.hideInfoWindow();
            }
        });
        commentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GlobalUtil.isLogin()) {
                    Intent i = new Intent(context, Login.class);
                    context.startActivity(i);
                    ViewInject.longToast("请先登录");
                    return;
                }
                CommentDialog dialog = new CommentDialog(context, avObj, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AVObject markerComment = (AVObject) v.getTag();
                        roadMapFragment.appendCommentToWindow(markerComment);
                    }
                });
                dialog.show();
            }
        });
        likeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikeUtil.addLike(v, likeCount, avObj.getObjectId(), 0, context);
            }
        });
        if (user.has("avatar")) {
            AVFile avatarFile = user.getAVFile("avatar");
            ImageLoader.getInstance().displayImage(avatarFile.getUrl(), avatar, HttpUtil.DefaultOptions);
        }
        markerCommentListView.setMarkerId(avObj);
        return view;
    }

    public static View createRoadView(final Context context, final JSONObject json, final Marker marker, final MainActivity mainActivity) {
        View view = View.inflate(context, R.layout.map_road_window, null);
        TextView roadName = (TextView) view.findViewById(R.id.roadName);
        ImageView hideBtn = (ImageView) view.findViewById(R.id.hideBtn);
        Button checkBtn = (Button) view.findViewById(R.id.checkBtn);
        Button favBtn = (Button) view.findViewById(R.id.favBtn);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker.hideInfoWindow();
            }
        });
        roadName.setText(marker.getTitle());

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, RoadVideo.class);
                i.putExtra("json", json.toString());
                context.startActivity(i);
            }
        });
        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalUtil.isLogin()) {
                    mainActivity.showLoading();
                    final AVUser user = AVUser.getCurrentUser();
                    AVQuery<AVObject> query = new AVQuery<AVObject>("FavoriteRoad");
                    query.whereEqualTo("userId", user.getObjectId());
                    try {
                        query.whereEqualTo("dir", json.getString("dir"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    query.getFirstInBackground(new GetCallback<AVObject>() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            if (avObject == null) {
                                AVObject favorite = new AVObject("FavoriteRoad");
                                try {
                                    favorite.put("dir", json.getString("dir"));
                                    favorite.put("userId", user.getObjectId());
                                    favorite.put("road", json.getString("road"));
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                favorite.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        mainActivity.hideLoading();
                                        if (e != null) {
                                            ViewInject.longToast("收藏失败");
                                        } else {
                                            ViewInject.longToast("收藏成功");
                                        }
                                    }
                                });
                            } else {
                                ViewInject.longToast("您已收藏该路段");
                                mainActivity.hideLoading();
                            }
                        }
                    });
                } else {
                    Simple simple = Simple.getByKey("fav_road");
                    JSONObject roads = null;
                    if (StringUtils.isEmpty(simple.value)) {
                        roads = new JSONObject();
                    } else {
                        try {
                            roads = new JSONObject(simple.value);
                        } catch (JSONException e) {
                            roads = new JSONObject();
                            e.printStackTrace();
                        }
                    }
                    try {
                        if (roads.has(json.getString("dir"))) {
                            ViewInject.longToast("您已收藏该路段");
                        } else {
                            roads.put(json.getString("dir"), json);
                            simple.value = roads.toString();
                            simple.save();
                            ViewInject.longToast("收藏成功");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return view;
    }

    public static View createEyesView(final Context context, final List<JSONObject> list, final Marker marker, final RoadMapFragment roadMapFragment, final MainActivity mainActivity) {
        View view = View.inflate(context, R.layout.map_eyes_window, null);
        ImageView hideBtn = (ImageView) view.findViewById(R.id.hideBtn);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker.hideInfoWindow();
            }
        });
        ElectronicEyesListView electronicEyesListView = (ElectronicEyesListView) view.findViewById(R.id.eyesList);
        electronicEyesListView.setDataSource(list, roadMapFragment);
        return view;
    }
}
