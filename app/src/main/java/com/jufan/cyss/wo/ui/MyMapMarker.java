package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.GetCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.frame.BaseUNIApplication;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.wo.ui.view.CommentDialog;
import com.jufan.cyss.wo.ui.view.EmojiTextView;
import com.jufan.cyss.wo.ui.view.MarkerCommentListView;
import com.jufan.cyss.wo.ui.view.MarkerImageGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.util.Date;

/**
 * Created by cyjss on 2015/3/19.
 */
public class MyMapMarker extends BaseUNIActivity implements AMap.InfoWindowAdapter, AMap.OnMarkerClickListener {

    @BindView(id = R.id.map)
    private MapView mapView;
    private AMap aMap;

    private String objectId;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_my_map_marker);
    }

    @Override
    protected void initWidget() {
        setupActionBar("我的话题", ActionBarType.BACK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setInfoWindowAdapter(this);
            aMap.setOnMarkerClickListener(this);
        }
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat", 0);
        double lon = intent.getDoubleExtra("lon", 0);
        int type = intent.getIntExtra("type", 0);
        objectId = intent.getStringExtra("objectId");
        Marker marker = aMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title("")
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), getMarkerIcon(type)))));
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat, lon), 18, 0, 0)));

    }

    private int getMarkerIcon(int type) {
        int icon = 0;
        switch (type) {
            case 0:
                icon = R.drawable.alert_pin_loads;
                break;
            case 1:
                icon = R.drawable.alert_pin_accident;
                break;
            case 2:
                icon = R.drawable.alert_pin_chit_chat;
                break;
            case 3:
                icon = R.drawable.alert_pin_police;
                break;
        }
        return icon;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Object obj = marker.getObject();
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        Object obj = marker.getObject();
        View view = null;
        if (obj == null) {
            AVObject markerAVObj = AVObject.createWithoutData("MapMarker", objectId);
            markerAVObj.fetchInBackground(new GetCallback<AVObject>() {
                @Override
                public void done(AVObject avObject, AVException e) {
                    if (e == null) {
                        marker.setObject(avObject);
                        marker.showInfoWindow();
                    } else {
                        GlobalUtil.showNetworkError();
                    }
                }
            });
        } else {
            view = getWindowView(marker, (AVObject) obj);
        }
        return view;
    }

    private View getWindowView(final Marker marker, AVObject obj) {
        final AVObject avObj = (AVObject) obj;
        AVUser user = AVUser.getCurrentUser();
        View view = View.inflate(this, R.layout.map_marker_window, null);
        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        TextView username = (TextView) view.findViewById(R.id.username);
        TextView time = (TextView) view.findViewById(R.id.time);
        ImageView hideBtn = (ImageView) view.findViewById(R.id.hideBtn);
        EmojiTextView etv = (EmojiTextView) view.findViewById(R.id.msgContent);
        MarkerImageGridView imgGrid = (MarkerImageGridView) view.findViewById(R.id.imgGrid);
        MarkerCommentListView markerCommentListView = (MarkerCommentListView) view.findViewById(R.id.commentList);
        LinearLayout likeContainer = (LinearLayout) view.findViewById(R.id.likeContainer);
        LinearLayout commentContainer = (LinearLayout) view.findViewById(R.id.commentContainer);
        final MarkerCommentListView commentList = (MarkerCommentListView) view.findViewById(R.id.commentList);
        final TextView likeCount = (TextView) view.findViewById(R.id.likeCount);
        final TextView commentCount = (TextView) view.findViewById(R.id.commentCount);
        AVQuery<AVObject> query = new AVQuery<>("LikeRecord");
        query.whereEqualTo("type", 0);
        query.whereEqualTo("attachId", avObj.getObjectId());
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    likeCount.setText("" + i);
                } else {
                    Log.e("", "", e);
                    GlobalUtil.showNetworkError();
                }
            }
        });
        query = new AVQuery<>("MapMarkerComment");
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
        commentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date attachDate = avObj.getCreatedAt();
                Date now = new Date();
                now.setHours(now.getHours() - 1);
                if (attachDate.getTime() < now.getTime()) {
                    ViewInject.longToast("该话题已关闭评论");
                    return;
                }
                CommentDialog dialog = new CommentDialog(MyMapMarker.this, avObj, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AVObject markerComment = (AVObject) v.getTag();
                        commentList.appendComment(markerComment);
                    }
                });
                dialog.show();
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
        if (user.has("avatar")) {
            AVFile avatarFile = user.getAVFile("avatar");
            ImageLoader.getInstance().displayImage(avatarFile.getUrl(), avatar, HttpUtil.DefaultOptions);
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        markerCommentListView.setMarkerId(avObj);
        return view;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
