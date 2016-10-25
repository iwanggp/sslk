package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.http.RoadVideoHttp;
import com.jufan.cyss.wo.ui.view.LoadingDialog;
import com.telly.groundy.Groundy;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

import java.util.Date;
import java.util.List;

/**
 * Created by cyjss on 2015/3/4.
 */
public class RoadVideo extends BaseUNIActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {

    @BindView(id = R.id.roadVideo)
    private SurfaceView surfaceView;

    private JSONObject road;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;

    private String roadName, dir;

    private int vWidth, vHeight;

    private Display currDisplay;

    private JSONArray roadVideos;

    private LoadingDialog loadingDialog;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_road_video);
    }

    @Override
    protected void initWidget() {
        Intent i = getIntent();
        try {
            this.road = new JSONObject(i.getStringExtra("json"));
            setupActionBar(this.road.getString("road"), ActionBarType.BACK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.loadingDialog = new LoadingDialog(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        currDisplay = this.getWindowManager().getDefaultDisplay();
    }

    private void playVideo() throws JSONException {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);

        String road = roadVideos.getString(roadVideos.length() - 1);
        try {
            mediaPlayer.reset();
            String url = road;
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
        }
    }

    private void requestNewestVideo() {
        this.loadingDialog.show();
        try {
            JSONObject obj = new JSONObject();
            obj.put("road_name", road.getString("dir"));
            Groundy.create(RoadVideoHttp.class).callback(this).arg("url", RoadVideoHttp.REQ_VIDEO_SEARCH_URL + "?timestamp="
                    + new Date().getTime()).arg("json", obj.toString()).queueUsing(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnSuccess(RoadVideoHttp.class)
    public void onSuccess(@Param("data") String data) {
        try {
            JSONObject obj = new JSONObject(data);
            this.roadVideos = obj.getJSONArray("road_list");
            playVideo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnFailure(RoadVideoHttp.class)
    public void onFailure(@Param("code") String code, @Param("desc") String desc) {
        ViewInject.longToast(desc);
        loadingDialog.dismiss();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        requestNewestVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        requestNewestVideo();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        vWidth = mp.getVideoWidth();
        vHeight = mp.getVideoHeight();
        float wRatio = (float) vWidth / (float) currDisplay.getWidth();
        float hRatio = (float) vHeight / (float) currDisplay.getHeight();
        float ratio = Math.max(wRatio, hRatio);
        vWidth = (int) Math.ceil((float) vWidth / ratio);
        vHeight = (int) Math.ceil((float) vHeight / ratio);
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(vWidth,
                vHeight));

        mp.start();
        loadingDialog.hide();
    }
}
