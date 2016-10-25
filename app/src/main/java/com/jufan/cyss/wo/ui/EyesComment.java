package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.LikeUtil;
import com.jufan.cyss.wo.ui.view.CommentDialog;
import com.jufan.cyss.wo.ui.view.EmojiEditText;
import com.jufan.cyss.wo.ui.view.EmojiGridView;
import com.jufan.cyss.wo.ui.view.EmojiTextView;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cyjss on 2015/4/2.
 */
public class EyesComment extends BaseUNIActivity implements KJRefreshListener, AdapterView.OnItemClickListener {
    private JSONObject eyeJson;
    private String lddm;
    @BindView(id = R.id.eyesCommentList)
    private KJListView eyesCommentList;
    @BindView(id = R.id.address)
    private TextView address;

    @BindView(id = R.id.sendCommentContainer)
    private RelativeLayout sendCommentContainer;
    @BindView(id = R.id.sendBtn, click = true)
    private Button sendBtn;
    @BindView(id = R.id.emojiBtn, click = true)
    private ImageView emojiBtn;
    @Required(order = 0, message = "评论不可为空")
    @BindView(id = R.id.commentText)
    private EmojiEditText commentText;
    @BindView(id = R.id.emojiGrid)
    private EmojiGridView emojiGrid;

    private EyesCommentAdapter adapter;
    private int page = 0;
    private int pageNum = 20;
    private Date refreshDate = new Date();

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_eyes_comment);
    }

    @Override
    protected void initWidget() {
        setupActionBar("电子眼吐槽", ActionBarType.BACK);
        Intent intent = getIntent();
        String jsonStr = intent.getStringExtra("json");
        try {
            this.eyeJson = new JSONObject(jsonStr);
            this.lddm = eyeJson.getString("lddm") + eyeJson.getString("dldm");
            this.address.setText(eyeJson.getString("address"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eyesCommentList.setOnRefreshListener(this);
        eyesCommentList.setPullLoadEnable(false);
        eyesCommentList.getFooterView().setVisibility(View.GONE);
        emojiGrid.setOnItemClickListener(this);
        loadMore();
    }

    private void refresh() {

        AVQuery<AVObject> query = new AVQuery<AVObject>("EyesComment");
        query.whereEqualTo("attachKey", this.lddm);
        query.whereGreaterThan("createdAt", refreshDate);
        query.include("attachComment");
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                eyesCommentList.stopRefreshData();
                hideLoading();
                if (e == null) {
                    if (!avObjects.isEmpty()) {
                        if (adapter == null) {
                            adapter = new EyesCommentAdapter();
                            eyesCommentList.setAdapter(adapter);
                        }
                        adapter.addRefreshData(avObjects);
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    private void loadMore() {
        showLoading();
        AVQuery<AVObject> query = new AVQuery<AVObject>("EyesComment");
        query.whereEqualTo("attachKey", this.lddm);
        query.skip(page * pageNum);
        query.limit(pageNum);
        query.include("attachComment");
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                hideLoading();
                eyesCommentList.stopLoadMore();
                if (e == null) {
                    if (avObjects.isEmpty()) {
                        eyesCommentList.setAdapter(new NoDataAdapter(EyesComment.this, "还没有任何吐槽...说几句来警醒后人"));
                    } else {
                        if (avObjects.size() < pageNum) {
                            eyesCommentList.setPullLoadEnable(false);
                            eyesCommentList.getFooterView().setVisibility(View.GONE);
                        } else {
                            eyesCommentList.setPullLoadEnable(true);
                            eyesCommentList.getFooterView().setVisibility(View.VISIBLE);
                        }
                        if (adapter == null) {
                            adapter = new EyesCommentAdapter();
                            eyesCommentList.setAdapter(adapter);
                        }
                        adapter.addMoreComment(avObjects);
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
                page++;
            }
        });
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onLoadMore() {
        loadMore();
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.emojiBtn:
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) sendCommentContainer.getLayoutParams();
                if (emojiGrid.getVisibility() == View.VISIBLE) {
                    emojiGrid.setVisibility(View.GONE);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                } else {
                    emojiGrid.setVisibility(View.VISIBLE);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                }
                break;
            case R.id.sendBtn:
                validate();
                break;
        }
    }

    @Override
    public void onValidationSucceeded() {
        if (GlobalUtil.isLogin()) {
            showLoading();
            sendBtn.setEnabled(false);
            final AVObject comment = new AVObject("EyesComment");
            comment.put("attachKey", lddm);
            comment.put("text", commentText.getText().toString());
            comment.put("user", AVUser.getCurrentUser());
            comment.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    hideLoading();
                    sendBtn.setEnabled(true);
                    if (e == null) {
                        if (adapter == null) {
                            adapter = new EyesCommentAdapter();
                            eyesCommentList.setAdapter(adapter);
                        }
                        adapter.addMyComment(comment);
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) sendCommentContainer.getLayoutParams();
                        emojiGrid.setVisibility(View.GONE);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        commentText.setText("");
                    } else {
                        GlobalUtil.showNetworkError();
                    }
                }
            });
        } else {
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        commentText.appendEmoji(emojiGrid.getEmojiName(position));
    }

    private class EyesCommentAdapter extends BaseAdapter implements View.OnClickListener {

        private List<AVObject> dataSource = new ArrayList<AVObject>();
        private Set<String> filter = new HashSet<String>();

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

        public void addMyComment(AVObject comment) {
            if (filter.contains(comment.getObjectId())) {
                return;
            }
            filter.add(comment.getObjectId());
            dataSource.add(0, comment);
            notifyDataSetChanged();
        }

        public void addMoreComment(List<AVObject> data) {
            for (AVObject item : data) {
                if (filter.contains(item.getObjectId())) {
                    continue;
                }
                filter.add(item.getObjectId());
                dataSource.add(item);
            }
            notifyDataSetChanged();
        }

        public void addRefreshData(List<AVObject> data) {
            for (AVObject item : data) {
                if (filter.contains(item.getObjectId())) {
                    continue;
                }
                filter.add(item.getObjectId());
                dataSource.add(0, item);
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(EyesComment.this, R.layout.list_community_comment_item, null);
            }
            AVObject comment = (AVObject) getItem(position);
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
                createDate = new Date();
            }
            time.setText(DateUtil.getShortTimeDesc(createDate));
            commentText.setEmojiText(comment.getString("text"));
            AVObject attachComment = comment.getAVObject("attachComment");
            if (attachComment == null) {
                forwardContainer.setVisibility(View.GONE);
            } else {
                forwardContainer.setVisibility(View.VISIBLE);
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
                    LikeUtil.addLike(likeIcon, likeCount, comment.getObjectId(), 4, EyesComment.this);
                    break;
                case R.id.commentIcon:
                    CommentDialog dialog = new CommentDialog(EyesComment.this, comment, new View.OnClickListener() {
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
