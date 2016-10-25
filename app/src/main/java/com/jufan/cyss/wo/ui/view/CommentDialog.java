package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.http.UMPushHttp;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.CommunityComment;
import com.jufan.cyss.wo.ui.Login;
import com.jufan.cyss.wo.ui.MainActivity;
import com.jufan.cyss.wo.ui.R;
import com.jufan.cyss.wo.ui.RoadMapFragment;
import com.telly.groundy.Groundy;
import com.umeng.message.UmengRegistrar;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;
import org.w3c.dom.ProcessingInstruction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cyjss on 2015/3/7.
 */
public class CommentDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener {

    private EmojiEditText commentMsg;
    private ImageView emojiBtn;
    private Button cancelBtn;
    private Button sendBtn;
    private EmojiGridView emojiGridView;


    private AVObject attachObj;

    private boolean isShowEmoji = false;

    private View.OnClickListener clickListener;

    public CommentDialog(Context context, AVObject attachObj, View.OnClickListener clickListener) {
        super(context, R.style.selectDataDialog);
        setContentView(R.layout.dialog_comment);

        this.commentMsg = (EmojiEditText) findViewById(R.id.commentMsg);
        this.emojiBtn = (ImageView) findViewById(R.id.emojiBtn);
        this.cancelBtn = (Button) findViewById(R.id.cancelBtn);
        this.sendBtn = (Button) findViewById(R.id.sendBtn);
        this.emojiGridView = (EmojiGridView) findViewById(R.id.emojiGrid);

        this.emojiBtn.setOnClickListener(this);
        this.cancelBtn.setOnClickListener(this);
        this.sendBtn.setOnClickListener(this);
        this.emojiGridView.setOnItemClickListener(this);
        this.attachObj = attachObj;
        this.clickListener = clickListener;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.emojiBtn:
                if (isShowEmoji) {
                    emojiGridView.setVisibility(View.GONE);
                } else {
                    emojiGridView.setVisibility(View.VISIBLE);
                }
                isShowEmoji = !isShowEmoji;
                break;
            case R.id.cancelBtn:
                dismiss();
                break;
            case R.id.sendBtn:
                if (!GlobalUtil.isLogin()) {
                    Intent i = new Intent(getContext(), Login.class);
                    getContext().startActivity(i);
                    return;
                }
                String text = commentMsg.getText().toString();
                if (StringUtils.isEmpty(text)) {
                    commentMsg.setError("内容不可为空");
                    break;
                }
                sendBtn.setEnabled(false);
                AVUser user = AVUser.getCurrentUser();

                //地图评论
                if ("MapMarker".equals(attachObj.getClassName()) || "MapMarkerComment".equals(attachObj.getClassName())) {
                    final AVObject mapMarkerComment = new AVObject("MapMarkerComment");
                    mapMarkerComment.put("user", user);
                    mapMarkerComment.put("text", text);
                    if ("MapMarker".equals(attachObj.getClassName())) {
                        mapMarkerComment.put("attachObj", this.attachObj);
                    } else if ("MapMarkerComment".equals(attachObj.getClassName())) {
                        mapMarkerComment.put("attachComment", this.attachObj);
                        mapMarkerComment.put("attachObj", this.attachObj.getAVObject("attachObj"));
                    } else {
                        ViewInject.longToast("评论出错，请重新再试");
                        return;
                    }
                    mapMarkerComment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            sendBtn.setEnabled(true);
                            if (e == null) {
                                if (clickListener != null) {
                                    v.setTag(mapMarkerComment);
                                    clickListener.onClick(v);
                                }
                                //推送
                                AVUser attachUser = attachObj.getAVUser("user");
                                String deviceToken = attachUser.getString("deviceToken");
                                Log.d("CommentDialog", "comment attach deviceToken:" + deviceToken);
                                if (!StringUtils.isEmpty(deviceToken)) {
                                    JSONObject sendObj = UMPushHttp.getCustomObj(MainActivity.class.getSimpleName(), null, UMPushHttp.SHOW_IN_NOTIFICATION);
                                    try {
                                        sendObj.put("text", "有人评论了你的话题");
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                    Groundy.create(UMPushHttp.class).arg("type", "unicast").arg("json", sendObj.toString()).arg("device_tokens", deviceToken).queueUsing(getContext());
                                }
                                dismiss();
                            } else {
                                Log.e("", "", e);
                                GlobalUtil.showNetworkError();
                            }
                        }
                    });
                } else if ("CommunityComment".equals(attachObj.getClassName())) {
                    final AVObject communityComment = new AVObject("CommunityComment");
                    communityComment.put("user", user);
                    communityComment.put("text", text);
                    communityComment.put("attachComment", this.attachObj);
                    communityComment.put("attachObj", this.attachObj.getAVObject("attachObj"));
                    communityComment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            sendBtn.setEnabled(true);
                            if (e == null) {
                                AVUser attachUser = attachObj.getAVUser("user");
                                String deviceToken = attachUser.getString("deviceToken");
                                Log.d("CommentDialog", "comment attach deviceToken:" + deviceToken);
                                if (!StringUtils.isEmpty(deviceToken)) {
                                    Map<String, String> args = new HashMap<String, String>();
                                    args.put("objectId", attachObj.getAVObject("attachObj").getObjectId());
                                    JSONObject sendObj = UMPushHttp.getCustomObj(CommunityComment.class.getSimpleName(), null, UMPushHttp.SHOW_IN_NOTIFICATION, args);
                                    try {
                                        sendObj.put("text", "有人评论了你的话题");
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                    Groundy.create(UMPushHttp.class).arg("type", "unicast").arg("json", sendObj.toString()).arg("device_tokens", deviceToken).queueUsing(getContext());
                                }
                                if (clickListener != null) {
                                    v.setTag(communityComment);
                                    clickListener.onClick(v);
                                }
                                dismiss();
                            } else {
                                Log.e("", "", e);
                                GlobalUtil.showNetworkError();
                            }
                        }
                    });
                } else if ("EyesComment".equals(attachObj.getClassName())) {
                    final AVObject eyesComment = new AVObject("EyesComment");
                    eyesComment.put("user", user);
                    eyesComment.put("text", text);
                    eyesComment.put("attachComment", this.attachObj);
                    eyesComment.put("attachKey", this.attachObj.getString("attachKey"));
                    eyesComment.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            sendBtn.setEnabled(true);
                            if (e == null) {
                                if (clickListener != null) {
                                    v.setTag(eyesComment);
                                    clickListener.onClick(v);
                                }
                                dismiss();
                            } else {
                                Log.e("", "", e);
                                GlobalUtil.showNetworkError();
                            }
                        }
                    });
                }
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String emoji = emojiGridView.getEmojiName(position);
        this.commentMsg.appendEmoji(emoji);
    }
}
