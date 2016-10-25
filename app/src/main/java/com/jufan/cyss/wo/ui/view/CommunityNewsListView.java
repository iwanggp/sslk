package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.frame.BaseUNIApplication;
import com.jufan.cyss.util.DateUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.wo.ui.CommunityFragment;
import com.jufan.cyss.wo.ui.CommunityNews;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;
import org.kymjs.aframe.ui.widget.KJRefreshListener;
import org.kymjs.aframe.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cyjss on 2015/3/20.
 */
public class

        CommunityNewsListView extends KJListView implements AdapterView.OnItemClickListener, KJRefreshListener {

    private CommunityFragment communityFragment;
    private BaseUNIApplication application;

    private int page = 0;
    private int pageNum = 12;
    private Date refreshDate = new Date();

    private CommunityNewsAdapter adapter;

    public CommunityNewsListView(Context context) {
        super(context);
    }

    public CommunityNewsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommunityNewsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(CommunityFragment communityFragment) {
        this.communityFragment = communityFragment;
        this.application = (BaseUNIApplication) communityFragment.getActivity().getApplication();
        loadMore();
        setPullRefreshEnable(true);
        setPullLoadEnable(true);
        setOnRefreshListener(this);
        setOnItemClickListener(this);
    }

    private void loadMore() {
        application.getMainActivity().showLoading();
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityNews");
        query.limit(pageNum);
        query.skip(page * pageNum);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                application.getMainActivity().hideLoading();
                stopLoadMore();
                if (e == null) {
                    if (page == 0) {
                        if (avObjects.isEmpty()) {
                            setAdapter(new NoDataAdapter(getContext()));
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                        } else {
                            adapter = new CommunityNewsAdapter();
                            setAdapter(adapter);
                            adapter.addMoreData(avObjects);
                            if (avObjects.size() < pageNum) {
                                setPullLoadEnable(false);
                                getFooterView().setVisibility(GONE);
                            }
                        }
                    } else {
                        if (avObjects.isEmpty()) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                            ViewInject.longToast("没有更多数据");
                        } else {
                            adapter.addMoreData(avObjects);
                        }
                    }
                    page++;
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    private void refreshData() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityNews");
        query.orderByDescending("createdAt");
        query.whereGreaterThan("createdAt", refreshDate);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopRefreshData();
                refreshDate = new Date();
                if (e == null) {
                    if (adapter != null) {
                        if (!avObjects.isEmpty()) {
                            adapter.addRefreshData(avObjects);
                        }
                    } else {
                        if (!avObjects.isEmpty()) {
                            adapter = new CommunityNewsAdapter();
                            adapter.addMoreData(avObjects);
                        }
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public void onLoadMore() {
        loadMore();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Holder holder = (Holder) view.getTag();
        Intent intent = new Intent(getContext(), CommunityNews.class);
        intent.putExtra("url", holder.url);
        getContext().startActivity(intent);
    }

    private class CommunityNewsAdapter extends BaseAdapter {

        private List<AVObject> dataSource = new ArrayList<AVObject>();

        public void addMoreData(List<AVObject> data) {
            this.dataSource.addAll(data);
            notifyDataSetChanged();
        }

        public void addRefreshData(List<AVObject> data) {
            this.dataSource.addAll(0, data);
            notifyDataSetChanged();
        }

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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AVObject avObject = (AVObject) getItem(position);
            Holder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_community_news_item, null);
                holder = new Holder();
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.newsTitle = (TextView) convertView.findViewById(R.id.newsTitle);
                holder.newsImg = (ImageView) convertView.findViewById(R.id.newsImg);
                holder.newsDesc = (TextView) convertView.findViewById(R.id.newsDesc);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.time.setText(DateUtil.getShortTimeDesc(avObject.getCreatedAt()));
            holder.newsTitle.setText(avObject.getString("newsTitle"));
            AVFile newsImgFile = avObject.getAVFile("newsImg");
            ImageLoader.getInstance().displayImage(newsImgFile.getUrl(), holder.newsImg, new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                    .showImageOnLoading(R.drawable.loading_img_bg)
                    .build());
            if (StringUtils.isEmpty(avObject.getString("newsDesc"))) {
                holder.newsDesc.setVisibility(GONE);
            } else {
                holder.newsDesc.setVisibility(VISIBLE);
                holder.newsDesc.setText(avObject.getString("newsDesc"));
            }
            holder.url = avObject.getString("newsText");
            return convertView;
        }
    }

    private class Holder {
        public TextView time;
        public TextView newsTitle;
        public ImageView newsImg;
        public TextView newsDesc;
        public String url;
    }
}
