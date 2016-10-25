package com.jufan.cyss.util;

import android.util.Log;

import com.avos.avoscloud.AVUser;
import com.jufan.cyss.wo.ui.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by cyjss on 2014/12/23.
 */
public class HttpUtil {
    private static final String LOG_MSG = "HttpUtil";
    private static final String REQ_URL = "http://www.jtgzfw.com/jtgzfw/Unicom";
    public static final String REQ_TEST_URL = "http://sys713.jtgzfw.com/jtgzfw/Unicom";
    //    private static final String REQ_URL = "http://192.168.1.130:8080/jtgzfw/Unicom";
    private static final String REQ_FILE_URL = "http://www.jtgzfw.com/jtgzfw/Unicom/upload/";
    private static final String REQ_VIO_THUMB_URL = "http://125.46.83.214/sslk/video/vio/thumb?";
    private static final String REQ_VIO_IMG_URL = "http://www.jtgzfw.com/jtgzfw/search/get_vio_pic.do?";
    private static HttpClient httpClient;
    private static BasicHttpParams httpParams;

    public static DisplayImageOptions DefaultOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.ic_launcher)
            .build();

    public static final String LOGIN_TAG = "username";

    static {
        httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
        httpClient = new DefaultHttpClient(httpParams);
    }

    public static String doPost(String url, String text) {
        String strResult = null;
        try {
            Log.d(LOG_MSG, "==->" + url);
            Log.d(LOG_MSG, "==->" + text);
            HttpPost post = new HttpPost(url);
            StringEntity se = new StringEntity(text, "UTF-8");
            post.setEntity(se);
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse res = httpClient.execute(post);
            if (res.getStatusLine().getStatusCode() == 200) {
                /* 读返回数据 */
                strResult = EntityUtils.toString(res.getEntity(), "utf-8");
                strResult = URLDecoder.decode(strResult, "utf-8");
                Log.d(LOG_MSG, "===>" + strResult);
                return strResult;
            } else {
                strResult = "Error Response: "
                        + res.getStatusLine().toString();
            }
        } catch (Exception e) {
            Log.e(LOG_MSG, LOG_MSG, e);
        }
        return strResult;
    }

    public static JSONObject callRemoteService(String code, JSONObject json) throws JSONException {
        return callRemoteService(REQ_URL, code, json);
    }

    public static JSONObject callRemoteService(String url, String code, JSONObject json) throws JSONException {
        String username = null;
        if (json.has(LOGIN_TAG)) {
            username = json.getString(LOGIN_TAG);
        }
        JSONObject head = new JSONObject();
        json.put("head", head);
        head.put("service_code", code);
        if (!StringUtils.isEmpty(username)) {
            head.put(LOGIN_TAG, username);
            head.put("channel", "M");
        }
        String md5Seg = md5Packet(json);
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("json", json.toString()));
            params.add(new BasicNameValuePair("md5", md5Seg));
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse res = httpClient.execute(post);
            /* 若状态码为200 ok */
            String strResult = null;
            if (res.getStatusLine().getStatusCode() == 200) {
                /* 读返回数据 */
                strResult = EntityUtils.toString(res.getEntity(), "utf-8");
                if (!strResult.startsWith("{")) {
                    strResult = strResult.substring(32);
                }
                strResult = URLDecoder.decode(strResult, "utf-8");
                Log.d(LOG_MSG, "===>" + strResult);
                return new JSONObject(strResult);
            } else {
                strResult = "Error Response: "
                        + res.getStatusLine().toString();
            }
        } catch (IOException e) {
            Log.e(LOG_MSG, "发送Http异常", e);
        }
        return null;
    }

    public static JSONObject callRemoteService(String code, JSONObject json, Map<String, File> files) throws JSONException {
        String username = null;
        if (json.has(LOGIN_TAG)) {
            username = json.getString(LOGIN_TAG);
        }
        JSONObject head = new JSONObject();
        json.put("head", head);
        if (!StringUtils.isEmpty(username)) {
            head.put(LOGIN_TAG, username);
            head.put("channel", "M");
        }
        String md5Seg = md5Packet(json);
        try {
            HttpPost post = new HttpPost(REQ_FILE_URL + code + ".do");
            MultipartEntity mpEntity = new MultipartEntity();
            mpEntity.addPart("json", new StringBody(md5Seg + json.toString(), Charset.forName("utf-8")));
            for (String key : files.keySet()) {
                Log.d(LOG_MSG, "=-=--=-=>" + files.get(key).getAbsolutePath());
                ContentBody file = new FileBody(files.get(key));
                mpEntity.addPart(key, file);
            }
            post.setEntity(mpEntity);
            HttpResponse res = httpClient.execute(post);
            /* 若状态码为200 ok */
            String strResult = null;
            if (res.getStatusLine().getStatusCode() == 200) {
                /* 读返回数据 */
                strResult = EntityUtils.toString(res.getEntity(), "utf-8");
                Log.d(LOG_MSG, "===>" + strResult);
                return new JSONObject(strResult);
            } else {
                strResult = "Error Response: "
                        + res.getStatusLine().toString();
            }
        } catch (IOException e) {
            Log.e(LOG_MSG, "发送Http异常", e);
        }
        return null;
    }

    public static String doPost(String url, Map<String, String> params) throws IOException {
        HttpPost post = new HttpPost(url);
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            paramsList.add(new BasicNameValuePair(key, params.get(key)));
        }
        post.setEntity(new UrlEncodedFormEntity(paramsList, HTTP.UTF_8));
        HttpResponse res = httpClient.execute(post);
            /* 若状态码为200 ok */
        String strResult = null;
        if (res.getStatusLine().getStatusCode() == 200) {
            strResult = EntityUtils.toString(res.getEntity(), "utf-8");
            Log.d(LOG_MSG, "===>" + strResult);
        } else {
            Log.d(LOG_MSG, "Error Response: "
                    + res.getStatusLine().toString());
        }
        return strResult;
    }

    public static String doGet(String url, Map<String, String> params) throws IOException {
        HttpGet get = new HttpGet(url);
        HttpParams httpParams = new BasicHttpParams();
        for (String key : params.keySet()) {
            httpParams.setParameter(key, params.get(key));
        }
        get.setParams(httpParams);
        HttpResponse res = httpClient.execute(get);
            /* 若状态码为200 ok */
        String strResult = null;
        if (res.getStatusLine().getStatusCode() == 200) {
            strResult = EntityUtils.toString(res.getEntity(), "utf-8");
            Log.d(LOG_MSG, "===>" + strResult);
        } else {
            Log.d(LOG_MSG, "Error Response: "
                    + res.getStatusLine().toString());
        }
        return strResult;
    }

    private static String md5Packet(JSONObject json) {
        String md5Seg = "";
//        List<String> paraList = new LinkedList<String>();
//        Iterator<String> it = json.keys();
//        while (it.hasNext()) {
//            paraList.add(it.next());
//        }
//        Collections.sort(paraList);
//        for (String key : paraList) {
//            md5Seg += key;
//        }
        try {
            md5Seg = MD5.encrypt32(json.toString() + "jufantec.com");
        } catch (Exception e) {
            Log.e(LOG_MSG, "加密错误", e);
        }
        return md5Seg;
    }

    public static String getReqVioImgUrl(String xh) {
        String username = "anon";
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            username = user.getMobilePhoneNumber();
        }
        return REQ_VIO_IMG_URL + "xh=" + xh + "&username=" + username;
    }

    public static String getReqVioThumbUrl(String xh) {
        String username = "anon";
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            username = user.getMobilePhoneNumber();
        }
        return REQ_VIO_THUMB_URL + "xh=" + xh + "&username=" + username;
    }
}
