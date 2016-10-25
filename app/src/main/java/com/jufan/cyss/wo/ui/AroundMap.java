package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.GlobalUtil;

import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyjss on 2015/3/4.
 */
public class AroundMap extends BaseUNIActivity implements AMapLocationListener, LocationSource, PoiSearch.OnPoiSearchListener {

    private String title;
    @BindView(id = R.id.map)
    private com.amap.api.maps.MapView map;

    private AMap aMap;
    private UiSettings uiSettings;
    private LocationManagerProxy locationManager;
    private LocationSource.OnLocationChangedListener listener;
    private Marker locationMarker;// 定位雷达小图标
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult; // poi返回的结果
    private PoiOverlay poiOverlay;// poi图层
    private List<PoiItem> poiItems;// poi数据

    private Toast locationToast;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_around_map);
    }

    @Override
    protected void initWidget() {
        Intent i = getIntent();
        this.title = i.getStringExtra("title");
        setupActionBar(this.title, ActionBarType.BACK);

        if (aMap == null) {
            aMap = map.getMap();
            MyTrafficStyle myTrafficStyle = new MyTrafficStyle();
            myTrafficStyle.setSeriousCongestedColor(0xff92000a);
            myTrafficStyle.setCongestedColor(0xffea0312);
            myTrafficStyle.setSlowColor(0xffff7508);
            myTrafficStyle.setSmoothColor(0xff00a209);
            aMap.setMyTrafficStyle(myTrafficStyle);
            aMap.setTrafficEnabled(true);

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
        }

        this.locationToast = Toast.makeText(this, "正在定位...", Toast.LENGTH_LONG);
        this.locationToast.show();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        map.onCreate(bundle);
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
            LatLng lp = new LatLng(location.getLatitude(), location
                    .getLongitude());
            locationMarker.setPosition(lp);// 定位雷达小图标
            float bearing = aMap.getCameraPosition().bearing;
            aMap.setMyLocationRotateAngle(bearing);// 设置小蓝点旋转角度
            if (this.locationToast != null) {
                this.locationToast.cancel();
            }
            if (query == null) {
                query = new PoiSearch.Query("", this.title, "郑州市");
                query.setPageSize(10);
                query.setPageNum(0);
                query.setLimitDiscount(false);
                query.setLimitGroupbuy(false);
                poiSearch = new PoiSearch(this, query);
                poiSearch.setOnPoiSearchListener(this);
                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(location.getLatitude(), location
                        .getLongitude()), 4000, true));//
                poiSearch.searchPOIAsyn();// 异步搜索
                showLoading();
            }
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
            locationManager = LocationManagerProxy.getInstance(this);
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
    public void onPoiSearched(PoiResult result, int rCode) {
        hideLoading();
        if (rCode == 0) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
//                        aMap.clear();// 清理之前的图标
                        poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        ViewInject.longToast("附近没有查询到" + title);
                    } else {
                        ViewInject.longToast("附近没有查询到" + title);
                    }
                }
            } else {
                ViewInject.longToast("附近没有查询到" + title);
            }
        } else if (rCode == 27) {
            GlobalUtil.showNetworkError();
        } else if (rCode == 32) {
            ViewInject.longToast("错误KEY");
        } else {
            ViewInject.longToast("未知异常[" + rCode + "]");
        }
    }

    @Override
    public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i) {

    }
}
