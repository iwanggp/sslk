package com.jufan.cyss.wo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by cyjss on 2015/3/21.
 */
public class JFWebViewClient extends WebViewClient {

    private Context ctx;
    private String refreshUrl = "";

    public JFWebViewClient(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        refreshUrl = failingUrl;
        view.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return super.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // If dialing phone (tel:5551212)
        if (url.startsWith(WebView.SCHEME_TEL)) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                ctx.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                System.out
                        .println("Error dialing " + url + ": " + e.toString());
            }
            return true;
        }

        // If displaying map (geo:0,0?q=address)
        else if (url.startsWith("geo:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                ctx.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                System.out.println("Error showing map " + url + ": "
                        + e.toString());
            }
            return true;
        }

        // If sending email (mailto:abc@corp.com)
        else if (url.startsWith(WebView.SCHEME_MAILTO)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                ctx.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                System.out.println("Error sending email " + url + ": "
                        + e.toString());
            }
            return true;
        }

        // If sms:5551212?body=This is the message
        else if (url.startsWith("sms:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);

                // Get address
                String address = null;
                int parmIndex = url.indexOf('?');
                if (parmIndex == -1) {
                    address = url.substring(4);
                } else {
                    address = url.substring(4, parmIndex);

                    // If body, then set sms body
                    Uri uri = Uri.parse(url);
                    String query = uri.getQuery();
                    if (query != null) {
                        if (query.startsWith("body=")) {
                            intent.putExtra("sms_body", query.substring(5));
                        }
                    }
                }
                intent.setData(Uri.parse("sms:" + address));
                intent.putExtra("address", address);
                intent.setType("vnd.android-dir/mms-sms");
                ctx.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                System.out.println("Error sending sms " + url + ":"
                        + e.toString());
            }
            return true;
        }

        // All else
        else {
            view.loadUrl(url);
        }
        return true;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    private void webViewLoadComplete(WebView wv) {
        wv.clearAnimation();
        wv.clearDisappearingChildren();
        wv.destroyDrawingCache();
        wv.freeMemory();

        // wv.stopLoading();
        // wv.pauseTimers();
        wv.clearCache(true);
        // wv.clearView();
        // wv.clearFormData();
        // wv.clearHistory();
        // wv.clearMatches();
        // wv.destroy();
    }
}