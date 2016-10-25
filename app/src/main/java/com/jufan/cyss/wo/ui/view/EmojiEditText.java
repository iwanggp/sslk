package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cyjss on 2015/3/6.
 */
public class EmojiEditText extends EditText {

    public EmojiEditText(Context context) {
        super(context);
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void appendEmoji(String emoji) {
        InputStream is = null;
        String emojiStr = emoji.substring(0, emoji.indexOf("."));
        emojiStr = ":[" + emojiStr + "] ";
        SpannableString spannable = new SpannableString(emojiStr);
        try {
            is = getResources().getAssets().open("emojis/" + emoji);
            Drawable drawable = new BitmapDrawable(BitmapFactory.decodeStream(is));

            drawable.setBounds(0, 0, (int) getTextSize(), (int) getTextSize());
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
            spannable.setSpan(span, 0, emojiStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            append(spannable);
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


    @Override
    public Editable getText() {
        Editable text = super.getText();
        return text;
    }
}
