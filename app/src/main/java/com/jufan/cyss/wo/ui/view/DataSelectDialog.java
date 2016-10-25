package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.R;

/**
 * Created by cyjss on 2015/2/2.
 */
public class DataSelectDialog extends Dialog {

    private TextView title;
    private Button cancelBtn;
    private LinearLayout selectContainer;
    private OnDataSelectDialogItemSelectedListener listener;

    private String[] array;

    public DataSelectDialog(Context context, int array) {
        super(context, R.style.selectDataDialog);
        setContentView(R.layout.dialog_data_select);
        this.title = (TextView) findViewById(R.id.title);
        this.selectContainer = (LinearLayout) findViewById(R.id.selectContainer);
        this.cancelBtn = (Button) findViewById(R.id.cancelBtn);
        this.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        this.array = getContext().getResources().getStringArray(array);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        for (int i = 0; i < array.length; i++) {
            String item = array[i];
            View v = LayoutInflater.from(getContext()).inflate(R.layout.part_dialog_data_select_btn, null);
            Button btn = (Button) v.findViewById(R.id.itemBtn);
            btn.setTag(i);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        Button btn = (Button) v;
                        listener.selected(v, (int) v.getTag(), btn.getText().toString());
                    }
                }
            });
            btn.setText(item);
            selectContainer.addView(v);
        }

        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = window.getWindowManager().getDefaultDisplay().getWidth();
        lp.x = 0;
        lp.y = window.getWindowManager().getDefaultDisplay().getHeight() - lp.height;
        window.setAttributes(lp);
    }

    public DataSelectDialog setTitle(String str) {
        title.setText(str);
        return this;
    }

    public DataSelectDialog setOnDataSelectDialogItemSelected(OnDataSelectDialogItemSelectedListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnDataSelectDialogItemSelectedListener {
        public void selected(View v, int index, String txt);
    }
}
