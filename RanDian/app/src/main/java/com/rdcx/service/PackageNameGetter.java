package com.rdcx.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.rdcx.tools.PermissionTools;
import com.rdcx.tools.SP;

import java.util.List;


/**
 * Created by Administrator on 2015/12/3 0003.
 * <p/>
 * 前台应用获取者
 */
public class PackageNameGetter {

    /**
     * 抓取当前正在运行的程序的接口
     */
    private interface Getter {
        String get();
    }

    /**
     * 抓取当前正在运行的程序接口的实例
     */
    private static Getter getter = null;

    /**
     * 根据不同版本的 Android 系统，分别有不同的抓取当前正在运行的程序的方法。
     *
     * @return 抓取当前正在运行的程序的不同实例
     */
    public static String getFrontPackageName(final MainService mainService, boolean refresh) {
        if (refresh || getter == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                Log.w("test", "Android 5.0 及以上版本：");
                boolean permission = PermissionTools.usageStats(mainService);
                SP.set(mainService, SP.USAGE_PERMISSION, permission);
                Log.d("test", "permission =>:" + permission);
                if (permission) {

                    // Android 5.0 以上版本，且有“查看应用使用情况”的权限时使用的抓取当前应用的方法
                    Log.w("test", "获取到了“查看应用使用情况”的权限");
                    getter = new Getter() {

                        private String packageName = null;

                        /**
                         * 保存 UsageStatsManager 对象，无需重复获取。
                         */
                        private UsageStatsManager usageStatsManager = null;

                        /**
                         * 获取 UsageStatsManager 对象，无需重要获取。
                         * @param context Context对象
                         * @return 应用使用数据管理对象
                         */
                        @TargetApi(21)
                        private UsageStatsManager getUsageStatsManager(Context context) {
                            if (usageStatsManager == null) {
                                usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                            }
                            return usageStatsManager;
                        }

                        @TargetApi(21)
                        @Override
                        public String get() {
                            long curTime = System.currentTimeMillis();
                            List<UsageStats> usageStatsList = getUsageStatsManager(mainService).queryUsageStats(UsageStatsManager.INTERVAL_DAILY, curTime - 1000L * 60, curTime);
                            Log.d("test", "usageStatsList.size()->:" + usageStatsList.size());
                            if (usageStatsList.size() > 0) {
                                UsageStats lastUsageStats = null;
                                long lastTime = 0;
                                for (UsageStats usageStats : usageStatsList) {
                                    if (usageStats.getLastTimeUsed() > lastTime) {
                                        lastUsageStats = usageStats;
                                        lastTime = usageStats.getLastTimeUsed();
                                    }
                                }
                                if (lastUsageStats != null) {
                                    return (packageName = lastUsageStats.getPackageName());
                                }
                            }
                            return packageName;
                        }
                    };

                } else {

                    // Android 5.0 以上版本，没有“查看应用使用情况”的权限时采取的抓取当前应用的方法
                    // 有些 Android 5.0 以上版本，并没有对 am.getRunningAppProcesses() 进行权限控制，可以顺利获取到数据。
                    // 有些 Android 5.0 以上版本，对 am.getRunningAppProcesses() 有权限控制，此时该方法将只能获取到该应用本身这一条数据，此时该方法将获取不到应用使用情况。
                    // 如果该方法失灵时，只能提示用户打开“查看应用使用情况”这条权限。
                    Log.w("test", "未获取到“查看应用使用情况”的权限");
                    getter = new Getter() {

                        private ActivityManager am = null;

                        @Override
                        public String get() {
                            if (am == null) {
                                am = (ActivityManager) mainService.getSystemService(Context.ACTIVITY_SERVICE);
                            }
                            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = am.getRunningAppProcesses();
                            Log.e("test", "runningAppProcessInfoList.size()->:" + runningAppProcessInfoList.size());
                            // 等于一条时表示只能获取当前应用
                            // 多于一条时才表示能够获取到所有正在运行的应用
                            if (runningAppProcessInfoList.size() > 5) {
                                String[] pkgList = runningAppProcessInfoList.get(0).pkgList;
                                if (pkgList.length > 0) {
                                    return pkgList[0];
                                }
                            }
                            return null;
                        }
                    };

                }
            } else {
                Log.w("test", "Android 5.0 以下版本：");
                // Android 5.0 以下版本抓取当前运行应用的方法
                getter = new Getter() {

                    private ActivityManager am = null;

                    @SuppressWarnings("deprecation")
                    @Override
                    public String get() {
                        if (am == null) {
                            am = (ActivityManager) mainService.getSystemService(Context.ACTIVITY_SERVICE);
                        }
                        return am.getRunningTasks(1).get(0).topActivity.getPackageName();
                    }
                };
            }
        }
        return getter.get();
    }

}
