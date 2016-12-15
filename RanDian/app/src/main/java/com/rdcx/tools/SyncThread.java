package com.rdcx.tools;

import android.content.Context;
import android.util.Log;

import com.rdcx.service.LocationGetterThread;
import com.rdcx.service.RandianNotification;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2015/12/14 0014.
 * <p/>
 * 同步进程，将数据进行同步操作
 */
public class SyncThread extends Thread {

    // 每小时需要执行的代码
    public static final int TYPE_ONE_HOUR = 0x2;

    // 每天需要执行的代码
    public static final int TYPE_ONE_DAY = 0x4;

    private int type;

    WeakReference<Context> reference;

    private SyncThread(Context context, int type) {
        this.reference = new WeakReference<>(context.getApplicationContext());
        this.type = type;
    }

    @Override
    public void run() {
        try {
            Context context;
            if (reference != null && (context = reference.get()) != null) {
                long curTime = System.currentTimeMillis();

                if ((type & TYPE_ONE_DAY) > 0) {

                    // 同步文案
                    DB.syncText(context);

                    // 对数据库中所有 App 进行重新分类操作，以便与后台更改的分类进行同步。
                    App.syncApp(context, true);
                }

                if ((type & TYPE_ONE_HOUR) > 0) {
                    // 获取足迹信息
                    LocationGetterThread.start(context, curTime);

                    // 同步通话记录
                    Call.syncPhone(context, curTime);

                    // 同步图片信息
                    DB.syncImage(context, curTime);

                    // 上传通话记录
                    Upload.uploadCall(context);

                    // 上传应用使用记录
                    Upload.uploadOperation(context);

                    // 上传图片信息
                    Upload.uploadImageInfo(context);

                    // 上传足迹信息
                    Upload.uploadLocation(context);

                    // 检测任务清单中的任务有没有完成，每隔一小时检测一次
                    TaskTools.checkAllTask(context);
                }
            }
        } catch (Exception e) {
            Log.e("test", "SyncThread run occurs a Exception=>:" + e);
            e.printStackTrace();
        }
    }

    private static SyncThread instance;

    public static void start(Context context, int type) {
        synchronized (SyncThread.class) {
            if (instance != null && instance.isAlive()) {
                Log.d("test", "同步数据的进程已经在运行中。。。");
            } else {
                Log.d("test", "启动同步数据的进程：");
                instance = new SyncThread(context, type);
                instance.start();
            }
        }
    }

}
