package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;

import com.jufan.cyss.wo.ui.R;

/**
 * Created by cyjss on 2015/1/30.
 */
public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context, R.style.loadingDialog);
        setContentView(R.layout.dialog_loading);
    }
}
