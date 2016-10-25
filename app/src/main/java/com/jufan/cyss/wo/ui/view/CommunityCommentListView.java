package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.LikeUtil;
import com.jufan.cyss.wo.ui.CommunityComment;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by cyjss on 2015/3/16.
 */
public class CommunityCommentListView extends KJListView implements KJRefreshListener {

    private CommunityComment communityComment;
    private String attachId;

    private int page = 0;
    private int pageNum = 20;
    private AVObject post;
    private Date refreshTime = new Date();

    private CommunityCommentAdapter adapter;


    public CommunityCommentListView(Context context) {
        super(context);
    }

    public CommunityCommentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommunityCommentListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(final CommunityComment communityComment, String attachId) {
        this.communityComment = communityComment;
        this.attachId = attachId;
        this.communityComment.showLoading();
        setOnRefreshListener(this);
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityPost");
        query.whereEqualTo("objectId", attachId);
        query.include("user");
        query.getFirstInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (e == null) {
                    if (avObject == null) {
                        ViewInject.longToast("该话题已被删除");
                        communityComment.finish();
                    } else {
                        adapter = new CommunityCommentAdapter();
                        post = avObject;
                        adapter.initPost(avObject);
                        setAdapter(adapter);
                        loadMore();
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    public AVObject getPost() {
        return post;
    }

    private void loadMore() {
        this.communityComment.showLoading();
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityComment");
        query.whereEqualTo("attachObj", post);
        query.orderByDescending("createdAt");
        query.include("user");
        query.include("attachComment");
        query.limit(pageNum);
        query.skip(page * pageNum);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                communityComment.hideLoading();
                stopLoadMore();
                if (e == null) {
                    if (page == 0) {
                        if (list.isEmpty()) {
                            adapter.setIsCommentEmpty(true);
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                        } else {
                            adapter.setIsCommentEmpty(false);
                            if (list.size() < pageNum) {
                                setPullLoadEnable(false);
                                getFooterView().setVisibility(GONE);
                            } else {
                                setPullLoadEnable(true);
                            }
                            adapter.appendMoreData(list);
                            page++;
                        }
                    } else {
                        if (list.isEmpty()) {
                            ViewInject.longToast("没有更多数据");
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                        } else {
                            adapter.appendMoreData(list);
                            page++;
                        }
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    public void addMyComment(AVObject comment) {
        adapter.addMyComment(comment);
    }

    @Override
    public void onRefresh() {
        communityComment.showLoading();
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityComment");
        query.whereEqualTo("attachObj", post);
        query.orderByDescending("createdAt");
        query.include("user");
        query.include("attachComment");
        query.whereGreaterThan("createdAt", refreshTime);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                communityComment.hideLoading();
                stopRefreshData();
                if (e == null) {
                    refreshTime = new Date();
                    if (avObjects.isEmpty()) {
                        ViewInject.longToast("没有最新数据");
                    } else {
                        adapter.refreshNewComment(avObjects);
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
        stopRefreshData();
    }

    @Override
    public void onLoadMore() {
        loadMore();
    }

    private class CommunityCommentAdapter extends BaseAdapter implements OnClickListener {

        private List<AVObject> dataSource = new LinkedList<AVObject>();
        private Set<String> filter = new HashSet<String>();
        private boolean isEmpty = true;

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

        public void initPost(AVObject post) {
            dataSource = new LinkedList<AVObject>();
            dataSource.add(post);
        }

        public void appendMoreData(List<AVObject> data) {
            for (AVObject item : data) {
                if (filter.contains(item.getObjectId())) {
                } else {
                    filter.add(item.getObjectId());
                    this.dataSource.add(item);
                }
            }
            notifyDataSetChanged();
        }

        public void addMyComment(AVObject comment) {
            setIsCommentEmpty(false);
            if (!filter.contains(comment.getObjectId())) {
                filter.add(comment.getObjectId());
                this.dataSource.add(1, comment);
                notifyDataSetChanged();
            }
            Log.d("-=->", "SIZE:" + getCount());
        }

        public void refreshNewComment(List<AVObject> data) {
            List<AVObject> tempList = new ArrayList<AVObject>();
            for (AVObject item : data) {
                if (filter.contains(item.getObjectId())) {
                } else {
                    filter.add(item.getObjectId());
                    tempList.add(item);
                }
            }
            this.dataSource.addAll(1, tempList);
            notifyDataSetChanged();
        }

        public void setIsCommentEmpty(boolean flag) {
            this.isEmpty = flag;
            if (flag) {
                if (getCount() == 1) {
                    AVObject obj = new AVObject();
                    obj.put("isEmpty", true);
                    dataSource.add(obj);
                }
            } else {
                if (getCount() > 1) {
                    AVObject obj = (AVObject) getItem(1);
                    if (obj.containsKey("isEmpty")) {
                        dataSource.remove(1);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //should use @Override getItemViewType, but like this ,it's code simple..
            if (position == 0) {
                AVObject post = (AVObject) getItem(position);
                convertView = View.inflate(getContext(), R.layout.list_community_post_item, null);
                ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
                TextView username = (TextView) convertView.findViewById(R.id.username);
                TextView time = (TextView) convertView.findViewById(R.id.time);
                EmojiTextView content = (EmojiTextView) convertView.findViewById(R.id.content);
                MarkerImageGridView imgGrid = (MarkerImageGridView) convertView.findViewById(R.id.imgGrid);
                AVUser user = post.getAVUser("user");
                username.setText(user.getUsername());
                time.setText(DateUtil.detailDateStr(post.getCreatedAt()));
                content.setEmojiText(post.getString("text"));

                if (user.getAVFile("avatar") == null) {
                    avatar.setImageResource(R.drawable.default_avatar);
                } else {
                    ImageLoader.getInstance().displayImage(user.getAVFile("avatar").getUrl(), avatar, HttpUtil.DefaultOptions);
                }
                imgGrid.setDataSource(post.getList("images"));
            } else {
                if (isEmpty) {
                    convertView = View.inflate(getContext(), R.layout.list_no_data_item, null);
                } else {
                    AVObject comment = (AVObject) getItem(position);
                    convertView = View.inflate(getContext(), R.layout.list_community_comment_item, null);
                    ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
                    TextView username = (TextView) convertView.findViewById(R.id.username);
                    TextView time = (TextView) convertView.findViewById(R.id.time);
                    EmojiTextView commentText = (EmojiTextView) convertView.findViewById(R.id.commentText);
                    ImageView likeIcon = (ImageView) convertView.findViewById(R.id.likeIcon);
                    TextView likeCount = (TextView) convertView.findViewById(R.id.likeCount);
                    ImageView commentIcon = (ImageView) convertView.findViewById(R.id.commentIcon);
                    RelativeLayout forwardContainer = (RelativeLayout) convertView.findViewById(R.id.forwardContainer);
                    final ImageView forwardAvatar = (ImageView) convertView.findViewById(R.id.forwardAvatar);
                    final TextView forwardUsername = (TextView) convertView.findViewById(R.id.forwardUsername);
                    TextView forwardTime = (TextView) convertView.findViewById(R.id.forwardTime);
                    EmojiTextView forwardText = (EmojiTextView) convertView.findViewById(R.id.forwardText);
                    LikeUtil.countQuery(likeCount, comment.getObjectId(), comment);
                    Object[] args = new Object[]{
                            comment, likeCount, likeIcon
                    };
                    likeIcon.setTag(args);
                    commentIcon.setTag(args);
                    likeIcon.setOnClickListener(this);
                    commentIcon.setOnClickListener(this);

                    AVUser user = comment.getAVUser("user");
                    if (user == null) {
                        user = AVUser.getCurrentUser();
                    }
                    if (user.getAVFile("avatar") == null) {
                        avatar.setImageResource(R.drawable.default_avatar);
                    } else {
                        ImageLoader.getInstance().displayImage(user.getAVFile("avatar").getUrl(), avatar, HttpUtil.DefaultOptions);
                    }
                    username.setText(user.getUsername());
                    Date createDate = new Date();
                    if (comment.getCreatedAt() != null) {
                        createDate = comment.getCreatedAt();
                    }
                    time.setText(DateUtil.getShortTimeDesc(createDate));
                    commentText.setEmojiText(comment.getString("text"));
                    AVObject attachComment = comment.getAVObject("attachComment");
                    if (attachComment == null) {
                        forwardContainer.setVisibility(GONE);
                    } else {
                        forwardContainer.setVisibility(VISIBLE);
                        AVUser forwardUser = attachComment.getAVUser("user");
                        forwardUser.fetchIfNeededInBackground(new GetCallback<AVObject>() {
                            @Override
                            public void done(AVObject avObject, AVException e) {
                                AVUser user = (AVUser) avObject;
                                if (e == null) {
                                    AVFile avatar = user.getAVFile("avatar");
                                    if (avatar == null) {
                                        forwardAvatar.setImageResource(R.drawable.default_avatar);
                                    } else {
                                        ImageLoader.getInstance().displayImage(avatar.getUrl(), forwardAvatar, HttpUtil.DefaultOptions);
                                    }
                                    forwardUsername.setText(user.getUsername());
                                } else {
                                    GlobalUtil.showNetworkError();
                                }
                            }
                        });
                        forwardTime.setText(DateUtil.getShortTimeDesc(attachComment.getCreatedAt()));
                        forwardText.setEmojiText(attachComment.getString("text"));
                    }
                }
            }
            return convertView;
        }

        @Override
        public void onClick(View v) {
            Object[] args = (Object[]) v.getTag();
            final AVObject comment = (AVObject) args[0];
            final TextView likeCount = (TextView) args[1];
            final ImageView likeIcon = (ImageView) args[2];
            switch (v.getId()) {
                case R.id.likeIcon:
                    LikeUtil.addLike(likeIcon, likeCount, comment.getObjectId(), 3, getContext());
                    break;
                case R.id.commentIcon:
                    CommentDialog dialog = new CommentDialog(getContext(), comment, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AVObject comment = (AVObject) v.getTag();
                            addMyComment(comment);
                        }
                    });
                    dialog.show();
                    break;
            }
        }
    }
}
