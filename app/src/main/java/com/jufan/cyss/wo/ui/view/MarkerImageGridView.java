package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetFileCallback;
import com.jufan.cyss.util.BitmapUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.jufan.cyss.wo.ui.ImageCategory;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cyjss on 2015/3/11.
 */
public class MarkerImageGridView extends GridView implements AdapterView.OnItemClickListener {

    private List<Object> dataSource;
    private MarkerImageAdapter adapter;
    private String[] urls;
    private String[] thumbUrls;

    public MarkerImageGridView(Context context) {
        super(context);
        init();
    }

    public MarkerImageGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarkerImageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.dataSource = new LinkedList<Object>();
        this.adapter = new MarkerImageAdapter();
        setOnItemClickListener(this);
    }

    public void setDataSource(List<Object> dataSource) {
        if (dataSource == null) {
            this.dataSource = new ArrayList<Object>();
        } else {
            this.dataSource = dataSource;
        }

        if (this.dataSource.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        this.urls = new String[this.dataSource.size()];
        this.thumbUrls = new String[this.dataSource.size()];
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (this.dataSource.size() <= 3) {
            lp.height = GlobalUtil.dip2px(getContext(), 68);
        } else {
            lp.height = GlobalUtil.dip2px(getContext(), 136);
        }
        setLayoutParams(lp);

        setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getContext(), ImageCategory.class);
        i.putExtra("position", position);
        i.putExtra("urls", urls);
        i.putExtra("thumb_urls", thumbUrls);
        getContext().startActivity(i);
    }

    private class MarkerImageAdapter extends BaseAdapter {

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            Object item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.grid_marker_image_item, null);
            }
            final ImageView imageIv = (ImageView) convertView.findViewById(R.id.image);
            if (item instanceof AVObject) {
                AVObject img = (AVObject) item;
                AVFile.withObjectIdInBackground(img.getObjectId(), new GetFileCallback<AVFile>() {
                    @Override
                    public void done(AVFile avFile, AVException e) {
                        if (e == null) {
                            String thumbUrl = avFile.getThumbnailUrl(true, 100, 100);
                            ImageLoader.getInstance().displayImage(thumbUrl, imageIv, HttpUtil.DefaultOptions);
                            thumbUrls[position] = thumbUrl;
                            urls[position] = avFile.getUrl();
                        } else {
                            Log.e("", "show img error", e);
                        }
                    }
                });
            } else if (item instanceof AVFile) {
                AVFile img = (AVFile) item;
                try {
                    imageIv.setImageBitmap(BitmapUtil.getPreviewBitmap(img.getData(), 100, 100));
                    thumbUrls[position] = img.getThumbnailUrl(true, 100, 100);
                    urls[position] = img.getUrl();
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
            return convertView;
        }
    }
}
