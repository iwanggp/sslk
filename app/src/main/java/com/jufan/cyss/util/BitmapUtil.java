package com.jufan.cyss.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by cyjss on 2015/3/10.
 */
public class BitmapUtil {

    private static Bitmap WEATHER_IMAGE;
    private static Bitmap[] WEATHER_IMAGE_ARRAY = new Bitmap[32];

    public static Bitmap getPreviewBitmap(byte[] data, int maxWidth, int maxHeight) {
        Bitmap bitm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            int sc = sampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sc;
            bitm = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitm;
    }

    public static Bitmap getPreviewBitmap(String path, int maxWidth, int maxHeight) {
        Bitmap bitm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(path, options);
            int sc = sampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            options.inSampleSize = sc;
            bitm = BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            Log.e("getPreviewBitmap", e.getMessage() + "");
            e.printStackTrace();
        }
        return bitm;
    }

    // Calculate the required degree for image compression:
    private static int sampleSize(int width, int height, int maxWidth, int maxHeight) {
        int sample = 1;
        if ((width > maxWidth) && (height > maxHeight) &&
                (maxWidth > 0) && (maxHeight > 0)) {
            float w = (float) width / maxWidth;
            float h = (float) height / maxHeight;
            sample = (int) Math.ceil((w + h) / 2);
        }
        return sample;
    }

    public static Bitmap getWeather(Context ctx, int index) {
        if (index > WEATHER_IMAGE_ARRAY.length - 1 && index != 53) {
            return null;
        }
        initWeather(ctx);
        return WEATHER_IMAGE_ARRAY[index];
    }

    private static void initWeather(Context ctx) {
        if (WEATHER_IMAGE == null) {
            try {
                WEATHER_IMAGE = BitmapFactory.decodeStream(ctx.getResources().getAssets().open("weather/wsprites.png"));
                for (int i = 0; i < WEATHER_IMAGE_ARRAY.length; i++) {
                    WEATHER_IMAGE_ARRAY[i] = BitmapFactory.decodeStream(ctx.getResources().getAssets().open("weather/" + i + ".jpg"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
