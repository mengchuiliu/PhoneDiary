package com.rdcx.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rdcx.service.BackupService;
import com.rdcx.service.MainService;

import java.util.List;

/**
 * Created by Administrator on 2015/11/20 0020.
 * <p/>
 * 对 Service 的一些工作类的合集
 */
public class ServiceUtils {

    /**
     * 用于判断某个 Service 是否在运行
     *
     * @param context   Context 对象
     * @param className 服务名
     * @return 是否在运行
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(50);

        if (null == serviceInfos || serviceInfos.size() < 1) {
            return false;
        }

        for (int i = 0; i < serviceInfos.size(); i++) {
            if (serviceInfos.get(i).service.getClassName().contains(className)) {
                isRunning = true;
                break;
            }
        }
        Log.e("test", "ServiceUtil===>" + className + " isRunning =  " + isRunning);
        return isRunning;
    }

    public static void keepBackupService(Context context) {
        keepService(context, "com.rdcx.service.BackupService", BackupService.class);
    }

    public static void keepMainService(Context context) {
        keepService(context, "com.rdcx.service.MainService", MainService.class);
    }

    public static void keepService(Context context, String packageName, Class<?> clazz) {
        boolean isRun = ServiceUtils.isServiceRunning(context, packageName);
        if (!isRun) {
            context.startService(new Intent(context, clazz));
            Log.d("test", "重启服务：" + packageName);
        }
    }

}
