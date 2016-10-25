package com.jufan.cyss.wo.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.wo.ui.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import java.util.Objects;

/**
 * Created by cyjss on 2015/4/14.
 */
public class ShareDialog extends Dialog implements AdapterView.OnItemClickListener {
    private Button cancelBtn;
    private GridView shareGrid;
    private UMSocialService mController;

    private final String SHARE_TEXT = "手机查违章、看违章照片更便捷，免费违章提醒更及时，更可与百万车主路况互动。";
    private final String SHARE_URL = "http://121.40.145.106:3000/index.html";

    private final ShareBean[] SHARE_CHANNEL = {
            new ShareBean("朋友圈", R.drawable.icon_pengyouquan),
            new ShareBean("微信好友", R.drawable.icon_weixin)
    };

    public ShareDialog(Context context) {
        super(context, R.style.selectDataDialog);
        setContentView(R.layout.dialog_share);
        this.cancelBtn = (Button) findViewById(R.id.cancelBtn);
        this.shareGrid = (GridView) findViewById(R.id.shareGrid);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // 首先在您的Activity中添加如下成员变量
        mController = UMServiceFactory.getUMSocialService("com.umeng.share");
        // 设置分享内容
        mController.setShareContent(SHARE_TEXT + SHARE_URL);

        String appID = "wxc6b75601020e46e3";
        String appSecret = "2b11ea841c1b9bd07983583bbd6e70f2";
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(getContext(), appID, appSecret);
        wxHandler.addToSocialSDK();
        // 添加微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(getContext(), appID, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();

        //设置微信好友分享内容
        WeiXinShareContent weixinContent = new WeiXinShareContent();
        //设置分享文字
        weixinContent.setShareContent(SHARE_TEXT + "<a href=\"" + SHARE_URL + "\">" + SHARE_URL + "</a>");
        //设置title
        weixinContent.setTitle(getContext().getResources().getString(R.string.app_name));
        //设置分享内容跳转URL
        weixinContent.setTargetUrl(SHARE_URL);
        mController.setShareMedia(weixinContent);

        //设置微信朋友圈分享内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent(SHARE_TEXT + SHARE_URL);
        //设置朋友圈title
        circleMedia.setTitle(getContext().getResources().getString(R.string.app_name));
        circleMedia.setTargetUrl(SHARE_URL);
        mController.setShareMedia(circleMedia);

        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = GlobalUtil.dip2px(getContext(), 220);
        lp.width = window.getWindowManager().getDefaultDisplay().getWidth();
        lp.x = 0;
        lp.y = window.getWindowManager().getDefaultDisplay().getHeight() - lp.height;
        window.setAttributes(lp);

        this.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        this.shareGrid.setAdapter(new ShareAdapter());
        this.shareGrid.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            mController.postShare(getContext(), SHARE_MEDIA.WEIXIN_CIRCLE, new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(SHARE_MEDIA share_media, int code, SocializeEntity socializeEntity) {

                    Log.d("sd==>", "==>" + code + "," + share_media.getReqCode() + "," + socializeEntity.getShareContent());
                }
            });
        } else if (position == 1) {
            mController.postShare(getContext(), SHARE_MEDIA.WEIXIN, new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(SHARE_MEDIA share_media, int code, SocializeEntity socializeEntity) {
                    Log.d("sd==>", "==>" + code + "," + share_media.getReqCode() + "," + socializeEntity.getShareContent());
                }
            });
        } else if (position == 2) {

        }
    }

    private class ShareAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return SHARE_CHANNEL.length;
        }

        @Override
        public Object getItem(int position) {
            return SHARE_CHANNEL[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ShareBean sb = (ShareBean) getItem(position);
            convertView = View.inflate(getContext(), R.layout.grid_share_item, null);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView text = (TextView) convertView.findViewById(R.id.text);
            icon.setImageResource(sb.icon);
            text.setText(sb.title);
            return convertView;
        }
    }

    private class ShareBean {
        public ShareBean(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

        public String title;
        public int icon;
    }
}
