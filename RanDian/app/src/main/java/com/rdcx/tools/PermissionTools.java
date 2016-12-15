package com.rdcx.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by Administrator on 2015/11/25 0025.
 * <p/>
 * 对系统各种权限进行判断或操作的一个类
 */
public class PermissionTools {

    /**
     * 从系统中判断是否有“查看应用使用情况”的权限
     *
     * @param context Context 对象
     * @return 是否有“查看应用使用情况”的权限
     */
    @TargetApi(21)
    public static boolean usageStats(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName()
                    , PackageManager.GET_META_DATA);
            AppOpsManager appOpsManager = ((AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE));
            int permission = appOpsManager.checkOpNoThrow("android:get_usage_stats", applicationInfo.uid,
                    applicationInfo.packageName);
            return permission == 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkPermission(final Context context, final String permission, final String reason, boolean needRequest) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (needRequest && context instanceof Activity) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    MessageTools.showMessageOKCancel((Activity) context, reason, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, 1);
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, 1);
                }
            }
            return false;
        } else {
            return true;
        }
    }

}
