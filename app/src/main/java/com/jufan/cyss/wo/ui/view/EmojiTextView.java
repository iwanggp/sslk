package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cyjss on 2015/3/6.
 */
public class EmojiTextView extends TextView {

    public EmojiTextView(Context context) {
        super(context);
    }

    public EmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setEmojiText(CharSequence text) {
        if (text == null) {
            return;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String txt = text.toString();
        int i = 0;
        while ((i = txt.indexOf(":[")) != -1) {
            builder.append(txt.substring(0, i));
            int end = txt.indexOf("] ");
            String emojiStr = txt.substring(i + 2, end);
            txt = txt.substring(end + 2);
            InputStream is = null;
            SpannableString spannable = new SpannableString(emojiStr);
            try {
                is = getResources().getAssets().open("emojis/" + emojiStr + ".png");
                Drawable drawable = new BitmapDrawable(BitmapFactory.decodeStream(is));
                drawable.setBounds(0, 0, (int) getTextSize() + 2, (int) getTextSize() + 2);
                ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                spannable.setSpan(span, 0, emojiStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                builder.append(spannable);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        builder.append(txt);
        if (builder.length() == 0) {
            setText(text);
        } else {
            setText(builder);
        }
    }


}
