package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.bean.User;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.util.SimpleStorageUtil;
import com.jufan.cyss.wo.ui.view.DataSelectDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.BindView;

import java.io.File;
import java.io.IOException;

/**
 * Created by cyjss on 2015/2/2.
 */
public class Profile extends BaseUNIActivity implements DataSelectDialog.OnDataSelectDialogItemSelectedListener {

    @BindView(id = R.id.phoneNum)
    private TextView phoneNum;
    @BindView(id = R.id.userRealName)
    private TextView userRealName;
    @BindView(id = R.id.gender, click = true)
    private TextView gender;
    @BindView(id = R.id.userAvatar, click = true)
    private ImageView userAvatar;

    private DataSelectDialog dialog;

    private DataSelectDialog genderDialog;

    private static final int RESULT_LOAD_IMAGE = 200;
    private static final int RESULT_CAPTURE_IMAGE = 300;
    private static final int RESULT_SET_AVATAR = 400;

    private Uri cameraImgPath;
    private Uri cropImgPath;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected void initWidget() {
        setupActionBar("我的信息", ActionBarType.BACK);
        final AVUser user = AVUser.getCurrentUser();
        phoneNum.setText(user.getMobilePhoneNumber());
        userRealName.setText(user.getUsername());
        this.dialog = new DataSelectDialog(this, R.array.img_pick);
        this.genderDialog = new DataSelectDialog(this, R.array.xb);
        this.genderDialog.setOnDataSelectDialogItemSelected(new DataSelectDialog.OnDataSelectDialogItemSelectedListener() {
            @Override
            public void selected(View v, int index, String txt) {
                if (!user.has("gender") || index != Integer.parseInt(user.get("gender").toString())) {
                    user.put("gender", index);
                    user.saveInBackground();
                    gender.setText(txt);
                    gender.setTag(index);
                }
                genderDialog.dismiss();
            }
        });
        this.dialog.setOnDataSelectDialogItemSelected(this);
        if (user.has("avatar")) {
            AVFile avatar = user.getAVFile("avatar");
            ImageLoader.getInstance().displayImage(avatar.getUrl(), userAvatar, HttpUtil.DefaultOptions);
        }
        if (user.has("gender")) {
            String genderStr = user.get("gender").toString();
            if ("0".equals(genderStr)) {
                gender.setText("男");
            } else {
                gender.setText("女");
            }
            gender.setTag(genderStr);
        }
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.userAvatar:
                this.dialog.show();
                break;
            case R.id.gender:
                this.genderDialog.show();
                break;
        }
    }

    @Override
    public void selected(View v, int index, String txt) {
        if (index == 0) {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } else {
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraImgPath = Uri.fromFile(getOutputMediaFile());
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImgPath);
            startActivityForResult(openCameraIntent, RESULT_CAPTURE_IMAGE);
        }
        this.dialog.dismiss();
    }

    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = null;
        try {
            // This location works best if you want the created images to be
            // shared
            // between applications and persist after your app has been
            // uninstalled.
            mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "woklk");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = System.currentTimeMillis() + "";
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        if (mediaFile == null) {
            File sdDir = null;
            boolean sdCardExist = Environment.getExternalStorageState()
                    .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
            if (sdCardExist) {
                sdDir = Environment.getExternalStorageDirectory();//获取跟目录
                File woDir = new File(sdDir, "woklk");
                if (!woDir.exists()) {
                    woDir.mkdir();
                }
                mediaFile = new File(woDir, "IMG_" + timeStamp + ".jpg");
            }
        }
        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_SET_AVATAR) {
                showLoading();
                final AVUser user = AVUser.getCurrentUser();
                try {
                    final AVFile avatar = AVFile.withAbsoluteLocalPath(user.getObjectId() + ".jpg", cropImgPath.getPath());
                    avatar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                user.put("avatar", avatar);
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e != null) {
                                            GlobalUtil.showNetworkError();
                                        } else {
                                            ImageLoader.getInstance().clearDiskCache();
                                            ImageLoader.getInstance().displayImage(avatar.getUrl(), userAvatar, HttpUtil.DefaultOptions);
                                        }
                                        hideLoading();
                                    }
                                });
                            } else {
                                GlobalUtil.showNetworkError();
                                hideLoading();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    hideLoading();
                }
                return;
            }
            String picturePath = null;
            if (requestCode == RESULT_LOAD_IMAGE && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
            } else if (requestCode == RESULT_CAPTURE_IMAGE) {
                picturePath = cameraImgPath.getPath();
            }
            if (picturePath != null)
                startPhotoZoom(picturePath);
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param path
     */
    private void startPhotoZoom(String path) {
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 120);
        intent.putExtra("outputY", 120);
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);
        cropImgPath = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImgPath);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, RESULT_SET_AVATAR);
    }

}
