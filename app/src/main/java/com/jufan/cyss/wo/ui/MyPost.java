package com.jufan.cyss.wo.ui;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.wo.ui.view.MyCommunityPostListView;
import com.jufan.cyss.wo.ui.view.MyMapMarkerPostListView;

import org.kymjs.aframe.ui.BindView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyjss on 2015/3/19.
 */
public class MyPost extends BaseUNIActivity implements ViewPager.OnPageChangeListener {

    @BindView(id = R.id.myPostPager)
    private ViewPager myPostPager;

    @BindView(id = R.id.slide)
    private TextView slide;
    @BindView(id = R.id.mapMarker, click = true)
    private LinearLayout mapMarker;
    @BindView(id = R.id.communityPost, click = true)
    private LinearLayout communityPost;
    @BindView(id = R.id.mapMarkerTv)
    private TextView mapMarkerTv;
    @BindView(id = R.id.postTv)
    private TextView postTv;

    private int nowPage = 0;
    private TextView[] tabPager;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_my_post);
    }

    @Override
    protected void initWidget() {
        setupActionBar("我的话题", ActionBarType.BACK);
        List<View> views = new ArrayList<View>();
        views.add(View.inflate(this, R.layout.page_my_map_marker_post, null));
        views.add(View.inflate(this, R.layout.page_my_community_post, null));
        tabPager = new TextView[]{
                mapMarkerTv, postTv
        };
        myPostPager.setAdapter(new CommunityPagerAdapter(views));

        this.myPostPager.setOnPageChangeListener(this);

        ViewGroup.LayoutParams lp = slide.getLayoutParams();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = dm.widthPixels / tabPager.length;
        slide.setLayoutParams(lp);
    }

    @Override
    public void widgetClick(View v) {
        TextView tv = this.tabPager[nowPage];
        tv.setTextColor(0xff808080);
        switch (v.getId()) {
            case R.id.mapMarker:
                nowPage = 0;
                break;
            case R.id.communityPost:
                nowPage = 1;
                break;
        }
        this.tabPager[nowPage].setTextColor(0xffff9933);
        this.myPostPager.setCurrentItem(nowPage);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) slide.getLayoutParams();
        lp.leftMargin = lp.width * position + positionOffsetPixels / tabPager.length;
        slide.setLayoutParams(lp);
    }

    @Override
    public void onPageSelected(int position) {
        TextView tv = this.tabPager[nowPage];
        tv.setTextColor(0xff808080);
        this.tabPager[position].setTextColor(0xffff9933);
        nowPage = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class CommunityPagerAdapter extends PagerAdapter {

        private List<View> views;

        public CommunityPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            if (position == 0) {
                MyMapMarkerPostListView listView = (MyMapMarkerPostListView) v.findViewById(R.id.mapMarkerPostList);
                listView.initData(MyPost.this);
            } else if (position == 1) {
                MyCommunityPostListView myCommunityPostListView = (MyCommunityPostListView) v.findViewById(R.id.myCommunityPostList);
                myCommunityPostListView.initData(MyPost.this);
            }
            return v;
        }
    }
}
