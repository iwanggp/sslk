package com.jufan.cyss.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by duan on 2015/6/27.
 */
public class SharedPreferencesUtil {
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static boolean isFirst;
    public SharedPreferencesUtil(Context context){
        sp=context.getSharedPreferences("sslk",context.MODE_PRIVATE);
        editor=sp.edit();
    }
    public static void firstLogin(){
       editor.putBoolean("isFirst",false);
        editor.commit();
    }
    public static boolean isFirstLogin(){
       isFirst=sp.getBoolean("isFirst",true);
        return  isFirst;
    }
}
