package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.avos.avoscloud.AVObject;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.List;

/**
 * Created by cyjss on 2015/3/18.
 */
public class FilterMapMarkerDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {

    public static final String SHOWN_STORAGE_KEY = "marker_show_storage";
    private AMap aMap;

    private CheckBox camera;
    private CheckBox roadStatus;
    private CheckBox accident;
    private CheckBox mood;
    private CheckBox police;
    private CheckBox eyes;

    public FilterMapMarkerDialog(Context context, AMap aMap) {
        super(context, R.style.filterDataDialog);
        setContentView(R.layout.dialog_filter_map_marker);
        this.aMap = aMap;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.camera = (CheckBox) findViewById(R.id.camera);
        this.roadStatus = (CheckBox) findViewById(R.id.roadStatus);
        this.accident = (CheckBox) findViewById(R.id.accident);
        this.mood = (CheckBox) findViewById(R.id.mood);
        this.police = (CheckBox) findViewById(R.id.police);
        this.eyes = (CheckBox) findViewById(R.id.eyes);
        this.camera.setOnCheckedChangeListener(this);
        this.roadStatus.setOnCheckedChangeListener(this);
        this.accident.setOnCheckedChangeListener(this);
        this.mood.setOnCheckedChangeListener(this);
        this.police.setOnCheckedChangeListener(this);
        this.eyes.setOnCheckedChangeListener(this);

        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 20;
        lp.y = GlobalUtil.dip2px(getContext(), 66);
        lp.gravity = Gravity.TOP | Gravity.RIGHT;
        window.setAttributes(lp);
        Simple simple = Simple.getByKey(SHOWN_STORAGE_KEY);
        if (StringUtils.isEmpty(simple.value)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("camera", true);
                obj.put("roadStatus", true);
                obj.put("accident", true);
                obj.put("mood", true);
                obj.put("police", true);
                obj.put("eyes", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            simple.value = obj.toString();
            simple.save();
        } else {
            try {
                JSONObject obj = new JSONObject(simple.value);
                camera.setChecked(obj.getBoolean("camera"));
                roadStatus.setChecked(obj.getBoolean("roadStatus"));
                accident.setChecked(obj.getBoolean("accident"));
                mood.setChecked(obj.getBoolean("mood"));
                police.setChecked(obj.getBoolean("police"));
                if (obj.has("eyes")) {
                    eyes.setChecked(obj.getBoolean("eyes"));
                } else {
                    eyes.setChecked(true);
                    obj.put("eyes", true);
                    simple.value = obj.toString();
                    simple.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.camera:
                setCameraVisible(isChecked);
                break;
            case R.id.roadStatus:
                setOtherVisible(0, isChecked);
                break;
            case R.id.accident:
                setOtherVisible(1, isChecked);
                break;
            case R.id.mood:
                setOtherVisible(2, isChecked);
                break;
            case R.id.police:
                setOtherVisible(3, isChecked);
                break;
            case R.id.eyes:
                setEyesVisible(isChecked);
                break;
        }
    }

    public void setEyesVisible(boolean flag) {
        if (aMap != null) {
            List<Marker> markerList = aMap.getMapScreenMarkers();
            for (Marker marker : markerList) {
                if (marker.getObject() == null) {
                    continue;
                } else {
                    Object obj = marker.getObject();
                    if (obj instanceof List) {
                        marker.setVisible(flag);
                    } else if (obj instanceof String) {
                        marker.setVisible(flag);
                    }
                }
            }
        }
    }

    public void setCameraVisible(boolean flag) {
        if (aMap != null) {
            List<Marker> markerList = aMap.getMapScreenMarkers();
            for (Marker marker : markerList) {
                if (marker.getObject() == null) {
                    continue;
                } else {
                    Object obj = marker.getObject();
                    if (obj instanceof JSONObject) {
                        JSONObject json = (JSONObject) obj;
                        if (json.has("dir")) {
                            marker.setVisible(flag);
                        }
                    }
                }
            }
        }
    }

    public void setOtherVisible(int type, boolean flag) {
        if (aMap != null) {
            List<Marker> markerList = aMap.getMapScreenMarkers();
            for (Marker marker : markerList) {
                if (marker.getObject() == null) {
                    continue;
                } else {
                    Object obj = marker.getObject();
                    if (obj instanceof AVObject) {
                        AVObject avObject = (AVObject) obj;
                        if (avObject.getInt("type") == type) {
                            marker.setVisible(flag);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Simple simple = Simple.getByKey(SHOWN_STORAGE_KEY);
        JSONObject obj = new JSONObject();
        try {
            obj.put("camera", camera.isChecked());
            obj.put("roadStatus", roadStatus.isChecked());
            obj.put("accident", accident.isChecked());
            obj.put("mood", mood.isChecked());
            obj.put("police", police.isChecked());
            obj.put("eyes", eyes.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        simple.value = obj.toString();
        simple.save();
    }

    public JSONObject getFilterStorageJson() {
        Simple simple = Simple.getByKey(FilterMapMarkerDialog.SHOWN_STORAGE_KEY);
        JSONObject obj = new JSONObject();
        if (StringUtils.isEmpty(simple.value)) {
            try {
                obj.put("camera", true);
                obj.put("roadStatus", true);
                obj.put("accident", true);
                obj.put("mood", true);
                obj.put("police", true);
                obj.put("eyes", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            simple.value = obj.toString();
            simple.save();
        } else {
            try {
                obj = new JSONObject(simple.value);
                if (!obj.has("eyes")) {
                    obj.put("eyes", true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

}
