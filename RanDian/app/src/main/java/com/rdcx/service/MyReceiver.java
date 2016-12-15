package com.rdcx.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.rdcx.randian.BlankActivity;
import com.rdcx.randian.MyApplication;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

public class MyReceiver extends BroadcastReceiver {

    public static final String REFRESH_PACKAGE_GETTER = "REFRESH_PACKAGE_GETTER";

    public static final String GET_LOCATION = "GET_LOCATION";

    private WeakReference<MainService> reference;

    public MyReceiver() {
    }

    public MyReceiver(MainService service) {
        this.reference = new WeakReference<>(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action;
        if (intent == null || (action = intent.getAction()) == null) {
            Log.e("test", "系统广播出现重大问题了，intent 或 action居然为 null 了。");
            return;
        }

        MainService mainService;
        if (reference != null && (mainService = reference.get()) != null) {
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                Log.d("test", "接收到新广播：屏幕被点亮了->:");
                mainService.startTimer();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d("test", "接收到新广播：屏幕被熄灭了->:");
                mainService.stopTimer();
            } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                Log.d("test", "接收到新广播：用户将屏幕解锁了->:");
                mainService.startTimer();
                LocationGetterThread.start(mainService, System.currentTimeMillis());
            } else if (action.equals(REFRESH_PACKAGE_GETTER)) {
                Log.d("test", "接收到新广播：刷新包名抓取器->:");
                mainService.refreshPackageGetter();
            }
        } else {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.d("test", "开机自启动。");
                Intent service = new Intent(context, MainService.class);
                context.startService(service);
            }
        }

    }
}
