package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.UMPushHttp;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.view.CommunityCommentListView;
import com.jufan.cyss.wo.ui.view.EmojiEditText;
import com.jufan.cyss.wo.ui.view.EmojiGridView;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.telly.groundy.Groundy;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cyjss on 2015/3/16.
 */
public class CommunityComment extends BaseUNIActivity implements AdapterView.OnItemClickListener {

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

    @BindView(id = R.id.commentList)
    private CommunityCommentListView commentList;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_community_comment);
    }

    @Override
    protected void initWidget() {
        setupActionBar("话题", ActionBarType.BACK);
        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");
        commentList.initData(this, objectId);
        emojiGrid.setOnItemClickListener(this);
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
        final AVObject post = commentList.getPost();
        if (post == null) {
            ViewInject.longToast("正在加载信息，请稍后再发送");
        } else {
            if (GlobalUtil.isLogin()) {
                showLoading();
                sendBtn.setEnabled(false);
                final AVObject comment = new AVObject("CommunityComment");
                comment.put("attachObj", post);
                comment.put("text", commentText.getText().toString());
                comment.put("user", AVUser.getCurrentUser());
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        hideLoading();
                        sendBtn.setEnabled(true);
                        if (e == null) {
                            commentList.addMyComment(comment);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) sendCommentContainer.getLayoutParams();
                            emojiGrid.setVisibility(View.GONE);
                            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                            commentText.setText("");


                            AVUser attachUser = post.getAVUser("user");
                            String deviceToken = attachUser.getString("deviceToken");
                            if (!StringUtils.isEmpty(deviceToken)) {
                                Map<String, String> args = new HashMap<String, String>();
                                args.put("objectId", post.getObjectId());
                                JSONObject sendObj = UMPushHttp.getCustomObj(CommunityComment.class.getSimpleName(), null, UMPushHttp.SHOW_IN_NOTIFICATION, args);
                                try {
                                    sendObj.put("text", "有人评论了你的话题");
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                Groundy.create(UMPushHttp.class).arg("type", "unicast").arg("json", sendObj.toString()).arg("device_tokens", deviceToken).queueUsing(CommunityComment.this);
                            }
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
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        commentText.appendEmoji(emojiGrid.getEmojiName(position));
    }
}
