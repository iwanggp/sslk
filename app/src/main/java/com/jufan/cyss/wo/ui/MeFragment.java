package com.jufan.cyss.wo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.jufan.cyss.frame.BaseUNIFragment;
import com.jufan.cyss.model.Simple;
import com.jufan.cyss.util.GlobalUtil;
import com.jufan.cyss.util.HttpUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.aframe.ui.BindView;
import org.kymjs.aframe.ui.ViewInject;
import org.kymjs.aframe.utils.StringUtils;

/**
 * Created by cyjss on 2015/1/29.
 */
public class MeFragment extends BaseUNIFragment {

    private View mainView;

    @BindView(id = R.id.username)
    private TextView username;
    @BindView(id = R.id.userDesc)
    private TextView userDesc;
    @BindView(id = R.id.logoutBtn, click = true)
    private Button logoutBtn;
    @BindView(id = R.id.avatar)
    private ImageView avatar;

    @BindView(id = R.id.myVioContainer, click = true)
    private RelativeLayout myVioContainer;
    @BindView(id = R.id.profileContainer, click = true)
    private RelativeLayout profileContainer;
    @BindView(id = R.id.myFavContainer, click = true)
    private RelativeLayout myFavContainer;
    @BindView(id = R.id.myVioFeeContainer, click = true)
    private RelativeLayout myVioFeeContainer;
    @BindView(id = R.id.myPostContainer, click = true)
    private RelativeLayout myPostContainer;

    @Override
    protected View inflaterView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_me, null);
    }

    @Override
    public void widgetResume() {
        application.getMainActivity().setupActionBar("我的");
        if (GlobalUtil.isLogin()) {
            AVUser user = AVUser.getCurrentUser();
            logoutBtn.setVisibility(View.VISIBLE);
            username.setText(user.getUsername());
            String desc = user.getString("userDesc");
            if (StringUtils.isEmpty(desc)) {
                desc = "未填写";
            }
            userDesc.setText("个人简介：" + desc);
            if (user.has("avatar")) {
                AVFile avatarFile = (AVFile) user.get("avatar");
                ImageLoader.getInstance().displayImage(avatarFile.getUrl(), avatar, HttpUtil.DefaultOptions);
            }
        } else {
            logoutBtn.setVisibility(View.GONE);
            username.setText("未登录，点击登录");
            userDesc.setText("个人简介：");
            avatar.setImageResource(R.drawable.default_avatar);
        }
    }

    @Override
    public void initWidget(View parentView) {

    }

    @Override
    public void widgetClick(View v) {
        Intent i = null;
        switch (v.getId()) {
            case R.id.myVioContainer:
                i = new Intent(getActivity(), CarList.class);
//                AVUser user = AVUser.getCurrentUser();
//                if (user == null) {
//                    Simple vio = Simple.getByKey("vio");
//                    if (!StringUtils.isEmpty(vio.value)) {
//                        i = new Intent(getActivity(), MyVio.class);
//                    } else {
//                        i = new Intent(getActivity(), BindCar.class);
//                    }
//                } else {
//                    application.getMainActivity().showLoading();
//                    AVQuery<AVObject> query = new AVQuery<AVObject>("BindVeh");
//                    query.whereEqualTo("userId", user.getObjectId()).getFirstInBackground(new GetCallback<AVObject>() {
//                        @Override
//                        public void done(AVObject avObject, AVException e) {
//                            application.getMainActivity().hideLoading();
//                            if (e == null) {
//                                if (avObject == null) {
//                                    Intent i = new Intent(getActivity(), BindCar.class);
//                                    startActivity(i);
//                                } else {
//                                    Simple s = Simple.getByKey("vio");
//                                    JSONObject json = new JSONObject();
//                                    try {
//                                        boolean tempFlag = true;
//                                        String hphm = "A" + avObject.getString("hphm");
//                                        String hpzl = avObject.getString("hpzl");
//                                        if (!StringUtils.isEmpty(s.value)) {
//                                            JSONObject oldJson = new JSONObject(s.value);
//                                            if (oldJson.getString("hphm").equals(hphm) && oldJson.getString("hpzl").equals(hpzl)) {
//                                                tempFlag = false;
//                                            }
//                                        }
//                                        if (tempFlag) {
//                                            json.put("hphm", hphm);
//                                            json.put("hpzl", hpzl);
//                                            json.put("clsbdh", avObject.getString("clsbdh"));
//                                            s.value = json.toString();
//                                            s.save();
//                                        }
//                                        Intent i = new Intent(getActivity(), MyVio.class);
//                                        startActivity(i);
//                                    } catch (Exception e1) {
//                                        e1.printStackTrace();
//                                    }
//                                }
//                            } else {
//                                GlobalUtil.showNetworkError();
//                            }
//                        }
//                    });
//                }
                break;
            case R.id.profileContainer:
                if (GlobalUtil.isLogin()) {
                    i = new Intent(getActivity(), Profile.class);
                } else {
                    i = new Intent(getActivity(), Login.class);
                }
                break;
            case R.id.logoutBtn:
                AVUser.logOut();
                widgetResume();
                i = new Intent(getActivity(), Login.class);
                break;
            case R.id.myFavContainer:
                i = new Intent(getActivity(), FavoriteRoads.class);
                break;
            case R.id.myVioFeeContainer:
                i = new Intent(getActivity(), TestPush.class);
                break;
            case R.id.myPostContainer:
                if (GlobalUtil.isLogin()) {
                    i = new Intent(getActivity(), MyPost.class);
                } else {
                    i = new Intent(getActivity(), Login.class);
                }
                break;
        }
        if (i != null) {
            startActivity(i);
        }
    }
}
