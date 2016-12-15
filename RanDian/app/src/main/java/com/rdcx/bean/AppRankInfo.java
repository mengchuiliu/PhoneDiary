package com.rdcx.bean;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/3/22 0022.
 *
 * @author mengchuiliu
 */
public class AppRankInfo {
    public int number;
    public Bitmap appIcon;
    public String appName;
    public int appCount;
    public long total;
    public String userId;

    @Override
    public String toString() {
        return "AppRankInfo{" +
                "number='" + number + '\'' +
                ", appIcon=" + appIcon +
                ", AppName='" + appName + '\'' +
                ", AppCount='" + appCount + '\'' +
                ", average='" + total + '\'' +
                '}';
    }
}
