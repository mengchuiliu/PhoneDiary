package com.rdcx.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.rdcx.randian.BlankActivity;
import com.rdcx.randian.MyApplication;
import com.rdcx.tools.App;
import com.rdcx.tools.DB;
import com.rdcx.tools.Operation;
import com.rdcx.tools.SP;
import com.rdcx.tools.ServiceUtils;
import com.rdcx.tools.SyncThread;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class MainService extends Service {

    @Override
    public void onTrimMemory(int level) {
        ServiceUtils.keepBackupService(this);
    }

    @Override
    public void onLowMemory() {
        ServiceUtils.keepBackupService(this);
    }

    /**
     * 需要被过滤掉的包名的正则表达式
     */
    private static final String[] filterPatternStrings = {"^com.miui.home$", "^com.android.providers.telephony$",
            "^com.android.gsf$", "^com.android.htccontacts$", "^com.android.location$", "^com.android.psclient$", "" +
            ".+\\.home$", ".+\\.launcher$", ".+\\.settings$", ".+\\.packageinstaller$"};

    /**
     * 所有需要被过滤掉的包名的正则表达式
     */
    private static Pattern filterPattern = null;

    private PackageManager pm = null;
    private KeyguardManager mKeyguardManager = null;
    private MyReceiver myReceiver = null;
    private Timer timer;
    private OperationTimerTask timerTask;
    private CheckTimerTask checkTimerTask;
    private final Object lockObject = new Object();
    private boolean getterFlag = false;

    // 重新生成 PackageNameGetter
    public void refreshPackageGetter() {
        getterFlag = true;
    }

    public MainService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(MyReceiver.REFRESH_PACKAGE_GETTER);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        myReceiver = new MyReceiver(this);
        registerReceiver(myReceiver, intentFilter);

        pm = this.getPackageManager();
        mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        // 开启每隔一秒获取一次当前进程的定时器
        timer = new Timer();
        startTimer();

        ServiceUtils.keepBackupService(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("test", "MainService start command.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("test", "MainService destroy.");
        stopTimer();
        unregisterReceiver(myReceiver);
        ServiceUtils.keepBackupService(this);
    }

    /**
     * 获取抓取的数据
     *
     * @return 抓取到的数据集合
     */
    public LinkedList<Operation> getOperationList() {
        return ((MyApplication) getApplication()).getOperationList();
    }

    private Operation screenOnOperation = null;

    /**
     * 获取屏幕开启的数据
     *
     * @return 屏幕开启的数据集合
     */
    public ArrayList<Operation> getScreenOperationList(long time) {
        ArrayList<Operation> screenOnOperationList = new ArrayList<>();

        long duration = Math.max(1000, (time - screenOnOperation.time) / 1000 * 1000);
        long maxDuration = (INTERVAL_ONE_HOUR - screenOnOperation.time % INTERVAL_ONE_HOUR + 1000) / 1000 * 1000;
        if (duration > maxDuration) {
            screenOnOperation.duration = maxDuration;
            screenOnOperationList.add(screenOnOperation);

            Operation otherOperation = new Operation();
            otherOperation.packageName = screenOnOperation.packageName;
            otherOperation.time = screenOnOperation.time + maxDuration;
            otherOperation.duration = duration - maxDuration;
            screenOnOperationList.add(otherOperation);

            screenOnOperation = otherOperation;
        } else {
            screenOnOperation.duration = duration;
            screenOnOperationList.add(screenOnOperation);
        }
        return screenOnOperationList;
    }

    /**
     * 开启定时器
     */
    public void startTimer() {
        synchronized (lockObject) {
            if (isUserPresent()) {

                if (checkTimerTask == null) {
                    checkTimerTask = new CheckTimerTask();
                    timer.schedule(checkTimerTask, 0L, 60000L);
                    Log.d("test", "后台监视定时器启动成功。");
                } else {
                    Log.d("test", "后台监视定时器已经启动，无需再次启动。");
                }

                if (timerTask == null) {
                    timerTask = new OperationTimerTask();
                    timer.schedule(timerTask, 0L, 1000L);
                    Log.d("test", "定时器启动成功。");

                    screenOnOperation = new Operation();
                    screenOnOperation.packageName = Operation.SCREEN_ON;
                    screenOnOperation.time = System.currentTimeMillis();
                    screenOnOperation.duration = 1000L;
                    Log.d("test", "屏幕点亮时间从此开始：");
                } else {
                    Log.d("test", "定时器正在运行中，不需要再次启动。");
                }
            }
        }
    }

    /**
     * 关闭定时器
     * 在关闭定时器的时候要将已经抓取的数据写入数据库中
     */
    public void stopTimer() {
        synchronized (lockObject) {
            if (timerTask == null) {
                Log.d("test", "定时器已经被关闭了，不能再次被关闭。");
            } else {
                timerTask.cancel();
                timerTask = null;
                Log.d("test", "定时器被关闭了。");
                Operation.insertOperationList(this, getScreenOperationList(System.currentTimeMillis()), 0, 100);
                Operation.insertOperations(this, getOperationList());
            }
        }
    }

    /**
     * 相隔多长时间真正检测一次屏幕是否点亮，也即相隔多长时间写入抓取到的数据
     */
    private final static long INTERVAL_ONE_MINUTE = 1000 * 60;

    /**
     * 相隔一小时
     */
    private final static long INTERVAL_ONE_HOUR = INTERVAL_ONE_MINUTE * 60;

    /**
     * 相隔一天
     */
    private final static long INTERVAL_ONE_DAY = INTERVAL_ONE_HOUR * 24;

    /**
     * 判断当前屏幕是否是亮的
     *
     * @return 屏幕是否是亮的
     */
    public boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= 20) {
            return ((PowerManager) getSystemService(POWER_SERVICE)).isInteractive();
        } else {
            return ((PowerManager) getSystemService(POWER_SERVICE)).isScreenOn();
        }
    }

    /**
     * 长驻定时器，该定时器会一直在后台运行
     * 每隔一分钟判断一次抓取前台应用的定时器是否需要开启或关闭
     */
    class CheckTimerTask extends TimerTask {

        private long markCheckTimerHour = 0;

        private long markCheckTimerDay = 0;

        @Override
        public void run() {
            try {
                boolean isScreenOn;
                if (isScreenOn = isScreenOn()) {
                    Log.w("test", "CheckTimerTask->:检测到系统的屏幕已经打开了，打开定时器。");
                    startTimer();
                } else {
                    Log.w("test", "CheckTimerTask->:检测到系统的屏幕已经关闭了，关闭定时器。");
                    stopTimer();
                }
                long time = System.currentTimeMillis();
                if (time - markCheckTimerHour >= INTERVAL_ONE_HOUR) {
                    markCheckTimerHour = time;
                    int syncThreadType = SyncThread.TYPE_ONE_HOUR;

                    if (time - markCheckTimerDay >= INTERVAL_ONE_DAY) {
                        markCheckTimerDay = time;
                        syncThreadType |= SyncThread.TYPE_ONE_DAY;
                    }

                    if (!isScreenOn) {
                        if (Pattern.matches("^MI.*$", Build.MODEL)) {
                            Intent dialogIntent = new Intent(MainService.this, BlankActivity.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MainService.this.startActivity(dialogIntent);
                        }
                    }
                    SyncThread.start(MainService.this, syncThreadType);
                }
            } catch (Exception e) {
                Log.e("test", "CheckTimerTask在运行时出来异常：", e);
            }
        }

    }

    /**
     * 判断当前是否非锁屏且用户已解锁
     *
     * @return 亮屏且用户已解锁
     */
    public boolean isUserPresent() {
        return !mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 上一次检测屏幕是否是点亮的时间点，也即上一次写入抓取到的数据的时间点
     */
    private long markOneMinute = 0;

    private long markOneHour = 0;

    private long markOneDay = 0;

    /**
     * 判断屏幕是否是点亮的，顺便将已抓取到的数据写入到数据库中
     * 如果在一分钟之内没有判断过，则需要重新向系统发送请求来判断一次，此时会顺便将已经抓取到的数据保存到数据库中
     * 如果在一分钟之内判断过，则不需要向系统发送请求，而是直接返回屏幕的状态是点亮的
     * 相当于每隔一分钟才真正向系统判断一次，以减少时间的消耗，节省电量
     * 当屏幕熄灭时，会有系统广播来控制定时器的关闭，所以判断屏幕是否点亮不是必要的，
     * 只是为了以防万一，系统广播没有收到的情况下，在屏幕熄灭后也能关掉定时器
     *
     * @param time 当前时间
     * @return 屏幕是否关闭
     */
    public boolean operationTimerTask(long time) {
        if (time - markOneMinute >= INTERVAL_ONE_MINUTE) {
            markOneMinute = time;
//            int syncThreadType = SyncThread.TYPE_ONE_MINUTE;

//            if (time - markOneHour >= INTERVAL_ONE_HOUR) {
//                markOneHour = time;
//                syncThreadType |= SyncThread.TYPE_ONE_HOUR;
//
//                if (time - markOneDay >= INTERVAL_ONE_DAY) {
//                    markOneDay = time;
//                    syncThreadType |= SyncThread.TYPE_ONE_DAY;
//                }
//            }
            try {
                // 每分钟进行的操作
                boolean isUserPresent = isUserPresent();
                if (isUserPresent) {
                    Operation.insertOperationList(this, getScreenOperationList(time), 0, 100);
                    Operation.insertOperations(this, getOperationList());
                }
//                SyncThread.start(this, syncThreadType);
                RandianNotification.checkNotificationRules(this, time);
                return isUserPresent;
            } catch (Exception e) {
                Log.e("test", "系统出现问题了，获取不到屏幕是否是点亮的。");
                return false;
            }
        }
        return true;
    }

    /**
     * 用于每隔一定时间抓取前台应用的定时器
     */
    class OperationTimerTask extends TimerTask {

        public LinkedList<Operation> operationList = getOperationList();
        Operation operation = null;
        String lastPackageName = null;
        long startTime;
        long maxDuration;

        // 抓取成功的次数
        int getPackageNameSuccessTime = 0;
        // 抓取失败的次数
        int getPackageNameFailedTime = 0;

        @Override
        public void run() {

            startTime = System.currentTimeMillis();
            if (!operationTimerTask(startTime)) {
                return;
            }
            String packageName = PackageNameGetter.getFrontPackageName(MainService.this, getterFlag);
            if (getterFlag) {
                getterFlag = false;
            }
            if (packageName == null) {
                lastPackageName = null;
                if (++getPackageNameFailedTime == 3) { // 超过3次抓取不到数据，则认为没有获取到权限，抓取不到数据
                    getPackageNameSuccessTime = 0;
                    SP.set(MainService.this, SP.USAGE_GET, false);
                }
                Log.w("test", "系统出现问题，获取不到前台应用。");
            } else {
                if (packageName.equals(lastPackageName) && operation.duration < maxDuration) {
                    operation.duration = ((startTime - operation.time) / 1000) * 1000;
                } else {
                    if (needFilter(packageName)) {
                        lastPackageName = null;
                        operation = null;
                        Log.w("test", "这是一个系统应用，不予统计：" + packageName);
                    } else {
                        lastPackageName = packageName;

                        operation = new Operation();
                        operation.time = startTime;
                        operation.packageName = lastPackageName;
                        operation.duration = 1000L;
                        operationList.offer(operation);

                        maxDuration = INTERVAL_ONE_HOUR - startTime % INTERVAL_ONE_HOUR - 1000;
                    }
                }
                if (operation != null) {
                    Log.w("test", "包名：" + operation.packageName + "，启动时间：" + operation.time + "，持续时间：" + operation.duration);
                }
                if (++getPackageNameSuccessTime == 1) { // 超过1次抓取都能抓到数据，则认为可以抓取到数据
                    getPackageNameFailedTime = 0;
                    SP.set(MainService.this, SP.USAGE_GET, true);
                }
            }
            Log.w("test", "耗时： " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    /**
     * 获取所有需要过滤掉的应用
     * 首先获取系统启动应用，然后再将需要过滤掉的正则表达式依次添加进去，合成一个链表
     *
     * @return 所有需要被过滤掉的应用的集合
     */
    public Pattern getFilterPatters() {
        if (filterPattern == null) {
            ArrayList<String> filterPatterns = new ArrayList<>();
            for (String intentStr : new String[]{Intent.ACTION_MAIN, Intent.ACTION_DIAL}) {
                Intent intent = new Intent(intentStr);
                if (Intent.ACTION_MAIN.equals(intentStr)) {
                    intent.addCategory("android.intent.category.HOME");
                }
                for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
                    filterPatterns.add("^" + resolveInfo.activityInfo.packageName + "$");
                }
            }
            // 过滤掉系统内的输入法。
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> inputMethodInfoList = imm.getInputMethodList();
            for (InputMethodInfo inputMethodInfo : inputMethodInfoList) {
                filterPatterns.add("^" + inputMethodInfo.getPackageName() + "$");
            }
            //------------------------------------------------------------------------------------

            Collections.addAll(filterPatterns, filterPatternStrings);

            StringBuilder sb = new StringBuilder();
            for (String str : filterPatterns) {
                sb.append("(").append(str).append(")").append("|");
            }
            sb.deleteCharAt(sb.lastIndexOf("|"));
            filterPattern = Pattern.compile(sb.toString());


        }
        return filterPattern;
    }

    private HashMap<String, Boolean> packageNameCacheMap = null;

    /**
     * 判断包名是否需要被过滤掉
     *
     * @param packageName 包名
     * @return 是否需要被过滤掉
     */
    private boolean needFilter(String packageName) {
        if (packageNameCacheMap == null) {
            packageNameCacheMap = new HashMap<>();
            List<App> appList = App.selectApp(this, "~");
            for (App app : appList) {
                packageNameCacheMap.put(app.packageName, false);
            }
        }
        Boolean needFilter;
        if ((needFilter = packageNameCacheMap.get(packageName)) == null) {
            packageNameCacheMap.put(packageName, (needFilter = getFilterPatters().matcher(packageName).matches()));

            App app = new App();
            app.packageName = packageName;
            if (needFilter) {
                App.deleteApp(this, app);
            } else {
                App.insertOrUpdateApp(this, app);
            }
        }
        return needFilter;
    }

    /**
     * 将抓取到的数据写入数据库中
     */
    public void refreshOneHour() {

        SyncThread.start(this, SyncThread.TYPE_ONE_HOUR);

    }

}
