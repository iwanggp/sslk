package com.jufan.cyss.wo.ui.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by cyjss on 2015/3/21.
 */
public class JFWebChromeClient extends WebChromeClient {

    private Activity ctx;
    private TextView loading;
    private int width;

    public JFWebChromeClient(Activity ctx, TextView loading) {
        this.ctx = ctx;
        this.loading = loading;
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        this.width = mDisplayMetrics.widthPixels;
    }

    /**
     * Tell the client to display a javascript alert dialog.
     *
     * @param view
     * @param url
     * @param message
     * @param result
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message,
                             final JsResult result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
        dlg.setMessage(message);
        dlg.setTitle("来自 " + url);
        dlg.setCancelable(false);
        dlg.setPositiveButton(android.R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dlg.create();
        dlg.show();
        return true;
    }

    /**
     * Tell the client to display a confirm dialog to the user.
     *
     * @param view
     * @param url
     * @param message
     * @param result
     */
    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
                               final JsResult result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
        dlg.setMessage(message);
        dlg.setTitle("来自 " + url);
        dlg.setCancelable(false);
        dlg.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dlg.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        dlg.create();
        dlg.show();
        return true;
    }

    /**
     * Tell the client to display a prompt dialog to the user. If the client
     * returns true, WebView will assume that the client will handle the prompt
     * dialog and call the appropriate JsPromptResult method.
     *
     * @param view
     * @param url
     * @param message
     * @param defaultValue
     * @param result
     */
    @Override
    public boolean onJsPrompt(WebView view, String url, String message,
                              String defaultValue, JsPromptResult result) {

        return true;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {

        if (newProgress != 100 && newProgress != 0) {
            // loadingBar.setBackgroundColor(ctx.getResources().getColor(
            // R.color.loading_bar_visible));
//            loadingBar.setVisibility(View.VISIBLE);
            loading.setVisibility(View.VISIBLE);
            int progress = width * newProgress / 100;
            ViewGroup.LayoutParams lp = loading.getLayoutParams();
            lp.width = progress;
            loading.setLayoutParams(lp);
            // Log.i(SystemUtil.LOG_MSG, "process: " + newProgress + ","
            // + progress);
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                    progress, 6);
//            layoutParams.addRule(RelativeLayout.ABOVE, R.id.bottom_bar);
//
//            loadingBar.setLayoutParams(layoutParams);

        } else if (newProgress == 100) {
//            loadingBar.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            view.destroyDrawingCache();
            view.clearCache(true);
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
    }
}
