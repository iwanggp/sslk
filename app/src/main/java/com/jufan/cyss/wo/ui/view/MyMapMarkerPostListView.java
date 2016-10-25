package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.wo.ui.MyMapMarker;
import com.jufan.cyss.wo.ui.MyPost;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cyjss on 2015/3/19.
 */
public class MyMapMarkerPostListView extends KJListView implements KJRefreshListener, AdapterView.OnItemClickListener {

    private int page = 0;
    private int pageNum = 20;
    private Date refreshDate = new Date();

    private MyPost myPost;

    private MyMapMarkerPostAdapter adapter;

    public MyMapMarkerPostListView(Context context) {
        super(context);
    }

    public MyMapMarkerPostListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMapMarkerPostListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(MyPost myPost) {
        this.myPost = myPost;
        setPullLoadEnable(true);
        setPullRefreshEnable(true);
        setOnRefreshListener(this);
        setOnItemClickListener(this);
        loadMore();
    }

    private void loadMore() {
        this.myPost.showLoading();
        AVUser user = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarker");
        query.whereEqualTo("user", user);
        query.orderByDescending("createdAt");
        query.limit(pageNum);
        query.skip(pageNum * page);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopLoadMore();
                myPost.hideLoading();
                if (e == null) {
                    if (page == 0) {
                        if (avObjects.isEmpty()) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                            setAdapter(new NoDataAdapter(getContext()));
                        } else {
                            if (avObjects.size() < pageNum) {
                                setPullLoadEnable(false);
                                getFooterView().setVisibility(GONE);
                            }
                            adapter = new MyMapMarkerPostAdapter();
                            setAdapter(adapter);
                            adapter.addMoreData(avObjects);
                        }
                    } else {
                        if (avObjects.isEmpty()) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                            setAdapter(new NoDataAdapter(getContext()));
                        } else {
                            adapter.addMoreData(avObjects);
                        }
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    private void refreshData() {
        AVUser user = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarker");
        query.whereEqualTo("user", user);
        query.orderByDescending("createdAt");
        query.whereGreaterThan("createdAt", refreshDate);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopRefreshData();
                refreshDate = new Date();
                if (e == null) {
                    if (avObjects.isEmpty()) {

                    } else {
                        if (adapter != null) {
                            adapter.addRefreshData(avObjects);
                        }
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public void onLoadMore() {
        loadMore();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AVObject obj = (AVObject) adapter.getItem(position - 1);
        Intent intent = new Intent(getContext(), MyMapMarker.class);
        intent.putExtra("lat", obj.getDouble("lat"));
        intent.putExtra("lon", obj.getDouble("lon"));
        intent.putExtra("type", obj.getInt("type"));
        intent.putExtra("objectId", obj.getObjectId());
        getContext().startActivity(intent);
    }


    private class MyMapMarkerPostAdapter extends BaseAdapter implements OnClickListener {

        private List<AVObject> dataSource = new ArrayList<AVObject>();

        public void addMoreData(List<AVObject> data) {
            dataSource.addAll(data);
            notifyDataSetChanged();
        }

        public void addRefreshData(List<AVObject> data) {
            dataSource.addAll(0, data);
            notifyDataSetChanged();
        }

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
            final AVObject avObject = (AVObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_my_map_marker_item, null);
                convertView.setOnClickListener(this);
                holder = new Holder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.content = (EmojiTextView) convertView.findViewById(R.id.content);
                holder.markerType = (ImageView) convertView.findViewById(R.id.markerType);
                holder.imgGrid = (MarkerImageGridView) convertView.findViewById(R.id.imgGrid);
                holder.like = (TextView) convertView.findViewById(R.id.like);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            AVUser user = AVUser.getCurrentUser();
            AVFile avatar = user.getAVFile("avatar");
            if (avatar == null) {
                holder.avatar.setImageResource(R.drawable.default_avatar);
            } else {
                ImageLoader.getInstance().displayImage(avatar.getUrl(), holder.avatar, HttpUtil.DefaultOptions);
            }
            holder.username.setText(user.getUsername());
            holder.time.setText(DateUtil.detailDateStr(avObject.getCreatedAt()));
            holder.content.setEmojiText(avObject.getString("text"));
            holder.imgGrid.setDataSource(avObject.getList("images"));
            holder.markerType.setImageResource(getMarkerIcon(avObject.getInt("type")));
            holder.data = avObject;
            if (avObject.has("likeCount")) {
                holder.like.setText("" + avObject.getInt("likeCount"));
            } else {
                final TextView likeCountTemp = holder.like;
                AVQuery<AVObject> query = new AVQuery<AVObject>("LikeRecord");
                query.whereEqualTo("attachId", avObject.getObjectId());
                query.countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, AVException e) {
                        likeCountTemp.setText(i + "");
                        avObject.put("likeCount", i);
                    }
                });
            }
            if (avObject.has("commentCount")) {
                holder.comment.setText("" + avObject.getInt("commentCount"));
            } else {
                final TextView commentCountTemp = holder.comment;
                AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarkerComment");
                query.whereEqualTo("attachObj", avObject);
                query.countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, AVException e) {
                        commentCountTemp.setText(i + "");
                        avObject.put("commentCount", i);
                    }
                });
            }
            return convertView;
        }

        private int getMarkerIcon(int type) {
            int icon = 0;
            switch (type) {
                case 0:
                    icon = R.drawable.alert_icon_traffic_info;
                    break;
                case 1:
                    icon = R.drawable.alert_icon_accident;
                    break;
                case 2:
                    icon = R.drawable.chat;
                    break;
                case 3:
                    icon = R.drawable.alert_icon_police;
                    break;
            }
            return icon;
        }

        @Override
        public void onClick(View v) {
            Holder holder = (Holder) v.getTag();
            AVObject obj = holder.data;
            Intent intent = new Intent(getContext(), MyMapMarker.class);
            intent.putExtra("lat", obj.getDouble("lat"));
            intent.putExtra("lon", obj.getDouble("lon"));
            intent.putExtra("type", obj.getInt("type"));
            intent.putExtra("objectId", obj.getObjectId());
            getContext().startActivity(intent);
        }
    }

    private class Holder {
        public ImageView avatar;
        public TextView username;
        public TextView time;
        public EmojiTextView content;
        public ImageView markerType;
        public MarkerImageGridView imgGrid;
        public TextView like;
        public TextView comment;
        public AVObject data;
    }
}
