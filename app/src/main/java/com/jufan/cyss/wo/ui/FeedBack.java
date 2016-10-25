package com.jufan.cyss.wo.ui;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.jufan.cyss.frame.BaseUNIActivity;
import com.jufan.cyss.util.GlobalUtil;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;

import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;

/**
 * Created by cyjss on 2015/2/2.
 */
public class FeedBack extends BaseUNIActivity {

    @Required(order = 3, message = "内容描述不可为空")
    @TextRule(order = 4, message = "内容描述长度不可超过256", maxLength = 256)
    @BindView(id = R.id.descEt)
    private EditText descEt;

    @BindView(id = R.id.type1, click = true)
    private TextView type1;
    @BindView(id = R.id.type2, click = true)
    private TextView type2;
    @BindView(id = R.id.type3, click = true)
    private TextView type3;
    @BindView(id = R.id.type4, click = true)
    private TextView type4;
    @BindView(id = R.id.type5, click = true)
    private TextView type5;

    private TextView[] typeArray = new TextView[5];

    private int typeSelectNum = 0;


    @Override
    public void setRootView() {
        setContentView(R.layout.activity_feedback);
    }

    @Override
    protected void initWidget() {
        setupActionBar("问题反馈", ActionBarType.BACK);
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText("提交");
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        typeArray[0] = type1;
        typeArray[1] = type2;
        typeArray[2] = type3;
        typeArray[3] = type4;
        typeArray[4] = type5;
    }

    @Override
    public void onValidationSucceeded() {
        showLoading();
        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setEnabled(false);
        AVObject feedBack = new AVObject("FeedBack");
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            feedBack.put("userId", user.getObjectId());
            feedBack.put("mobilePhoneNumber", user.getMobilePhoneNumber());
        }
        feedBack.put("questionTag", typeArray[typeSelectNum].getTag().toString());
        feedBack.put("detail", descEt.getText().toString());
        feedBack.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                hideLoading();
                Button rightBtn = (Button) findViewById(R.id.rightBtn);
                rightBtn.setEnabled(true);
                if (e == null) {
                    descEt.setText("");
                    setTypeSelected(0);
                    ViewInject.longToast("感谢您的反馈，我们会尽快改进");
                } else {
                    GlobalUtil.showNetworkError();
                }
            }
        });
    }

    private void setTypeSelected(int num) {
        for (TextView tv : typeArray) {
            tv.setTextColor(0xff444648);
            tv.setBackgroundColor(Color.WHITE);
        }
        typeArray[num].setTextColor(Color.WHITE);
        typeArray[num].setBackgroundColor(0xffff9900);
        typeSelectNum = num;
    }

    @Override
    public void widgetClick(View v) {
        switch (v.getId()) {
            case R.id.type1:
                setTypeSelected(0);
                break;
            case R.id.type2:
                setTypeSelected(1);
                break;
            case R.id.type3:
                setTypeSelected(2);
                break;
            case R.id.type4:
                setTypeSelected(3);
                break;
            case R.id.type5:
                setTypeSelected(4);
                break;
        }
    }
}
