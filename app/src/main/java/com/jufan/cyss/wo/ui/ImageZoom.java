package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.wo.ui.view.LoadingDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by cyjss on 2015/1/9.
 */
public class ImageZoom extends BaseUNIActivity {

    @BindView(id = R.id.image)
    private ImageViewTouch image;
    private LoadingDialog dialog;
    private int reloadTimes = 0;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_image_zoom);
    }

    @Override
    protected void initWidget() {
        super.setupActionBar("违章图片", ActionBarType.BACK);
        this.dialog = new LoadingDialog(this);
        Intent i = getIntent();
        String xh = i.getStringExtra("xh");
        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        showImage(xh);
    }

    private void showImage(final String xh) {

        String url = HttpUtil.getReqVioImgUrl(xh);
        ImageLoader.getInstance().loadImage(url, new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .build(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                String urlStr = HttpUtil.getReqVioThumbUrl(xh);
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(urlStr);
                image.setImageBitmap(bitmap, null, -1, 8f);
                dialog.show();
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                dialog.dismiss();
                Log.e("", "===>", failReason.getCause().fillInStackTrace());
                ImageLoader.getInstance().clearMemoryCache();
                System.gc();
                reloadTimes++;
                if (reloadTimes > 2) {
                    ViewInject.longToast("加载图片错误，请稍后再试");
                } else {
                    showImage(xh);
                }
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                Log.d("", "--->" + bitmap.getWidth() + "," + bitmap.getHeight());
                image.setImageBitmap(bitmap, null, -1, 8f);
                dialog.dismiss();
                reloadTimes = 0;
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                dialog.dismiss();
            }
        });
    }
}
