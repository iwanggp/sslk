package com.jufan.cyss.frame;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.jufan.cyss.wo.ui.MainActivity;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cyjss on 2015/1/28.
 */
public class BaseUNIApplication extends Application {
    private List<Activity> activityList = new LinkedList<Activity>();
    private SharedPreferences sp;
    private MainActivity mainActivity;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initImageLoader(getApplicationContext());
        AVOSCloud.initialize(this, "kilaiyealtxc69yh6h2078h2cka88kl63zeax7b14kbpmaxf", "7d2c59udhn9o7aqigpj0fpjgnb27glinc4gsptdhkkqqtkmq");
        AVAnalytics.enableCrashReport(this, true);
        AVOSCloud.setDebugLogEnabled(false);
        ActiveAndroid.initialize(this);
        loadRClass();
    }

    public void addActivity(Activity act) {
        activityList.add(act);
    }

    public void closeActivityWithoutThis(Activity act) {
        for (Activity a : activityList) {
            if (a == act) {
                continue;
            } else {
                a.finish();
            }
        }
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCacheExtraOptions(1500, 3000, null)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    private void loadRClass() {
        String packageName = getPackageName();
        Log.d("==->", packageName);
    }
}
