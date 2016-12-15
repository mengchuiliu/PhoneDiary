package com.rdcx.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rdcx.randian.MyApplication;
import com.rdcx.randian.R;
import com.rdcx.randian.SetActivity;
import com.rdcx.randian.UpdateUserInfo;
import com.rdcx.randian.WebHtmlActivity;
import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;
import com.rdcx.utils.Utils;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/11/24 0024.
 *
 * @author mengchuiliu
 */
@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class UserInfoFragment extends Fragment {
    View view;
    ImageView user_icon;
    TextView nickname, tv_userId, search, discover, community, set, skin, shareApp;
    String userId;
    AlertDialog dialog;
    JSONArray flashPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_info, null);
        user_icon = (ImageView) view.findViewById(R.id.user_icon);
        nickname = (TextView) view.findViewById(R.id.nickname);
        tv_userId = (TextView) view.findViewById(R.id.tv_userId);
        search = (TextView) view.findViewById(R.id.search);
        discover = (TextView) view.findViewById(R.id.discover);
        community = (TextView) view.findViewById(R.id.community);
        String html = "<a href='http://buluo.qq.com/p/barindex.html?bid=301946'>\t</a>" + "<font color='#52B6A4'>\t微社区</font>" + "<a href='http://buluo.qq.com/p/barindex.html?bid=301946'>\t</a>";
        CharSequence charSequence = Html.fromHtml(html);
        community.setText(charSequence);
        community.setMovementMethod(LinkMovementMethod.getInstance());
        set = (TextView) view.findViewById(R.id.set);
        skin = (TextView) view.findViewById(R.id.skin);
        shareApp = (TextView) view.findViewById(R.id.shareApp);
        String flashPageStr = SP.getString(this.getContext(), SP.FLASH_PAGE, null);
        if (flashPageStr != null) {
            try {
                flashPage = new JSONArray(flashPageStr);
                long currentTime = System.currentTimeMillis();
                String userId = SP.getString(getContext(), SP.USER_ID, null);
                for (int i = 0, len = flashPage.length(); i < len; i++) {
                    JSONObject jo = flashPage.getJSONObject(i);

                    // 当前时间不在活动时间范围之内时，不需要显示该菜单
                    if (currentTime < jo.getJSONObject("startDate").getLong("time") || currentTime > jo.getJSONObject("endDate").getLong("time")) {
                        continue;
                    }

                    // userIds 存在且不为空时：包含当前用户的ID则显示菜单，不包含当前用户的ID则不显示菜单
                    String userIds = jo.getString("userIds");
                    if (userIds != null && userIds.length() > 0) {
                        if (!(userIds.startsWith(userId + ",") || userIds.contains("," + userId + ",") || userIds.endsWith("," + userId))) {
                            continue;
                        }
                    }

                    TextView tv = (TextView) inflater.inflate(R.layout.item_user_info, null);
                    tv.setText("    " + jo.getString("text"));
                    tv.setOnClickListener(onClickListener);
                    ((ViewGroup) (community.getParent())).addView(tv);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        user_icon.setImageBitmap(Utils.getPortraitBitmap(getActivity(), handler, true));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        user_icon.setOnClickListener(onClickListener);
        search.setOnClickListener(onClickListener);
        discover.setOnClickListener(onClickListener);
//        community.setOnClickListener(onClickListener);
        set.setOnClickListener(onClickListener);
//        skin.setOnClickListener(onClickListener);
        shareApp.setOnClickListener(onClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        String name = SP.getString(getActivity(), "nickName", "");
        userId = SP.getString(getActivity(), SP.USER_ID, "-1");
        if (TextUtils.isEmpty(name)) {
            name = Build.MODEL;
        }
        nickname.setText(name);
        tv_userId.setText(userId);
        user_icon.setImageBitmap(Utils.getPortraitBitmap(getActivity(), handler, false));

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.user_icon://头像
                    startActivity(new Intent(getActivity(), UpdateUserInfo.class));
                    break;
                case R.id.search://搜索
                    Intent intent = new Intent(getActivity(), WebHtmlActivity.class);
                    intent.putExtra("search", true);
                    startActivity(intent);
                    break;
                case R.id.discover://发现
                    Intent intent1 = new Intent(getActivity(), WebHtmlActivity.class);
                    intent1.putExtra("discover", true);
                    startActivity(intent1);
                    break;
                case R.id.community://发送消息到qq部落群
//                    final Bundle params1 = new Bundle();
//                    params1.putString(GameAppOperation.QQFAV_DATALINE_APPNAME, "手机日记");
//                    params1.putString(GameAppOperation.QQFAV_DATALINE_TITLE, "我的吐槽");
//                    params1.putString(GameAppOperation.QQFAV_DATALINE_DESCRIPTION, "手机日记的测试吐槽必须十个字");
//                    params1.putString(GameAppOperation.TROOPBAR_ID, "301946");
//                    MyApplication.mTencent.shareToTroopBar(getActivity(), params1, new IUiListener() {
//                        @Override
//                        public void onComplete(Object o) {
//                            Toast.makeText(getActivity(), "QQ部落成功！", Toast.LENGTH_SHORT).show();
//                        }
//                        @Override
//                        public void onError(UiError uiError) {
//                            Toast.makeText(getActivity(), "code:" + uiError.errorCode + ", msg:"
//                                    + uiError.errorMessage + uiError.errorDetail, Toast.LENGTH_SHORT).show();
//                        }
//                        @Override
//                        public void onCancel() {
//                            Toast.makeText(getActivity(), "QQ部落取消！", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    break;
                case R.id.set://设置
                    startActivity(new Intent(getActivity(), SetActivity.class));
                    break;
                case R.id.skin://换肤
                    //                    http://weixin.qq.com/r/8kwjO3XEByh4rTFY9xng
                    break;
                case R.id.shareApp://分享App
                    showShare();
                    break;
                case R.id.share_qq://分享应用到qq
                    final Bundle params = new Bundle();
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
                    params.putString(QQShare.SHARE_TO_QQ_TITLE, "手机日记•分享");
                    params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "\n独乐乐不如众乐乐，分享“手机日记”，记录生活，感动自己。");
//                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, "http://imgcache.qq" +
//                            ".com/qzone/space_item/pre/0/66768.gif");
//                    params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "返回");
                    MyApplication.mTencent.shareToQQ(getActivity(), params, new IUiListener() {
                        @Override
                        public void onComplete(Object response) {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            Toast.makeText(getActivity(), "QQ分享应用成功！", Toast.LENGTH_SHORT).show();

                            TaskTools.addStageTask(getActivity(), 1003); // 完成阶段任务 1003：分享应用给好友一次

                        }

                        @Override
                        public void onError(UiError uiError) {
                            Toast.makeText(getActivity(), "QQ分享应用失败！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(getActivity(), "QQ分享应用取消！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case R.id.share_wx:
                    MyApplication.flag = false;
                    MyApplication.wxType = 2;
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.randian);
                    WXWebpageObject webpage = new WXWebpageObject();
                    webpage.webpageUrl = "http://www.randiancx.com/enjoy.html?from=groupmessage&isappinstalled=0";
                    WXMediaMessage msg = new WXMediaMessage(webpage);
                    msg.title = "手机日记•分享";
                    msg.description = "\n独乐乐不如众乐乐，分享“手机日记”，记录生活，感动自己。";

                    // 这里替换一张自己工程里的图片资源
                    Bitmap thumb = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                    msg.thumbData = Utils.bmpByte(thumb, true);

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = Utils.buildTransaction("webpage");
                    req.message = msg;
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                    boolean fla = MyApplication.api.sendReq(req);
                    if (!bmp.isRecycled()) {
                        bmp.recycle();
                    }
                    if (fla) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();

                            TaskTools.addStageTask(getActivity(), 1003); // 完成阶段任务 1003：分享应用给好友一次

                        }
                    } else {
                        Toast.makeText(getActivity(), "微信分享应用失败！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    if (flashPage != null) {
                        try {
                            if (v instanceof TextView) {
                                String text = ((TextView) v).getText().toString();
                                for (int i = 0, len = flashPage.length(); i < len; i++) {
                                    JSONObject jo = flashPage.getJSONObject(i);
                                    if (("    " + jo.getString("text")).equals(text)) {
                                        String url = jo.getString("url");
                                        Intent otherIntent = new Intent(getActivity(), WebHtmlActivity.class);
                                        otherIntent.putExtra("url", url);
                                        startActivity(otherIntent);
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    private void showShare() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.share_show, null);
        view.findViewById(R.id.share_qq).setOnClickListener(onClickListener);
        view.findViewById(R.id.share_wx).setOnClickListener(onClickListener);
        if (dialog == null) {
            dialog = new AlertDialog.Builder(getActivity()).setTitle("分享APP到").setIcon(R.mipmap.share_app)
                    .setView(view).setPositiveButton("取消", null).create();
        }
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 16:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) {
                            user_icon.setImageBitmap(Utils.toRoundBitmap(bitmap));
                            Utils.setPortraitBitmap(getActivity(), bitmap);
                        }
                    } else {
                        Log.e("my_log", "头像加载失败！");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApplication.mTencent.onActivityResult(requestCode, resultCode, data);
    }
}
