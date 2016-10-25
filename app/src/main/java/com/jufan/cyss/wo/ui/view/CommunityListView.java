package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.frame.BaseUNIApplication;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.LikeUtil;
import com.jufan.cyss.wo.ui.CommunityComment;
import com.jufan.cyss.wo.ui.CommunityFragment;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;
import org.kymjs.aframe.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cyjss on 2015/3/14.
 */
public class CommunityListView extends KJListView implements KJRefreshListener, AdapterView.OnItemClickListener {

    protected int page = 0;
    protected int pageNum = 10;
    protected Date refreshDate = new Date();

    private CommunityFragment communityFragment;
    private BaseUNIApplication application;
    protected CommunityListAdapter adapter;

    public CommunityListView(Context context) {
        super(context);
    }

    public CommunityListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommunityListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(CommunityFragment communityFragment) {
        this.communityFragment = communityFragment;
        application = (BaseUNIApplication) this.communityFragment.getActivity().getApplication();
        setOnRefreshListener(this);
        adapter = new CommunityListAdapter();
        setAdapter(adapter);
        setPullRefreshEnable(true);
        setPullLoadEnable(true);
        setOnItemClickListener(this);
        loadData();
    }

    protected void loadData() {
        application.getMainActivity().showLoading();
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityPost");
        query.orderByDescending("createdAt");
        query.skip(page * pageNum);
        query.limit(pageNum);
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                application.getMainActivity().hideLoading();
                stopLoadMore();
                if (e == null) {
                    if (avObjects == null || avObjects.isEmpty()) {
                        if (page == 0) {
                            setAdapter(new NoDataAdapter(getContext()));
                        } else {
                            ViewInject.longToast("没有更多数据");
                        }
                        setPullLoadEnable(false);
                        getFooterView().setVisibility(GONE);
                    } else {
                        adapter.addData(avObjects);
                        if (page == 0 && avObjects.size() != pageNum) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                        }
                        page++;
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityPost");
        query.orderByDescending("createdAt");
        query.whereGreaterThan("createdAt", refreshDate);
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopRefreshData();
                if (e == null) {
                    refreshDate = new Date();
                    if (avObjects != null && !avObjects.isEmpty()) {
                        if (adapter.getCount() == 0) {
                            setAdapter(adapter);
                        }
                        adapter.addRefreshData(avObjects);
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    public void addMyPost(AVObject post) {
        if (adapter.getCount() == 0) {
            setAdapter(adapter);
        }
        adapter.addData(post);
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent commentIntent = new Intent(getContext(), CommunityComment.class);
        AVObject post = (AVObject) adapter.getItem(position - 1);
        commentIntent.putExtra("objectId", post.getObjectId());
        getContext().startActivity(commentIntent);
    }

    protected class CommunityListAdapter extends BaseAdapter implements OnClickListener {

        private List<AVObject> dataSource = new ArrayList<AVObject>();
        private Set<String> objectIds = new HashSet<String>();

        public void addData(List<AVObject> data) {
            for (AVObject obj : data) {
                if (objectIds.contains(obj.getObjectId())) {
                    continue;
                }
                dataSource.add(obj);
            }
            notifyDataSetChanged();
        }

        public void addRefreshData(List<AVObject> data) {
            for (AVObject obj : data) {
                if (objectIds.contains(obj.getObjectId())) {
                    continue;
                }
                dataSource.add(0, obj);
            }
            notifyDataSetChanged();
        }

        public void addData(AVObject data) {
            if (objectIds.contains(data.getObjectId())) {
                return;
            }
            dataSource.add(0, data);
            notifyDataSetChanged();
            addObjects(data.getObjectId());
        }

        private void addObjects(String key) {
            objectIds.add(key);
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
            final AVObject post = (AVObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_community_item, null);
                holder = new Holder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.content = (EmojiTextView) convertView.findViewById(R.id.content);
                holder.imgGrid = (MarkerImageGridView) convertView.findViewById(R.id.imgGrid);
                holder.forwardContainer = (LinearLayout) convertView.findViewById(R.id.forwardContainer);
                holder.forward = (TextView) convertView.findViewById(R.id.forward);
                holder.commentContainer = (LinearLayout) convertView.findViewById(R.id.commentContainer);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.likeContainer = (LinearLayout) convertView.findViewById(R.id.likeContainer);
                holder.like = (TextView) convertView.findViewById(R.id.like);
                convertView.setTag(holder);

                holder.forwardContainer.setOnClickListener(this);
                holder.commentContainer.setOnClickListener(this);
                holder.likeContainer.setOnClickListener(this);
                convertView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Holder holder = (Holder) v.getTag();
                        Intent commentIntent = new Intent(getContext(), CommunityComment.class);
                        AVObject post = (AVObject) getItem(holder.position);
                        commentIntent.putExtra("objectId", post.getObjectId());
                        getContext().startActivity(commentIntent);
                    }
                });
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.position = position;

            AVUser user = post.getAVUser("user");
            AVFile avatarImg = user.getAVFile("avatar");
            if (avatarImg == null) {
                holder.avatar.setImageResource(R.drawable.default_avatar);
            } else {
                ImageLoader.getInstance().displayImage(avatarImg.getUrl(), holder.avatar, HttpUtil.DefaultOptions);
            }
            holder.username.setText(user.getUsername());
            holder.time.setText(DateUtil.getShortTimeDesc(post.getCreatedAt()));
            holder.content.setEmojiText(post.getString("text"));
            holder.imgGrid.setDataSource(post.getList("images"));
            final Object[] args = new Object[]{
                    post, holder.forward, holder.comment, holder.like
            };
            holder.forwardContainer.setTag(args);
            holder.commentContainer.setTag(args);
            holder.likeContainer.setTag(args);

            LikeUtil.countQuery(holder.like, post.getObjectId(), post);
            if (post.has("commentCount")) {
                holder.comment.setText(post.getInt("commentCount") + "");
            } else {
                AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityComment");
                query.whereEqualTo("attachObj", post);
                query.countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, AVException e) {
                        TextView tv = (TextView) (args[2]);
                        tv.setText(i + "");
                        post.put("commentCount", i);
                    }
                });
            }
            return convertView;
        }

        @Override
        public void onClick(View v) {
            Object[] args = (Object[]) v.getTag();
            final AVObject post = (AVObject) args[0];
            final TextView forward = (TextView) args[1];
            final TextView comment = (TextView) args[2];
            final TextView like = (TextView) args[3];
            final AVUser user = AVUser.getCurrentUser();
            switch (v.getId()) {
                case R.id.forwardContainer:

                    break;
                case R.id.commentContainer:
                    Intent commentIntent = new Intent(getContext(), CommunityComment.class);
                    commentIntent.putExtra("objectId", post.getObjectId());
                    getContext().startActivity(commentIntent);
                    break;
                case R.id.likeContainer:
                    LikeUtil.addLike(v, like, post.getObjectId(), 2, getContext());
                    break;
            }
        }
    }

    private class Holder {
        public ImageView avatar;
        public TextView username;
        public TextView time;
        public EmojiTextView content;
        public MarkerImageGridView imgGrid;
        public LinearLayout forwardContainer;
        public TextView forward;
        private LinearLayout commentContainer;
        private TextView comment;
        private LinearLayout likeContainer;
        private TextView like;
        public int position;
    }
}
