package com.jufan.cyss.wo.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListViewListener;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.JtgzfwHttp;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;
import org.kymjs.aframe.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cyjss on 2015/3/28.
 */
public class CarList extends BaseUNIActivity implements AdapterView.OnItemClickListener {

    @BindView(id = R.id.carList)
    private SwipeListView carList;
    private CarListAdapter adapter;
    private List<AVObject> dataSource;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_car_list);
    }

    @Override
    protected void initWidget() {
        setupActionBar("车辆管理", ActionBarType.BACK);
        initRightBtn();
        carList.setOnItemClickListener(this);
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        carList.setOffsetLeft(width - 100);
        requestBindCar();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            requestBindCar();
        }
    }

    private void initRightBtn() {
        Button right = (Button) findViewById(R.id.rightBtn);
        right.setVisibility(View.VISIBLE);
        right.setText("添加");
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataSource == null) {
                    GlobalUtil.showNetworkError();
                } else {
                    if (dataSource.size() >= 3) {
                        ViewInject.longToast("目前最多只能绑定3俩机动车");
                    } else {
                        Intent intent = new Intent(CarList.this, BindCar.class);
                        startActivityForResult(intent, 200);
                    }
                }
            }
        });
    }

    private void requestBindCar() {
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            showLoading();
            AVQuery<AVObject> query = new AVQuery<AVObject>("BindVeh");
            query.whereEqualTo("userId", user.getObjectId());
            query.whereEqualTo("flag", true);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> avObjects, AVException e) {
                    hideLoading();
                    if (e == null) {
                        dataSource = avObjects;
                        if (avObjects.isEmpty()) {
                            carList.setAdapter(new NoDataAdapter(CarList.this, "您尚未绑定车辆"));
                        } else {
                            adapter = new CarListAdapter();
                            carList.setAdapter(adapter);
                        }
                    } else {
                        GlobalUtil.showNetworkError();
                        carList.setAdapter(new NoDataAdapter(CarList.this, "网络异常，请检查网络"));
                    }
                }
            });
        } else {
            Simple simple = Simple.getByKey("vio");
            try {
                dataSource = new ArrayList<AVObject>();
                if (StringUtils.isEmpty(simple.value)) {
                    carList.setAdapter(new NoDataAdapter(CarList.this, "您尚未绑定车辆"));
                } else {
                    JSONObject obj = new JSONObject(simple.value);
                    Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        AVObject avObject = new AVObject();
                        JSONObject veh = new JSONObject(obj.getString(key));
                        avObject.put("hphm", veh.getString("hphm"));
                        avObject.put("hpzl", veh.getString("hpzl"));
                        avObject.put("clsbdh", veh.getString("clsbdh"));
                        avObject.put("pp_pic", veh.getString("pp_pic"));
                        dataSource.add(avObject);
                    }
                    if (dataSource.isEmpty()) {
                        carList.setAdapter(new NoDataAdapter(CarList.this, "您尚未绑定车辆"));
                    } else {
                        adapter = new CarListAdapter();
                        carList.setAdapter(adapter);
                    }
                }
            } catch (JSONException e) {
                Simple.deleteByKy("vio");
                requestBindCar();
                e.printStackTrace();
            }
            simple.save();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Intent i = new Intent(this, MyVio.class);
            AVObject veh = (AVObject) adapter.getItem(position);
            i.putExtra("hphm", veh.getString("hphm"));
            i.putExtra("hpzl", veh.getString("hpzl"));
            i.putExtra("clsbdh", veh.getString("clsbdh"));
            startActivity(i);
        } catch (Exception e) {
            Log.d("", "should click no data item");
            e.printStackTrace();
        }
    }

    private class CarListAdapter extends BaseAdapter {

        private float downX;
        private float upX;

        @Override
        public int getCount() {
            return dataSource.size();
        }

        @Override
        public Object getItem(int position) {
            return dataSource.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AVObject avObject = (AVObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(CarList.this, R.layout.list_car_list_item, null);
                holder = new Holder();
                holder.showContent = (RelativeLayout) convertView.findViewById(R.id.showContent);
                holder.deleteBtn = (Button) convertView.findViewById(R.id.deleteBtn);
                holder.clpp = (ImageView) convertView.findViewById(R.id.clpp);
                holder.hphm = (TextView) convertView.findViewById(R.id.hphm);
                holder.vioNum = (TextView) convertView.findViewById(R.id.vioNum);
                holder.loading = (ProgressBar) convertView.findViewById(R.id.loading);
                holder.yxqz = (TextView) convertView.findViewById(R.id.yxqz);
                holder.yxqzLoading = (ProgressBar) convertView.findViewById(R.id.yxqzLoading);
                holder.fkje = (TextView) convertView.findViewById(R.id.fkje);
                holder.fkjeLoading = (ProgressBar) convertView.findViewById(R.id.fkjeLoading);
                holder.wfjf = (TextView) convertView.findViewById(R.id.wfjf);
                holder.wfjfLoading = (ProgressBar) convertView.findViewById(R.id.wfjfLoading);
                convertView.setTag(holder);
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        new AlertDialog.Builder(CarList.this).setTitle("提示").setMessage("是否删除？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                final AVObject veh = (AVObject) adapter.getItem(Integer.parseInt(v.getTag().toString()));
                                if(GlobalUtil.isLogin()) {
                                    veh.put("flag", false);
                                    CarList.this.showLoading();
                                    veh.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            CarList.this.hideLoading();
                                            if (e == null) {
                                                dataSource.remove(veh);
                                                if (dataSource.isEmpty()) {
                                                    carList.setAdapter(new NoDataAdapter(CarList.this, "您尚未绑定车辆"));
                                                } else {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                GlobalUtil.showNetworkError();
                                            }
                                        }
                                    });
                                } else {
                                    ViewInject.longToast("登录注册之后，可管理删除车辆");
                                    Intent i = new Intent(CarList.this, Login.class);
                                    CarList.this.startActivity(i);
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                });

                holder.showContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent i = new Intent(CarList.this, MyVio.class);
                            AVObject veh = (AVObject) adapter.getItem(Integer.parseInt(v.getTag().toString()));
                            i.putExtra("hphm", veh.getString("hphm"));
                            i.putExtra("hpzl", veh.getString("hpzl"));
                            i.putExtra("clsbdh", veh.getString("clsbdh"));
                            startActivity(i);
                        } catch (Exception e) {
                            Log.d("", "should click no data item");
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.deleteBtn.setTag(position);
            holder.showContent.setTag(position);
//            convertView.setOnTouchListener(new CarListTouchListener(convertView));
            String hphm = avObject.getString("hphm");
            String hpzl = avObject.getString("hpzl");
            String clsbdh = avObject.getString("clsbdh");
            if (hphm.length() == 6) {
                hphm = hphm.substring(1);
            }
            holder.hphm.setText("豫A" + hphm);
            if (avObject.has("hasLoaded") && avObject.getBoolean("hasLoaded")) {
                holder.loading.setVisibility(View.GONE);
                holder.yxqzLoading.setVisibility(View.GONE);
                holder.fkjeLoading.setVisibility(View.GONE);
                holder.wfjfLoading.setVisibility(View.GONE);
                holder.vioNum.setVisibility(View.VISIBLE);
                holder.vioNum.setText(avObject.getString("vioNum"));
                holder.yxqz.setText(avObject.getString("yxqz"));
                holder.fkje.setText(avObject.getString("fkje"));
                holder.wfjf.setText(avObject.getString("wfjf"));
                ImageLoader.getInstance().displayImage("assets://clpp/car_" + avObject.getString("pp_pic") + ".jpg", holder.clpp);
            } else {
                holder.loading.setVisibility(View.VISIBLE);
                holder.yxqzLoading.setVisibility(View.VISIBLE);
                holder.fkjeLoading.setVisibility(View.VISIBLE);
                holder.wfjfLoading.setVisibility(View.VISIBLE);
                holder.vioNum.setVisibility(View.GONE);
                Simple simple = Simple.getByKey("vio");
                try {
                    JSONObject obj = new JSONObject(simple.value);
                    String key = "A" + hphm + hpzl;
                    if (obj.has(key)) {
                        JSONObject vehObj = new JSONObject(obj.getString(key));
                        ImageLoader.getInstance().displayImage("assets://clpp/car_" + vehObj.getString("pp_pic") + ".jpg", holder.clpp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("hphm", hphm);
                    obj.put("clsbdh", clsbdh);
                    obj.put("hpzl", hpzl);
                    Groundy.create(JtgzfwHttp.class).callback(new GetVehVio()).arg("code", "S50100").arg("json", obj.toString()).arg("tag", position + "").queueUsing(CarList.this);
                } catch (Exception e) {
                }
            }
            return convertView;
        }


    }

    private class CarListTouchListener implements View.OnTouchListener {

        private View parentView;
        private RelativeLayout showContent;
        private Button deleteBtn;

        private float offsetX = 0;
        private float oldX = 0;

        private int width = 0;

        public CarListTouchListener(View view) {
            this.parentView = view;
            this.showContent = (RelativeLayout) view.findViewById(R.id.show_content);
            this.deleteBtn = (Button) view.findViewById(R.id.deleteBtn);
            if (deleteBtn.getWidth() > 0) {
                this.width = deleteBtn.getWidth();
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    oldX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    offsetX += event.getX() - oldX;
                    oldX = event.getX();
                    if (-(int) offsetX > this.width) {
                        offsetX = -width;
                    } else if (offsetX > 0) {
                        offsetX = 0;
                    }
                    lp.setMargins((int) offsetX, 0, -(int) offsetX, 0);
                    showContent.setLayoutParams(lp);
                    break;
                case MotionEvent.ACTION_UP:
                    if (-offsetX >= width / 2) {
                        offsetX = -width;
                    } else {
                        offsetX = 0;
                    }
                    lp.setMargins((int) offsetX, 0, -(int) offsetX, 0);
                    showContent.setLayoutParams(lp);
                    break;
            }
            return true;
        }
    }

    private class GetVehVio {
        @OnSuccess(JtgzfwHttp.class)
        public void onSuccess(@Param("data") String data, @Param("tag") String tag) {
            hideLoading();
            int index = Integer.parseInt(tag);
            AVObject avObject = dataSource.get(index);
            JSONObject json = null;
            try {
                json = new JSONObject(data);
                json.put("update_time", DateUtil.detailDateStr(new Date()));
                data = json.toString();
                JSONArray wzxx = json.getJSONArray("wzxx");
                avObject.put("vioNum", wzxx.length() + "");
                avObject.put("wzxx", data);
                avObject.put("yxqz", json.getString("yxqz"));
                avObject.put("hasLoaded", true);
                avObject.put("pp_pic", json.getString("pp_pic"));
                int fkje = 0, wfjf = 0;
                for (int i = 0; i < wzxx.length(); i++) {
                    JSONObject item = wzxx.getJSONObject(i);
                    String fkjeStr = item.getString("fkje");
                    String wfxw = item.getString("wfxw");
                    String wfsj = item.getString("wfsj").substring(0, 4);
                    int wfjfItem = 0;
                    if (Integer.parseInt(wfsj) >= 2013) {
                        wfjfItem = Integer.parseInt(wfxw.substring(1, 2));
                    }
                    fkje += Integer.parseInt(fkjeStr);
                    wfjf += wfjfItem;
                }
                avObject.put("fkje", fkje + "");
                avObject.put("wfjf", wfjf + "");
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Simple simple = Simple.getByKey("vio");
            try {
                JSONObject obj = new JSONObject();
                if (StringUtils.isEmpty(simple.value)) {
                    simple.value = obj.toString();
                } else {
                    obj = new JSONObject(simple.value);
                }
                obj.put(json.getString("hphm") + json.getString("hpzl"), data);
                simple.value = obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            simple.save();
        }

        @OnFailure(JtgzfwHttp.class)
        public void onFailure(@Param("code") String code, @Param("desc") String desc) {
            ViewInject.longToast(desc);
        }
    }

    private class Holder {
        public RelativeLayout showContent;
        public Button deleteBtn;
        public ImageView clpp;
        public TextView hphm;
        public TextView vioNum;
        public ProgressBar loading;
        public TextView yxqz;
        public ProgressBar yxqzLoading;
        public TextView fkje;
        public ProgressBar fkjeLoading;
        public TextView wfjf;
        public ProgressBar wfjfLoading;
    }
}
