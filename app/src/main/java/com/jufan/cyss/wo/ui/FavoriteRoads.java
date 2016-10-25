package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.GlobalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by cyjss on 2015/3/4.
 */
public class FavoriteRoads extends BaseUNIActivity implements AdapterView.OnItemClickListener {

    private JSONArray favRoads;
    private List<AVObject> favRoadsAVList;

    @BindView(id = R.id.favRoadsList)
    private ListView favRoadsList;
    @BindView(id = R.id.noFavTip)
    private TextView noFavTip;
    @BindView(id = R.id.deleteBtn, click = true)
    private Button deleteBtn;

    private FavoriteRoadsAdapter adapter;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_favorite_roads);
    }

    @Override
    protected void initWidget() {
        setupActionBar("路况收藏", ActionBarType.BACK);
        fillData();
        favRoadsList.setOnItemClickListener(this);
    }

    private void fillData() {
        if (GlobalUtil.isLogin()) {
            showLoading();
            AVUser user = AVUser.getCurrentUser();
            AVQuery<AVObject> query = new AVQuery<AVObject>("FavoriteRoad");
            query.whereEqualTo("userId", user.getObjectId());
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> avObjects, AVException e) {
                    hideLoading();
                    if (e == null) {
                        if (avObjects.isEmpty()) {
                            noFavTip.setVisibility(View.VISIBLE);
                            favRoadsList.setVisibility(View.GONE);
                        } else {
                            favRoadsAVList = avObjects;
                            favRoads = new JSONArray();
                            for (AVObject road : avObjects) {
                                JSONObject obj = new JSONObject();
                                try {
                                    obj.put("dir", road.getString("dir"));
                                    obj.put("road", road.getString("road"));
                                    favRoads.put(obj);
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            adapter = new FavoriteRoadsAdapter();
                            favRoadsList.setAdapter(adapter);
                            noFavTip.setVisibility(View.GONE);
                            favRoadsList.setVisibility(View.VISIBLE);
                            setRightBtnListener();
                        }
                    } else {
                        GlobalUtil.showNetworkError();
                    }
                }
            });
        } else {
            Simple s = Simple.getByKey("fav_road");
            if (!StringUtils.isEmpty(s.value)) {
                favRoads = new JSONArray();
                try {
                    JSONObject obj = new JSONObject(s.value);
                    if (obj.length() == 0) {
                        noFavTip.setVisibility(View.VISIBLE);
                        favRoadsList.setVisibility(View.GONE);
                    } else {
                        Iterator<String> keys = obj.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            favRoads.put(obj.getJSONObject(key));
                        }
                        adapter = new FavoriteRoadsAdapter();
                        favRoadsList.setAdapter(adapter);
                        setRightBtnListener();
                        noFavTip.setVisibility(View.GONE);
                        favRoadsList.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                noFavTip.setVisibility(View.VISIBLE);
                favRoadsList.setVisibility(View.GONE);
            }
        }
    }

    private void setRightBtnListener() {
        final Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText("管理");
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getMode() == 0) {
                    deleteBtn.setVisibility(View.VISIBLE);
                    rightBtn.setText("取消");
                    adapter.setMode(1);
                } else {
                    deleteBtn.setVisibility(View.GONE);
                    adapter.setMode(0);
                    rightBtn.setText("管理");
                }
            }
        });
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.deleteBtn:
                Set<Integer> checkSet = adapter.getCheckSet();
                if (checkSet.isEmpty()) {
                    return;
                }
                if (GlobalUtil.isLogin()) {
                    showLoading();
                    final List<AVObject> deleteList = new ArrayList<AVObject>();
                    for (Integer index : checkSet) {
                        deleteList.add(favRoadsAVList.get(index));
                    }
                    checkSet.clear();
                    AVObject.deleteAllInBackground(deleteList, new DeleteCallback() {
                        @Override
                        public void done(AVException e) {
                            hideLoading();
                            if (e == null) {
                                favRoadsAVList.removeAll(deleteList);
                                if (favRoadsAVList.isEmpty()) {
                                    noFavTip.setVisibility(View.VISIBLE);
                                    favRoadsList.setVisibility(View.GONE);
                                } else {
                                    favRoads = new JSONArray();
                                    for (AVObject road : favRoadsAVList) {
                                        JSONObject obj = new JSONObject();
                                        try {
                                            obj.put("dir", road.getString("dir"));
                                            obj.put("road", road.getString("road"));
                                            favRoads.put(obj);
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    adapter = new FavoriteRoadsAdapter();
                                    favRoadsList.setAdapter(adapter);
                                }
                            } else {
                                GlobalUtil.showNetworkError();
                            }
                        }
                    });
                } else {
                    Simple s = Simple.getByKey("fav_road");
                    JSONObject saveObj = null;
                    try {
                        saveObj = new JSONObject(s.value);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (Integer index : checkSet) {
                        try {
                            JSONObject obj = favRoads.getJSONObject(index);
                            saveObj.remove(obj.getString("dir"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    checkSet.clear();
                    s.value = saveObj.toString();
                    s.save();
                    fillData();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Intent i = new Intent(this, RoadVideo.class);
            i.putExtra("json", favRoads.get(position).toString());
            startActivity(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class FavoriteRoadsAdapter extends BaseAdapter {

        private int mode = 0;
        private Set<Integer> checkSet = new HashSet<Integer>();

        @Override
        public int getCount() {
            return favRoads.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return favRoads.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setMode(int mode) {
            this.mode = mode;
            notifyDataSetChanged();
        }

        public int getMode() {
            return this.mode;
        }

        public Set<Integer> getCheckSet() {
            return checkSet;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject json = (JSONObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(FavoriteRoads.this, R.layout.list_fav_roads_item, null);
                holder = new Holder();
                holder.roadName = (TextView) convertView.findViewById(R.id.roadName);
                holder.checkRoad = (CheckBox) convertView.findViewById(R.id.checkRoad);
                holder.checkRoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int index = (int) buttonView.getTag();
                        if (isChecked) {
                            checkSet.add(index);
                        } else {
                            if (checkSet.contains(index))
                                checkSet.remove(index);
                        }
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            try {
                Log.d("", position + "-=->" + json);
                holder.roadName.setText(json.getString("road"));
                Log.d("", "-=->end");
                if (mode == 0) {
                    holder.checkRoad.setVisibility(View.GONE);
                } else {
                    holder.checkRoad.setVisibility(View.VISIBLE);
                    holder.checkRoad.setTag(position);
                    holder.checkRoad.setChecked(checkSet.contains(position));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    private class Holder {
        public TextView roadName;
        public CheckBox checkRoad;
    }
}
