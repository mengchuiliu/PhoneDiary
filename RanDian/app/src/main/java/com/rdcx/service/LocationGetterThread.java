package com.rdcx.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.rdcx.tools.DB;
import com.rdcx.tools.LocationInfo;
import com.rdcx.tools.SP;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2015/12/2 0002.
 * <p/>
 * 获取当前位置信息的线程
 */
public class LocationGetterThread extends Thread implements AMapLocationListener {

    WeakReference<Context> reference;

    LocationManagerProxy mLocationManagerProxy;
    long time = 0;
    boolean alreadySet = false;

    private LocationGetterThread(Context context, long time) {
        this.reference = new WeakReference<>(context);
        this.time = time;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void run() {
        try {

            final Context context;
            if (reference == null || (context = reference.get()) == null) {
                return;
            }
            Location location = null;
            // 初始化定位，只采用网络定位
            mLocationManagerProxy = LocationManagerProxy.getInstance(context);
            mLocationManagerProxy.setGpsEnable(false);

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用destroy()方法
            // 其中如果间隔时间为-1，则定位只定一次,
            // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
            mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);
            for (int i = 0; i < 60 * 5 && !alreadySet; i++) {
                Thread.sleep(1000);
            }
        } catch (SecurityException e) {
            Log.e("test", "LocationGetterThread run occurs a Exception=>:", e);
        } catch (Exception e) {
            Log.e("test", "LocationGetterThread run occurs a Exception=>:", e);
        } finally {
            try {
                mLocationManagerProxy.removeUpdates(this);
                mLocationManagerProxy.destroy();
                mLocationManagerProxy = null;
            } catch (SecurityException e) {
                Log.e("test", "LocationGetterThread run occurs a Exception=>:", e);
            }
            if (!alreadySet) {
                setLocation(time, null);
            }
        }

    }

    /**
     * 获取用户当前的位置信息，并写入数据库
     */
    public void setLocation(long time, Location location) {
        final Context context;
        if (reference == null || (context = reference.get()) == null) {
            return;
        }
        if (location == null) {
            Log.d("test", "获取位置信息时失败。");
            SP.increment(context, "getLocation", 0);
        } else {
            LocationInfo.insertLocation(context, location);
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.d("test", "LocationGetterThread->onLocationChanged aMapLocation=>:" + aMapLocation);
        if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            setLocation(time, aMapLocation);
            alreadySet = true;
        } else {
            Log.d("test", "LocationGetterThread->onLocationChanged aMapLocation.getAMapException().getErrorCode()=>:" + aMapLocation);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("test", "LocationGetterThread->onLocationChanged location=>:" + location);
        setLocation(time, location);
        alreadySet = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("test", "LocationGetterThread->onStatusChanged.");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("test", "LocationGetterThread->onProviderEnabled.");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("test", "LocationGetterThread->onProviderDisabled.");
    }

    /**
     * 获取用户位置信息线程的实例
     */
    private static LocationGetterThread instance;

    private static long lastLocationTime = 0;

    public static void start(Context context, long time) {
        synchronized (LocationGetterThread.class) {
            if (time - lastLocationTime < 1000 * 60 * 10) {
                Log.d("test", "必须间隔十分钟才能再次获取当前位置信息！");
                return;
            }
            lastLocationTime = time;
            if (instance != null && instance.isAlive()) {
                Log.d("test", "获取当前位置信息的进程已经在运行中。。。");
            } else {
                Log.d("test", "启动获取当前位置信息的进程：");
                instance = new LocationGetterThread(context, time);
                instance.start();
            }
        }
    }

}
