package com.jufan.cyss.wo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.view.CommunityListView;
import com.jufan.cyss.wo.ui.view.CommunityNewsListView;

import org.json.JSONArray;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.widget.KJListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cyjss on 2015/1/29.
 */
public class CommunityFragment extends BaseUNIFragment implements ViewPager.OnPageChangeListener {

    @BindView(id = R.id.newsAround, click = true)
    private LinearLayout newsAround;
    @BindView(id = R.id.newsAroundTextView)
    private TextView newsAroundTextView;
    @BindView(id = R.id.news, click = true)
    private LinearLayout news;
    @BindView(id = R.id.newsTextView)
    private TextView newsTextView;
    @BindView(id = R.id.slide)
    private TextView slide;

    private CommunityListView communityListView;

    private TextView[] tabPager;
    private int selectIndex = 0;


    @BindView(id = R.id.communityPager)
    private ViewPager communityPager;
    private CommunityPagerAdapter communityPagerAdapter;

    @Override
    protected View inflaterView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_community, null);
    }

    @Override
    public void widgetResume() {
        application.getMainActivity().setupActionBar("社区", BaseUNIActivity.ActionBarType.COMMUNITY);
        Button addPostBtn = (Button) application.getMainActivity().findViewById(R.id.addPostBtn);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalUtil.isLogin()) {
                    Intent i = new Intent(getActivity(), Post.class);
                    startActivityForResult(i, MainActivity.REQUEST_COMMUNITY_ADD_POST);
                } else {
                    Intent i = new Intent(getActivity(), Login.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void initWidget(View parentView) {
        List<View> viewList = new ArrayList<View>();
        viewList.add(LayoutInflater.from(getActivity()).inflate(R.layout.pager_community_fragment, null));
        viewList.add(LayoutInflater.from(getActivity()).inflate(R.layout.page_community_news, null));
        communityPagerAdapter = new CommunityPagerAdapter(viewList);
        communityPager.setAdapter(communityPagerAdapter);
        this.tabPager = new TextView[]{
                newsAroundTextView, newsTextView
        };
        this.communityPager.setOnPageChangeListener(this);

        ViewGroup.LayoutParams lp = slide.getLayoutParams();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = dm.widthPixels / tabPager.length;
        slide.setLayoutParams(lp);
    }

    @Override
    public void widgetClick(View v) {
        TextView tv = this.tabPager[selectIndex];
        tv.setTextColor(0xff808080);
        switch (v.getId()) {
            case R.id.newsAround:
                selectIndex = 0;
                break;
            case R.id.news:
                selectIndex = 1;
                break;
        }
        this.tabPager[selectIndex].setTextColor(0xffff9933);
        this.communityPager.setCurrentItem(selectIndex);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) slide.getLayoutParams();
        lp.leftMargin = lp.width * position + positionOffsetPixels / tabPager.length;
        Log.d(getPageName(), "position:" + position + ",positionOffset:" + positionOffset + ",positionOffsetPixels:" + positionOffsetPixels + ",leftMargin:" + lp.leftMargin);
        slide.setLayoutParams(lp);
    }

    @Override
    public void onPageSelected(int position) {
        TextView tv = this.tabPager[selectIndex];
        tv.setTextColor(0xff808080);
        this.tabPager[position].setTextColor(0xffff9933);
        selectIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("", "=-=>onActivityResult");
        if (resultCode == getActivity().RESULT_OK) {
            if (MainActivity.REQUEST_COMMUNITY_ADD_POST == requestCode && data != null) {
                if (communityPager.getCurrentItem() != 0) {
                    communityPager.setCurrentItem(0);
                }
                String objectId = data.getStringExtra("objectId");
                AVQuery query = new AVQuery("CommunityPost");
                query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
                query.whereEqualTo("objectId", objectId);
                query.getFirstInBackground(new GetCallback() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        if (e == null) {
                            communityListView.addMyPost(avObject);
                        } else {
                            GlobalUtil.showNetworkError();
                        }
                    }
                });
//                AVObject post = AVObject.createWithoutData("CommunityPost", objectId);
//                application.getMainActivity().showLoading();
//                post.fetchIfNeededInBackground(new GetCallback<AVObject>() {
//                    @Override
//                    public void done(AVObject avObject, AVException e) {
//                        application.getMainActivity().hideLoading();
//                    }
//                });
            }
        }
    }

    private class CommunityPagerAdapter extends PagerAdapter {

        private List<View> viewList;

        public CommunityPagerAdapter(List<View> viewList) {
            this.viewList = viewList;
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
        public Object instantiateItem(ViewGroup container, int position) {
            View v = viewList.get(position);
            container.addView(v);
            if (position == 0) {
                communityListView = (CommunityListView) v.findViewById(R.id.communityList);
                application.getMainActivity().showLoading();
                communityListView.initData(CommunityFragment.this);
            } else if (position == 1) {
                CommunityNewsListView communityNewsListView = (CommunityNewsListView) v.findViewById(R.id.communityNewsList);
                communityNewsListView.initData(CommunityFragment.this);
            }
            return v;
        }
    }

}
