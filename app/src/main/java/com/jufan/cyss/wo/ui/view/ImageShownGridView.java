package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.util.BitmapUtil;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.kymjs.aframe.ui.ViewInject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by cyjss on 2015/3/10.
 */
public class ImageShownGridView extends GridView {

    private List<AVFile> dataSource;
    private List<Boolean> isUploading = new ArrayList<Boolean>();
    private ImageShownAdapter adapter;

    public ImageShownGridView(Context context) {
        super(context);
        init();
    }

    public ImageShownGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageShownGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dataSource = new LinkedList<AVFile>();
        adapter = new ImageShownAdapter();
        setAdapter(adapter);
        setVisibility(GONE);
    }

    public List<AVFile> getImageFiles() {
        return dataSource;
    }

    public boolean isUploadComplete() {
        for (Boolean flag : isUploading) {
            if (flag) {
                return false;
            }
        }
        return true;
    }

    public void addImage(AVFile file) {
        if (dataSource.size() >= 6) {
            ViewInject.longToast("最多只能上传6张照片");
            return;
        }
        dataSource.add(file);
        final int index = dataSource.size() - 1;
        isUploading.add(true);
        this.setVisibility(VISIBLE);
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (dataSource.size() <= 3) {
            lp.height = GlobalUtil.dip2px(getContext(), 120);
        } else {
            lp.height = GlobalUtil.dip2px(getContext(), 258);
        }
        setLayoutParams(lp);
        adapter.notifyDataSetChanged();
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    isUploading.remove(index);
                    isUploading.add(index, false);
                    adapter.notifyDataSetChanged();
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    public boolean isImageEnough() {
        return dataSource.size() >= 6;
    }

    private class ImageShownAdapter extends BaseAdapter {

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

        public void removeImage(int position) {
            AVFile file = dataSource.remove(position);
            isUploading.remove(position);
            file.deleteInBackground();
            notifyDataSetChanged();
            if (dataSource.isEmpty()) {
                setVisibility(GONE);
            }
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (dataSource.size() <= 3) {
                lp.height = GlobalUtil.dip2px(getContext(), 120);
            } else {
                lp.height = GlobalUtil.dip2px(getContext(), 258);
            }
            setLayoutParams(lp);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AVFile img = (AVFile) getItem(position);
            boolean isUpload = isUploading.get(position);
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.grid_image_shown_item, null);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            TextView removeBtn = (TextView) convertView.findViewById(R.id.removeBtn);
            ProgressBar loading = (ProgressBar) convertView.findViewById(R.id.loading);
            removeBtn.setTag(position);
            removeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = Integer.parseInt(v.getTag().toString());
                    removeImage(index);
                }
            });
            removeBtn.bringToFront();
            try {
                image.setImageBitmap(BitmapUtil.getPreviewBitmap(img.getData(), 120, 120));
            } catch (AVException e) {
                e.printStackTrace();
            }
            if (isUpload) {
                loading.setVisibility(VISIBLE);
            } else {
                loading.setVisibility(GONE);
            }
            return convertView;
        }
    }
}
