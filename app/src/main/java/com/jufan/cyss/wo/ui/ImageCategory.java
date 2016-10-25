package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.wo.ui.view.LoadingDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.kymjs.aframe.ui.BindView;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by cyjss on 2015/3/11.
 */
public class ImageCategory extends BaseUNIActivity implements ViewPager.OnPageChangeListener {
    @BindView(id = R.id.imagePager)
    private ViewPager pager;
    @BindView(id = R.id.pageTip)
    private TextView pageTip;
    private ImageCategoryPagerAdapter pagerAdapter;

    private LoadingDialog dialog;
    private String[] urls;
    private String[] thumbUrls;

    private int position = 0;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_image_category);
    }

    @Override
    protected void initWidget() {
        super.setupActionBar("话题图片", ActionBarType.BACK);
        this.dialog = new LoadingDialog(this);
        Intent i = getIntent();
        urls = i.getStringArrayExtra("urls");
        thumbUrls = i.getStringArrayExtra("thumb_urls");
        position = i.getIntExtra("position", 0);
        this.pagerAdapter = new ImageCategoryPagerAdapter();
        for (String url : urls) {
            this.pagerAdapter.addView(View.inflate(this, R.layout.page_image_category, null));
        }
        pager.setAdapter(this.pagerAdapter);
        pager.setOnPageChangeListener(this);
        pageTip.setText((1) + "/" + urls.length);
        pager.setCurrentItem(position);
        dialog = new LoadingDialog(this);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        pageTip.setText((position + 1) + "/" + urls.length);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class ImageCategoryPagerAdapter extends PagerAdapter {
        private List<View> viewList;

        private int pageNum = 1;

        public ImageCategoryPagerAdapter() {
            this.viewList = new ArrayList<View>();
        }

        public void addView(View v) {
            this.viewList.add(v);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(viewList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View v = viewList.get(position);
            container.addView(v);
            final ImageViewTouch image = (ImageViewTouch) v.findViewById(R.id.image);
            image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
            ImageLoader.getInstance().loadImage(urls[position], HttpUtil.DefaultOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    Bitmap bitmap = ImageLoader.getInstance().loadImageSync(thumbUrls[position]);
                    image.setImageBitmap(bitmap, null, -1, 8f);
                    dialog.show();
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    dialog.dismiss();
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    image.setImageBitmap(bitmap, null, -1, 8f);
                    dialog.dismiss();
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    dialog.dismiss();
                }
            });
            return v;
        }
    }
}