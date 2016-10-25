package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.jufan.cyss.adapter.NoDataAdapter;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.GlobalUtil;

import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.ui.widget.KJListView;

import java.util.Date;
import java.util.List;

/**
 * Created by cyjss on 2015/3/21.
 */
public class MyCommunityPostListView extends CommunityListView {

    private BaseUNIActivity activity;

    public MyCommunityPostListView(Context context) {
        super(context);
    }

    public MyCommunityPostListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCommunityPostListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(BaseUNIActivity activity) {
        this.activity = activity;
        setOnRefreshListener(this);
        adapter = new CommunityListAdapter();
        setAdapter(adapter);
        setPullRefreshEnable(true);
        setPullLoadEnable(true);
        setOnItemClickListener(this);
        loadData();
    }

    @Override
    protected void loadData() {
        activity.showLoading();
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityPost");
        query.whereEqualTo("user", AVUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.skip(page * pageNum);
        query.limit(pageNum);
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                activity.hideLoading();
                stopLoadMore();
                if (e == null) {
                    if (avObjects == null || avObjects.isEmpty()) {
                        if (page == 0) {
                            setAdapter(new NoDataAdapter(getContext()));
                        } else {
                            ViewInject.longToast("没有更多数据");
                        }
                        setPullLoadEnable(false);
                        getFooterView().setVisibility(GONE);
                    } else {
                        adapter.addData(avObjects);
                        if (page == 0 && avObjects.size() != pageNum) {
                            setPullLoadEnable(false);
                            getFooterView().setVisibility(GONE);
                        }
                        page++;
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("CommunityPost");
        query.orderByDescending("createdAt");
        query.whereEqualTo("user", AVUser.getCurrentUser());
        query.whereGreaterThan("createdAt", refreshDate);
        query.include("user");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                stopRefreshData();
                if (e == null) {
                    refreshDate = new Date();
                    if (avObjects != null && !avObjects.isEmpty()) {
                        if (adapter.getCount() == 0) {
                            setAdapter(adapter);
                        }
                        adapter.addRefreshData(avObjects);
                    }
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

}
