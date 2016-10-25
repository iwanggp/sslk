package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.jufan.cyss.amap.cluster.ClusterClickListener;
import com.jufan.cyss.amap.cluster.ClusterItem;
import com.jufan.cyss.amap.cluster.ClusterOverlay;
import com.jufan.cyss.amap.cluster.ClusterRender;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.http.JtgzfwHttp;
import com.jufan.cyss.http.RoadVideoHttp;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.SharedPreferencesUtil;
import com.jufan.cyss.wo.ui.view.FilterMapMarkerDialog;
import com.jufan.cyss.wo.ui.view.MapSelectDialog;
import com.jufan.cyss.wo.ui.view.MarkerCommentListView;
import com.jufan.cyss.wo.ui.view.ShareDialog;
import com.jufan.cyss.wo.ui.view.factory.MapMarkerViewFactory;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;
import com.umeng.message.UmengRegistrar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cyjss on 2015/1/29.
 */
public class RoadMapFragment extends BaseUNIFragment implements AMapLocationListener, LocationSource,
        AMap.InfoWindowAdapter, AMap.OnMarkerClickListener, AMap.OnMapTouchListener,
        AMap.OnMapLoadedListener {

    private MapView map;
    private AMap aMap;
    private UiSettings uiSettings;
    private SharedPreferencesUtil sp;
    private LocationManagerProxy locationManager;
    private OnLocationChangedListener listener;
    private Marker locationMarker;// 定位雷达小图标
    @BindView(id = R.id.mshare, click = true)
    private Button share;
    @BindView(id = R.id.selectBtn, click = true)
    private Button selectBtn;
//    private boolean isRotate = false;

    private MapSelectDialog mapSelectDialog;
    private FilterMapMarkerDialog filterMapMarkerDialog;
    private JSONObject filterJson;

    private JSONArray roads;
    private List<Marker> markerList = new ArrayList<Marker>();
    private JSONArray eyesList;
    private View nowShowMapWindow;
    private Marker nowShowMapMarker;

    private Timer timer;//同步每个marker的时间及消除过时marker

    private final int MARKER_STAND_TIME = 3400 * 1000;//一小时-200s
    private final int clusterRadius = 60;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //定时器修改window的time
                case 0:
                    TextView time = (TextView) nowShowMapWindow.findViewById(R.id.time);
                    time.setText(DateUtil.getShortTimeDesc((Date) msg.obj));
                    break;
            }
        }
    };

    @Override
    protected View inflaterView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_road_map, null);
        map = (MapView) v.findViewById(R.id.map);
        map.onCreate(bundle);
        return v;
    }

    @Override
    public void widgetResume() {
        application.getMainActivity().setupActionBar("实时路况");
        Button rightBtn = (Button) application.getMainActivity().findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText("筛选");
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMapMarkerDialog.show();
            }
        });
        reqMyLocation();
        startTimer();
        if (!pushList.isEmpty()) {
            synchronized (pushList) {
                for (JSONObject json : pushList) {
                    handlePushData(json);
                }
                pushList.clear();
            }
        }
    }

    @Override
    public void initWidget(View parentView) {
        mapSelectDialog = new MapSelectDialog(getActivity(), selectBtn, this);
        if (aMap == null) {
            aMap = map.getMap();
            MyTrafficStyle myTrafficStyle = new MyTrafficStyle();
            myTrafficStyle.setSeriousCongestedColor(0xff92000a);
            myTrafficStyle.setCongestedColor(0xffea0312);
            myTrafficStyle.setSlowColor(0xffff7508);
            myTrafficStyle.setSmoothColor(0xff00a209);
            aMap.setMyTrafficStyle(myTrafficStyle);
            aMap.setTrafficEnabled(true);
            sp=new SharedPreferencesUtil(getActivity());
            this.uiSettings = aMap.getUiSettings();
            this.uiSettings.setZoomControlsEnabled(false);
            this.uiSettings.setCompassEnabled(true);
            this.uiSettings.setMyLocationButtonEnabled(true);

            ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
            giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point1));
            giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point2));
            giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point3));
            giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point4));
            giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point5));
            giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point6));
            locationMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                    .icons(giflist).period(50));

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
            myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
            myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
            // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
            myLocationStyle.strokeWidth(0.1f);// 设置圆形的边框粗细
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationRotateAngle(180);
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            //设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            aMap.setInfoWindowAdapter(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnMapTouchListener(this);
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(34.790, 113.675), 12, 0, 0)));
        }
        filterMapMarkerDialog = new FilterMapMarkerDialog(getActivity(), aMap);
        filterJson = filterMapMarkerDialog.getFilterStorageJson();
        try {
            application.getMainActivity().showLoading();
            JSONObject obj = new JSONObject();
            Groundy.create(RoadVideoHttp.class).callback(this).arg("url", RoadVideoHttp.REQ_GET_ALL_VIDEO_URL).arg("json", obj.toString()).queueUsing(this.getActivity());
            Groundy.create(JtgzfwHttp.class).callback(new EyesMap()).arg("code", "S50101").arg("json", "{}").arg("_url", HttpUtil.REQ_TEST_URL).queueUsing(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarker");
        Date date = new Date();
        date.setHours(date.getHours() - 1);
        query.whereGreaterThan("createdAt", date);
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null) {
                    addMarkersToMap(avObjects);
                    startTimer();
                } else {
                    Log.e("", "", e);
                    GlobalUtil.showNetworkError();
                }
            }
        });
        boolean isFirst=sp.isFirstLogin();
      if(isFirst) {
          widgetClick(selectBtn);//进入本界面时激活该按钮
      }
        sp.firstLogin();
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.selectBtn:
                RotateAnimation rotateAnimation = new RotateAnimation(0, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setFillAfter(true);
                rotateAnimation.setDuration(300);
                rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mapSelectDialog.show();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                selectBtn.startAnimation(rotateAnimation);
                break;
            case R.id.mshare:
                ShareDialog shareDialog = new ShareDialog(getActivity());
                shareDialog.show();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        deactivate();
        stopTimer();
    }

    @OnSuccess(RoadVideoHttp.class)
    public void onSuccess(@Param("data") String data) {
        application.getMainActivity().hideLoading();
        try {
            JSONObject json = new JSONObject(data);
            this.roads = json.getJSONArray("roads");
            if (roads.length() == 0) {
                ViewInject.longToast("服务异常，无法加载路况摄像头");
            } else {
                addRoadMarkersToMap();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnFailure(RoadVideoHttp.class)
    public void onFailure(@Param("code") String code, @Param("desc") String desc) {
        ViewInject.longToast(desc);
        application.getMainActivity().hideLoading();
    }


    @Override
    public void onMapLoaded() {
    }

    private class EyesMap implements ClusterRender, ClusterClickListener {
        @OnSuccess(JtgzfwHttp.class)
        public void onSuccess(@Param("data") String data) {
            application.getMainActivity().hideLoading();
            try {
                JSONObject json = new JSONObject(data);
                eyesList = json.getJSONArray("result");
                ClusterOverlay clusterOverlay = new ClusterOverlay(aMap,
                        GlobalUtil.dip2px(getActivity(), clusterRadius),
                        getActivity());
                clusterOverlay.setClusterRenderer(this);
                clusterOverlay.setOnClusterClickListener(this);
                for (int i = 0; i < eyesList.length(); i++) {
                    try {
                        JSONObject item = eyesList.getJSONObject(i);
                        LatLng ll = new LatLng(Double.parseDouble(item.getString("lat")), Double.parseDouble(item.getString("lon")));
                        ll = bd09Decrypt(ll);
                        clusterOverlay.addClusterItem(new EyesItem(ll, item));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @OnFailure(JtgzfwHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            ViewInject.longToast(desc);
            application.getMainActivity().hideLoading();
        }

        @Override
        public void onClick(Marker marker, List<ClusterItem> clusterItems) {
            List<JSONObject> list = new ArrayList<JSONObject>();
            for (ClusterItem item : clusterItems) {
                EyesItem ei = (EyesItem) item;
                list.add(ei.getJson());
            }
            marker.setObject(list);
            marker.setTitle("Eyes");
            marker.showInfoWindow();
        }

        @Override
        public Drawable getDrawAble(int clusterNum) {
            return getActivity().getResources().getDrawable(
                    R.drawable.eyes);
        }
    }

    private void startTimer() {
        if (timer == null) {
            Log.d("RoadMapFragment", "start timer");
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (markerList != null && !markerList.isEmpty()) {
                        Log.d("RoadMapFragment", "size: " + markerList.size());
                        for (Marker marker : markerList) {
                            AVObject obj = (AVObject) marker.getObject();
                            Date createdAt = obj.getCreatedAt();
                            Date now = new Date();
                            Log.d("RoadMapFragment", "time gap:" + (now.getTime() - createdAt.getTime()));
                            if (now.getTime() - createdAt.getTime() > MARKER_STAND_TIME) {
                                Log.d("RoadMapFragment", "disappearMarker");
                                disappearMarker(marker);
                                return;
                            }
                            if (marker.isInfoWindowShown()) {
                                Message msg = handler.obtainMessage();
                                msg.what = 0;
                                msg.obj = createdAt;
                                handler.sendMessage(msg);
                            }
                        }
                    }
                }
            }, 10, 1000 * 5);
        }
    }

    private void stopTimer() {
        Log.d("RoadMapFramgment", "stop timer");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void addRoadMarkersToMap() {
        for (int i = 0; i < roads.length(); i++) {
            try {
                JSONObject item = roads.getJSONObject(i);
                LatLng ll = new LatLng(Double.parseDouble(item.getString("lat")), Double.parseDouble(item.getString("lon")));
                ll = bd09Decrypt(ll);
                Marker marker = aMap.addMarker(new MarkerOptions()
                        .position(ll)
                        .title(item.getString("road"))
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marka))));
                marker.setObject(item);
                marker.setVisible(filterJson.getBoolean("camera"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //百度坐标转火星坐标
    private LatLng bd09Decrypt(LatLng bd09) {
        double M_PI = 3.1415926535897932384626;
        double x = bd09.longitude - 0.0065, y = bd09.latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * M_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * M_PI);
        return new LatLng(z * Math.sin(theta), z * Math.cos(theta));
    }

    private void addMarkersToMap(List<AVObject> list) {
        for (AVObject obj : list) {
            MarkerOptions options = new MarkerOptions();
            LatLng ll = new LatLng(obj.getDouble("lat"), obj.getDouble("lon"));
            int type = obj.getInt("type");
            int icon = getMarkerIcon(type);
            options.title(obj.get("text").toString());
            options.position(ll);
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), icon)));
            options.perspective(true);
            markerList.add(aMap.addMarker(options));
        }

        for (int i = 0; i < markerList.size(); i++) {
            Marker m = markerList.get(i);
            m.setObject(list.get(i));
            AVObject obj = (AVObject) m.getObject();
            try {
                if (obj.getInt("type") == 0 && !filterJson.getBoolean("roadStatus")) {
                    m.setVisible(false);
                }
                if (obj.getInt("type") == 1 && !filterJson.getBoolean("accident")) {
                    m.setVisible(false);
                }
                if (obj.getInt("type") == 2 && !filterJson.getBoolean("mood")) {
                    m.setVisible(false);
                }
                if (obj.getInt("type") == 3 && !filterJson.getBoolean("police")) {
                    m.setVisible(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void reqMyLocation() {
        aMap.setMyLocationEnabled(true);
    }

    public void addMarkersToMap(int type, AVObject obj) {
        addMarkersToMap(type, obj, true);
    }

    public void addMarkersToMap(int type, AVObject obj, boolean animate) {
        LatLng ll = new LatLng(obj.getDouble("lat"), obj.getDouble("lon"));
        int icon = getMarkerIcon(type);
        Marker marker = aMap.addMarker(new MarkerOptions()
                .position(ll)
                .title(obj.get("text").toString())
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), icon))));
        marker.setObject(obj);
        if (animate) {
            marker.showInfoWindow();
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(ll, 18, 0, 0)));
        }
        jumpPoint(marker);
        markerList.add(marker);
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

    public LatLng getMyLocation() {
        return locationMarker.getPosition();
    }

    private void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        startPoint.offset(0, -600);
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final LatLng ll = marker.getPosition();
        final Interpolator interpolator = new AccelerateInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = ll.longitude;
                double lat = t * ll.latitude + (1 - t)
                        * startLatLng.latitude;
                if (t < 1.0) {
                    marker.setPosition(new LatLng(lat, lng));
                    handler.postDelayed(this, 16);
                } else {
                    marker.setPosition(ll);
                }
            }
        });
    }

    private Handler disappearHandler = new Handler();

    private void disappearMarker(final Marker marker) {
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        Point endPoint = proj.toScreenLocation(marker.getPosition());
        endPoint.offset(0, -600);
        final LatLng endLatLng = proj.fromScreenLocation(endPoint);
        final long duration = 800;
        final LatLng ll = marker.getPosition();
        final Interpolator interpolator = new AnticipateInterpolator();
        disappearHandler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = ll.longitude;
                double lat = t * endLatLng.latitude + (1 - t)
                        * ll.latitude;
                if (t < 1.0) {
                    marker.setPosition(new LatLng(lat, lng));
                    disappearHandler.postDelayed(this, 16);
                } else {
                    marker.setPosition(endLatLng);
                    marker.remove();
                }
            }
        });
        markerList.remove(marker);
    }

    public void appendCommentToWindow(AVObject comment) {
        MarkerCommentListView commentList = (MarkerCommentListView) nowShowMapWindow.findViewById(R.id.commentList);
        commentList.appendComment(comment);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (listener != null && location != null) {
            listener.onLocationChanged(location);// 显示系统小蓝点
            locationMarker.setPosition(new LatLng(location.getLatitude(), location
                    .getLongitude()));// 定位雷达小图标
            float bearing = aMap.getCameraPosition().bearing;
            aMap.setMyLocationRotateAngle(bearing);// 设置小蓝点旋转角度
        }
    }

    /**
     * 此方法已经废弃
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.listener = onLocationChangedListener;
        if (locationManager == null) {
            locationManager = LocationManagerProxy.getInstance(getActivity());
            locationManager.setGpsEnable(true);
            /*
             * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
            locationManager.requestLocationUpdates(
                    LocationProviderProxy.AMapNetwork, 2000, 10, this);
        }
    }

    @Override
    public void deactivate() {
        listener = null;
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            locationManager.destory();
        }
        locationManager = null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        this.nowShowMapMarker = marker;
        Object obj = marker.getObject();
        View view = null;
        if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            view = MapMarkerViewFactory.createRoadView(getActivity(), json, marker, application.getMainActivity());
        } else if (obj instanceof AVObject) {
            AVObject avObj = (AVObject) obj;
            view = MapMarkerViewFactory.createMarkerView(getActivity(), avObj, marker, this, application.getMainActivity());
        } else if (obj instanceof List) {
            List<JSONObject> list = (List<JSONObject>) obj;
            view = MapMarkerViewFactory.createEyesView(getActivity(), list, marker, this, application.getMainActivity());
        }
        nowShowMapWindow = view;
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        disappearMarker(marker);
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            AVFile imgFile = null;
            if (MainActivity.REQUEST_TRAFFIC_LOAD_IMAGE == requestCode) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                try {
                    imgFile = AVFile.withAbsoluteLocalPath("img.jpg", picturePath);
                } catch (IOException e) {
                    Log.e("", "", e);
                    e.printStackTrace();
                }
            } else if (MainActivity.REQUEST_TRAFFIC_CAPTURE_IMAGE == requestCode) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imgFile = new AVFile("img.jpg", baos.toByteArray());
            }
            if (imgFile != null) {
                mapSelectDialog.getTrafficDialog().addImage(imgFile);
            }
        }
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
//        nowShowMapMarker.hideInfoWindow();
    }

    @Override
    public void executeReceiver(JSONObject json) {
        handlePushData(json);
    }

    private void handlePushData(JSONObject json) {
        try {
            //0: add Marker 1: add Comment
            String deviceToken = UmengRegistrar.getRegistrationId(getActivity());
            int action = json.getInt("custom_action");
            String from = json.getString("_from_");
            Log.d(getPageName(), "from:" + from);
            Log.d(getPageName(), "deviceToken:" + deviceToken);
            if (action == 0) {
                if (from.equals(deviceToken)) {
                    return;
                }
                String objectId = json.getString("objectId");
                AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarker");
                query.include("user");
                query.whereEqualTo("objectId", objectId);
                query.getFirstInBackground(new GetCallback<AVObject>() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        if (e == null && avObject != null) {
                            addMarkersToMap(avObject.getInt("type"), avObject, false);
                        }
                    }
                });
            } else if (action == 1) {
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class EyesItem implements ClusterItem {

        private LatLng ll;
        private JSONObject json;

        public EyesItem(LatLng ll, JSONObject json) {
            this.ll = ll;
            this.json = json;
        }

        public void setJson(JSONObject json) {
            this.json = json;
        }

        public JSONObject getJson() {
            return json;
        }

        public void setPosition(LatLng ll) {
            this.ll = ll;
        }

        @Override
        public LatLng getPosition() {
            return ll;
        }
    }
}
