package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.adapter.LoadingAdapter;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.LikeUtil;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;
import org.kymjs.aframe.utils.StringUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cyjss on 2015/3/7.
 */
public class MarkerCommentListView extends KJListView implements KJRefreshListener {

    private MarkerCommentAdapter adapter;
    private List<AVObject> dataSource;
    private Set<String> objectIdSet = new HashSet<String>();
    private AVObject attachObj;

    private int page = 0;
    private int pageNum = 3;
    private Date refreshDate = new Date();

    public MarkerCommentListView(Context context) {
        super(context);
        init();
    }

    public MarkerCommentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarkerCommentListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnRefreshListener(this);
        setPullLoadEnable(true);
        setPullRefreshEnable(true);
    }

    public void setMarkerId(AVObject attachObj) {
        if (this.attachObj == null) {
            this.attachObj = attachObj;
            setAdapter(new LoadingAdapter(getContext()));
            loadMore();
        }
    }

    private void loadMore() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarkerComment");
        query.whereEqualTo("attachObj", attachObj);
        query.orderByDescending("createdAt");
        query.include("user");
        query.include("attachComment");
        query.limit(pageNum);
        query.skip(page * pageNum);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopLoadMore();
                if (e == null) {
                    if (page == 0) {
                        ViewGroup.LayoutParams lp = getLayoutParams();
                        if (avObjects == null || avObjects.isEmpty()) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                            lp.height = 0;
                            setLayoutParams(lp);
                        } else if (avObjects.size() < pageNum) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                            lp.height = GlobalUtil.dip2px(getContext(), 220);
                            setLayoutParams(lp);
                        } else {
                            lp.height = GlobalUtil.dip2px(getContext(), 220);
                            setLayoutParams(lp);
                        }
                        dataSource = avObjects;
                        for (AVObject item : dataSource) {
                            objectIdSet.add(item.getObjectId());
                        }
                        adapter = new MarkerCommentAdapter();
                        setAdapter(adapter);
                    } else {
                        if (avObjects == null || avObjects.isEmpty()) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                        } else {
                            dataSource.addAll(avObjects);
                            adapter.notifyDataSetChanged();
                        }
                        for (AVObject item : avObjects) {
                            objectIdSet.add(item.getObjectId());
                        }
                    }
                    page++;
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    private void refreshData() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("MapMarkerComment");
        query.whereEqualTo("attachObj", attachObj);
        query.orderByDescending("createdAt");
        query.include("user");
        query.include("attachComment");
        query.whereGreaterThan("createdAt", refreshDate);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopRefreshData();
                refreshDate = new Date();
                if (e == null) {
                    for (AVObject item : avObjects) {
                        if (objectIdSet.contains(item.getObjectId())) {
                            continue;
                        } else {
                            dataSource.add(0, item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }


    public void appendComment(AVObject comment) {
        if (dataSource == null || adapter == null) {
            ViewInject.longToast("网络不稳定，请稍后再试");
        } else {
            dataSource.add(0, comment);
            adapter.notifyDataSetChanged();
            if (dataSource.size() == 1) {
                ViewGroup.LayoutParams lp = getLayoutParams();
                lp.height = GlobalUtil.dip2px(getContext(), 220);
                setLayoutParams(lp);
            }
            objectIdSet.add(comment.getObjectId());
        }
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public void onLoadMore() {
        loadMore();
    }

    private class MarkerCommentAdapter extends BaseAdapter {
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
            final AVObject obj = (AVObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_marker_comment_item, null);
                holder = new Holder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.msgContent = (EmojiTextView) convertView.findViewById(R.id.msgContent);
                holder.likeContainer = (LinearLayout) convertView.findViewById(R.id.likeContainer);
                holder.likeCount = (TextView) convertView.findViewById(R.id.likeCount);
                final TextView likeCountTemp = holder.likeCount;
                holder.commentBtn = (ImageView) convertView.findViewById(R.id.commentBtn);
                holder.forwardContainer = (LinearLayout) convertView.findViewById(R.id.forwardContainer);
                holder.forwardText = (EmojiTextView) convertView.findViewById(R.id.forwardText);
                holder.forwardAvatar = (ImageView) convertView.findViewById(R.id.forwardAvatar);
                holder.forwardUsername = (TextView) convertView.findViewById(R.id.forwardUsername);
                holder.likeContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeUtil.addLike(v, likeCountTemp, obj.getObjectId(), 1, getContext());
                    }
                });
                holder.commentBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AVObject obj = (AVObject) v.getTag();
                        Date attachDate = obj.getCreatedAt();
                        Date now = new Date();
                        now.setHours(now.getHours() - 1);
                        if (attachDate.getTime() < now.getTime()) {
                            ViewInject.longToast("该话题已关闭评论");
                            return;
                        }
                        CommentDialog cd = new CommentDialog(getContext(), obj, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AVObject markerComment = (AVObject) v.getTag();
                                ViewInject.longToast("评论成功");
                                appendComment(markerComment);
                            }
                        });
                        cd.show();
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            LikeUtil.countQuery(holder.likeCount, obj.getObjectId(), obj);

            AVObject attachComment = obj.getAVObject("attachComment");
            if (attachComment == null) {
                holder.forwardContainer.setVisibility(GONE);
            } else {
                holder.forwardContainer.setVisibility(VISIBLE);
                holder.forwardText.setEmojiText(attachComment.getString("text"));
                AVUser attachUser = attachComment.getAVUser("user");
                if (attachUser != null && attachUser.getAVFile("avatar") != null) {
                    ImageLoader.getInstance().displayImage(attachUser.getAVFile("avatar").getUrl(), holder.forwardAvatar, HttpUtil.DefaultOptions);
                    holder.forwardUsername.setText(attachUser.getUsername());
                } else {
                    final TextView forwardUsername = holder.forwardUsername;
                    final ImageView forwardAvatar = holder.forwardAvatar;
                    attachUser.fetchIfNeededInBackground(new GetCallback<AVObject>() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            if (e == null) {
                                AVUser user = (AVUser) avObject;
                                if (user.getAVFile("avatar") != null) {
                                    ImageLoader.getInstance().displayImage(user.getAVFile("avatar").getUrl(), forwardAvatar, HttpUtil.DefaultOptions);
                                }
                                forwardUsername.setText(user.getUsername());
                            } else {
                                Log.e("", "", e);
                            }
                        }
                    });
                }
            }
            AVUser commentUser = (AVUser) obj.get("user");
            holder.likeContainer.setTag(obj);
            holder.commentBtn.setTag(obj);
            holder.username.setText(commentUser.getUsername());
            holder.time.setText(DateUtil.getShortTimeDesc(obj.getCreatedAt()));
            holder.msgContent.setEmojiText(obj.getString("text"));
            if (commentUser.getAVFile("avatar") == null) {
                final ImageView avatar = holder.avatar;
                commentUser.fetchIfNeededInBackground(new GetCallback<AVObject>() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        if (e == null) {
                            AVUser user = (AVUser) avObject;
                            if (user.getAVFile("avatar") != null) {
                                ImageLoader.getInstance().displayImage(user.getAVFile("avatar").getUrl(), avatar, HttpUtil.DefaultOptions);
                            } else {
                                avatar.setImageResource(R.drawable.default_avatar);
                            }
                        } else {
                            Log.e("", "", e);
                            avatar.setImageResource(R.drawable.default_avatar);
                        }
                    }
                });
            } else {
                ImageLoader.getInstance().displayImage(commentUser.getAVFile("avatar").getUrl(), holder.avatar, HttpUtil.DefaultOptions);
            }
            return convertView;
        }
    }

    private class Holder {
        public ImageView avatar;
        public TextView username;
        public TextView time;
        public EmojiTextView msgContent;
        public LinearLayout likeContainer;
        public TextView likeCount;
        public ImageView commentBtn;
        public LinearLayout forwardContainer;
        public EmojiTextView forwardText;
        public ImageView forwardAvatar;
        public TextView forwardUsername;
    }
}
