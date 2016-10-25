package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.wo.ui.view.JFWebChromeClient;
import com.jufan.cyss.wo.ui.view.JFWebViewClient;

import org.kymjs.aframe.ui.BindView;

/**
 * Created by cyjss on 2015/3/20.
 */
public class CommunityNews extends BaseUNIActivity {

    @BindView(id = R.id.webView)
    private WebView webView;
    @BindView(id = R.id.loading)
    private TextView loading;

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_community_news);
    }

    @Override
    protected void initWidget() {
        setupActionBar("播报", ActionBarType.BACK);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new JFWebViewClient(this));
        webView.setWebChromeClient(new JFWebChromeClient(this, loading));
        webView.loadUrl(url);
    }
}
