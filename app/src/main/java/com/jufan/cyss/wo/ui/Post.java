package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.view.EmojiEditText;
import com.jufan.cyss.wo.ui.view.EmojiGridView;
import com.jufan.cyss.wo.ui.view.ImageShownGridView;

import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by cyjss on 2015/1/31.
 */
public class Post extends BaseUNIActivity implements AdapterView.OnItemClickListener {

    @BindView(id = R.id.postContent)
    private EmojiEditText postContent;
    @BindView(id = R.id.imgGrid)
    private ImageShownGridView imgGrid;
    @BindView(id = R.id.emojiGrid)
    private EmojiGridView emojiGrid;
    @BindView(id = R.id.pickImg, click = true)
    private ImageView pickImg;
    @BindView(id = R.id.camera, click = true)
    private ImageView camera;
    @BindView(id = R.id.emoji, click = true)
    private ImageView emoji;

    private boolean emojiShowFlag = false;


    public static final int REQUEST_POST_LOAD_IMAGE = 200;
    public static final int REQUEST_POST_CAPTURE_IMAGE = 300;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_post);
    }

    @Override
    protected void initWidget() {
        setupActionBar("发表话题", ActionBarType.BACK);
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText("发表");
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postContentStr = postContent.getText().toString();
                if (StringUtils.isEmpty(postContentStr)) {
                    postContent.setError("消息不可为空");
                    return;
                }
                if (!imgGrid.isUploadComplete()) {
                    ViewInject.longToast("请等待图片上传完成");
                    return;
                }
                showLoading();
                final AVUser user = AVUser.getCurrentUser();
                final AVObject communityPost = new AVObject("CommunityPost");
                communityPost.put("text", postContentStr);
                communityPost.put("user", user);
                communityPost.addAll("images", imgGrid.getImageFiles());
                communityPost.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        hideLoading();
                        if (e == null) {
                            Intent intent = new Intent();
                            intent.putExtra("objectId", communityPost.getObjectId());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            GlobalUtil.showNetworkError();
                        }
                    }
                });
            }
        });
        emojiGrid.setOnItemClickListener(this);
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.pickImg:
                if (imgGrid.isImageEnough()) {
                    ViewInject.longToast("最多只能上传6张照片");
                    break;
                }
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_POST_LOAD_IMAGE);
                break;
            case R.id.camera:
                if (imgGrid.isImageEnough()) {
                    ViewInject.longToast("最多只能上传6张照片");
                    break;
                }
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(openCameraIntent, REQUEST_POST_CAPTURE_IMAGE);
                break;
            case R.id.emoji:
                if (emojiShowFlag) {
                    emojiGrid.setVisibility(View.GONE);
                } else {
                    emojiGrid.setVisibility(View.VISIBLE);
                }
                emojiShowFlag = !emojiShowFlag;
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        postContent.appendEmoji(emojiGrid.getEmojiName(position));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getPackageName(), "resultCode===>" + resultCode);
        Log.d(getPackageName(), "requestCode===>" + requestCode);
        if (resultCode == RESULT_OK) {
            AVFile imgFile = null;
            if (REQUEST_POST_LOAD_IMAGE == requestCode) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                try {
                    imgFile = AVFile.withAbsoluteLocalPath("img.jpg", picturePath);
                } catch (IOException e) {
                    Log.e("", "", e);
                    e.printStackTrace();
                }
            } else if (REQUEST_POST_CAPTURE_IMAGE == requestCode) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imgFile = new AVFile("img.jpg", baos.toByteArray());
            }
            if (imgFile != null) {
                imgGrid.addImage(imgFile);
            }
        }
    }
}
