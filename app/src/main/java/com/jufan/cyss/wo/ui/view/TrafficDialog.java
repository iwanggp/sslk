package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amap.api.maps.model.LatLng;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SendCallback;
import com.jufan.cyss.frame.BaseUNIApplication;
import com.jufan.cyss.http.UMPushHttp;
import com.jufan.cyss.receiver.CustomReceiver;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.MainActivity;
import com.jufan.cyss.wo.ui.R;
import com.jufan.cyss.wo.ui.RoadMapFragment;
import com.telly.groundy.Groundy;
import com.umeng.message.UmengRegistrar;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.util.List;

/**
 * Created by cyjss on 2015/3/5.
 */
public class TrafficDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener {

    private LinearLayout traffic;
    private LinearLayout accident;
    private LinearLayout words;
    private LinearLayout police;
    private EmojiEditText msgContent;
    private ImageView pickImg;
    private ImageView emoji;
    private ImageView camera;
    private Button sendBtn;
    private Button cancelBtn;
    private EmojiGridView emojiGrid;
    private ImageShownGridView imageShownGridView;
    private RoadMapFragment roadMapFragment;
    private BaseUNIApplication application;

    private LinearLayout[] types;

    private boolean isShowEmojis = false;

    private int nowIndex = 0;

    private final String[] HINT = {
            "我这里比较拥堵，说点什么？",
            "我这里出现事故，有图有真相",
            "我在这里，嗨!",
            "我这里有交警，注意交通规则！"
    };

    public TrafficDialog(Context context, int index, RoadMapFragment roadMapFragment) {
        super(context, R.style.selectDialog);
        setContentView(R.layout.dialog_traffic);
        this.roadMapFragment = roadMapFragment;
        this.traffic = (LinearLayout) findViewById(R.id.traffic);
        this.accident = (LinearLayout) findViewById(R.id.accident);
        this.words = (LinearLayout) findViewById(R.id.words);
        this.police = (LinearLayout) findViewById(R.id.police);
        this.msgContent = (EmojiEditText) findViewById(R.id.msgContent);
        this.pickImg = (ImageView) findViewById(R.id.pickImg);
        this.camera = (ImageView) findViewById(R.id.camera);
        this.emoji = (ImageView) findViewById(R.id.emoji);
        this.emojiGrid = (EmojiGridView) findViewById(R.id.emojiGrid);
        this.imageShownGridView = (ImageShownGridView) findViewById(R.id.imgGrid);
        this.emojiGrid.setOnItemClickListener(this);
        this.sendBtn = (Button) findViewById(R.id.sendBtn);
        this.cancelBtn = (Button) findViewById(R.id.cancelBtn);
        types = new LinearLayout[]{
                traffic, accident, words, police
        };
        setCheckType(index);
        this.traffic.setOnClickListener(this);
        this.accident.setOnClickListener(this);
        this.words.setOnClickListener(this);
        this.police.setOnClickListener(this);
        this.emoji.setOnClickListener(this);
        this.sendBtn.setOnClickListener(this);
        this.cancelBtn.setOnClickListener(this);
        this.pickImg.setOnClickListener(this);
        this.camera.setOnClickListener(this);
        this.emoji.setOnClickListener(this);

        setCanceledOnTouchOutside(false);
        application = (BaseUNIApplication) roadMapFragment.getActivity().getApplication();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public void addImage(AVFile image) {
        this.imageShownGridView.addImage(image);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String emoji = emojiGrid.getEmojiName(position);
        msgContent.appendEmoji(emoji);
    }

    private void setCheckType(int index) {
        for (int i = 0; i < types.length; i++) {
            LinearLayout ll = types[i];
            if (i == index) {
                ll.setBackgroundColor(getContext().getResources().getColor(R.color.woklk_orange_4));
            } else {
                ll.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            }
        }
        this.msgContent.setHint(HINT[index]);
        this.nowIndex = index;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.traffic:
            case R.id.accident:
            case R.id.words:
            case R.id.police:
                setCheckType(Integer.parseInt(v.getTag().toString()));
                break;
            case R.id.pickImg:
                if (imageShownGridView.isImageEnough()) {
                    ViewInject.longToast("最多只能上传6张照片");
                    break;
                }
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                application.getMainActivity().startActivityForResult(i, MainActivity.REQUEST_TRAFFIC_LOAD_IMAGE);
                break;
            case R.id.camera:
                if (imageShownGridView.isImageEnough()) {
                    ViewInject.longToast("最多只能上传6张照片");
                    break;
                }
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                application.getMainActivity().startActivityForResult(openCameraIntent, MainActivity.REQUEST_TRAFFIC_CAPTURE_IMAGE);
                break;
            case R.id.emoji:
                if (isShowEmojis) {
                    this.emojiGrid.setVisibility(View.GONE);
                } else {
                    this.emojiGrid.setVisibility(View.VISIBLE);
                }
                isShowEmojis = !isShowEmojis;
                break;
            case R.id.sendBtn:
                LatLng ll = roadMapFragment.getMyLocation();
                if (ll == null) {
                    ViewInject.longToast("正在定位...");
                    roadMapFragment.reqMyLocation();
                    break;
                }
                if (StringUtils.isEmpty(msgContent.getText().toString())) {
//                    msgContent.setError("请填写内容");
//                    break;
                    msgContent.setText(HINT[nowIndex]);
                }
                if (!imageShownGridView.isUploadComplete()) {
                    ViewInject.longToast("请等待图片上传完成");
                    return;
                }
                sendBtn.setEnabled(false);
                final BaseUNIApplication app = (BaseUNIApplication) getContext().getApplicationContext();
                app.getMainActivity().showLoading();
                final AVObject mapMarker = new AVObject("MapMarker");
                AVUser user = AVUser.getCurrentUser();
                mapMarker.put("lat", ll.latitude);
                mapMarker.put("lon", ll.longitude);
                mapMarker.put("text", msgContent.getText().toString());
                mapMarker.put("type", nowIndex);
                mapMarker.put("userId", user.getObjectId());
                mapMarker.put("user", user);
                List<AVFile> images = imageShownGridView.getImageFiles();
                if (!images.isEmpty()) {
                    mapMarker.addAll("images", images);
                }

                mapMarker.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        app.getMainActivity().hideLoading();
                        sendBtn.setEnabled(true);
                        if (e == null) {
                            roadMapFragment.addMarkersToMap(nowIndex, mapMarker);
                            String deviceToken = UmengRegistrar.getRegistrationId(getContext());
                            JSONObject customObj = UMPushHttp.getCustomObj(application.getMainActivity(), roadMapFragment);
                            try {
                                customObj.put("objectId", mapMarker.getObjectId());
                                customObj.put("custom_action", 0);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
//                            Groundy.create(UMPushHttp.class).arg("type", "broadcast").arg("json", customObj.toString()).queueUsing(getContext());
                            Groundy.create(UMPushHttp.class).arg("type", "active").arg("json", customObj.toString()).queueUsing(getContext());
                            dismiss();
                        } else {
                            GlobalUtil.showNetworkError();
                        }
                    }
                });
                break;
            case R.id.cancelBtn:
                dismiss();
                break;
        }
    }
}
