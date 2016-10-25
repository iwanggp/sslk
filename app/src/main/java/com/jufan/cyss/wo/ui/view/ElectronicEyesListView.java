package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.jufan.cyss.wo.ui.EyesComment;
import com.jufan.cyss.wo.ui.R;
import com.jufan.cyss.wo.ui.RoadMapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by cyjss on 2015/4/2.
 */
public class ElectronicEyesListView extends ListView implements AdapterView.OnItemClickListener {
    private List<JSONObject> dataSource;
    private ElectronicEyesAdapter adapter;
    private RoadMapFragment roadMapFragment;

    public ElectronicEyesListView(Context context) {
        super(context);
    }

    public ElectronicEyesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElectronicEyesListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDataSource(List<JSONObject> dataSource, RoadMapFragment roadMapFragment) {
        this.roadMapFragment = roadMapFragment;
        if (dataSource == null) {
            return;
        }
        this.dataSource = dataSource;
        for (JSONObject json : this.dataSource) {
            if (json.has("comment_count")) {
                json.remove("comment_count");
            }
        }
        adapter = new ElectronicEyesAdapter();
        setAdapter(adapter);
        setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject json = (JSONObject) adapter.getItem(position);
        Intent intent = new Intent(this.roadMapFragment.getActivity(), EyesComment.class);
        intent.putExtra("json", json.toString());
        this.roadMapFragment.startActivity(intent);
    }

    private class ElectronicEyesAdapter extends BaseAdapter {

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
            final JSONObject json = (JSONObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_eyes_item, null);
                holder = new Holder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.commentNumber = (TextView) convertView.findViewById(R.id.commentNumber);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            String attachKey = null;
            try {
                holder.name.setText(json.getString("address"));
                attachKey = json.getString("lddm") + json.getString("dldm");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.has("comment_count")) {
                    final TextView commentNumberTemp = holder.commentNumber;
                    AVQuery<AVObject> query = new AVQuery<>("EyesComment");
                    query.whereEqualTo("attachKey", attachKey);
                    query.countInBackground(new CountCallback() {
                        @Override
                        public void done(int i, AVException e) {
                            String count = i + "";
                            commentNumberTemp.setText("吐槽 " + count);
                            try {
                                json.put("comment_count", count);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                } else {
                    holder.commentNumber.setText("吐槽 " + json.getString("comment_count"));
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return convertView;
        }
    }

    private class Holder {
        public TextView name;
        public TextView commentNumber;
    }
}
