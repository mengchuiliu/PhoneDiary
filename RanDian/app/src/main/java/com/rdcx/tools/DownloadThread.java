package com.rdcx.tools;

import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Zhu on 2015/12/28.
 * <p/>
 * 下载进程
 */
public class DownloadThread extends Thread {

    WeakReference<Context> reference;

    private DownloadThread(Context context) {
        this.reference = new WeakReference<>(context.getApplicationContext());
    }

    @Override
    public void run() {
        Context context;
        if (reference != null && (context = reference.get()) != null) {

            long curTime = System.currentTimeMillis();
            long startTime = DB.getDBTime(DB.TYPE_LAST_WEEK, curTime).get("start");
            long endTime = DB.getDBTime(DB.TYPE_TODAY, curTime).get("end");

            Download.downloadOperation(context, startTime, endTime);

        }
    }

    private static DownloadThread instance;

    public static void start(Context context) {
        synchronized (SyncThread.class) {
            if (instance != null && instance.isAlive()) {
                Log.d("test", "下载数据的进程已经在运行中。。。");
            } else {
                Log.d("test", "启动下载数据的进程：");
                instance = new DownloadThread(context);
                instance.start();
            }
        }
    }

}