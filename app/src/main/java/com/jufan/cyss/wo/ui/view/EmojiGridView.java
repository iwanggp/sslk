package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;

/**
 * Created by cyjss on 2015/3/6.
 */
public class EmojiGridView extends GridView {

    private final String LOG_TAG = "EmojiGridView";
    private static String[] emojiArray;

    public EmojiGridView(Context context) {
        super(context);
        init();
    }

    public EmojiGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmojiGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public String getEmojiName(int index) {
        return emojiArray[index];
    }

    private void init() {
        if (emojiArray == null) {
            try {
                emojiArray = getContext().getAssets().list("emojis");
                Log.d(LOG_TAG, "count===>" + emojiArray.length);
            } catch (IOException e) {
                Log.e(LOG_TAG, "", e);
            }
        }
        setAdapter(new EmojiAdapter());
    }

    private class EmojiAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return emojiArray.length;
        }

        @Override
        public Object getItem(int position) {
            return emojiArray[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView emojiImg = null;
            String emoji = emojiArray[position];
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_emoji_item, null);
                emojiImg = (ImageView) convertView.findViewById(R.id.emojiImg);
                convertView.setTag(emojiImg);
            } else {
                emojiImg = (ImageView) convertView.getTag();
            }
            ImageLoader.getInstance().displayImage("assets://emojis/" + emoji, emojiImg);
            return convertView;
        }
    }
}