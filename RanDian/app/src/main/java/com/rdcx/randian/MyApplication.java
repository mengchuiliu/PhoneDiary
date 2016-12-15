package com.rdcx.randian;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.rdcx.loction.Location;
import com.rdcx.tools.Operation;
import com.rdcx.tools.PermissionTools;
import com.rdcx.utils.Utils;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/12 0012.
 *
 * @author mengchuiliu
 */
public class MyApplication extends Application {
    private List<Activity> list = new LinkedList<Activity>();
    private static String phoneStr = null;
    public static boolean flag = true;// 区别微信分享还是登录
    public static int wxType = 0;
    // qq登录
    public static Tencent mTencent;
    public static final String QQAPP_ID = "1105043040";

    // IWXAPI 是第三方app和微信通信的openapi接口
    public static IWXAPI api;
    public static final String APP_ID = "wx15f6abdf7895a736";// 微信appId

    // 新浪web授权
    public static final String APP_KEY = "1848002151"; // 应用的APP_KEY
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";// 应用的回调页
    public static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
    public static AuthInfo mAuthInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        // qq登陆
        mTencent = Tencent.createInstance(QQAPP_ID, this.getApplicationContext());

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        api.registerApp(APP_ID);

        // 微博实例
        mAuthInfo = new AuthInfo(this, APP_KEY, REDIRECT_URL, SCOPE);

        Utils.setBitmapToCache("randian", BitmapFactory.decodeResource(getResources(), R.mipmap.randian));

    }

    public static String getPhoneStr(Context context) {
        if (phoneStr == null) {
            if (context instanceof Activity) {
                boolean permissionState = PermissionTools.checkPermission((Activity) context, Manifest.permission.READ_PHONE_STATE, "READ_PHONE_STATE 权限是必要的。", true);
                if (permissionState) {
                    //唯一标识字符
                    phoneStr = Utils.getMyUUID(context);
                } else {
                    Toast.makeText(context, "读取手机信息时出错，没有权限，请允许权限再重复前面的操作。", Toast.LENGTH_SHORT).show();
                }
            }
        }
        Log.d("test", "MyApplication getPhoneStr=>:" + phoneStr);
        return phoneStr;
    }

    public void addActivity(Activity activity) {
        list.add(activity);
    }

    public void exit() {
        try {
            for (Activity activity : list) {
                if (activity != null) {
                    try {
                        activity.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            System.exit(0);
            list.clear();
        }
    }

    public void actFinisih() {
        for (int i = list.size() - 1; i >= 0; i--) {
            try {
                list.remove(i).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Location.clear();
    }

    private LinkedList<Operation> operationList;

    public LinkedList<Operation> getOperationList() {
        if (operationList == null) {
            operationList = new LinkedList<>();
        }
        return operationList;
    }

}
