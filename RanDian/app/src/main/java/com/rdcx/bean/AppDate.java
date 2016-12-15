package com.rdcx.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2015/12/4 0004.
 * 应用信息
 */
public class AppDate {
    Drawable icon;
    int percent;
    int time;
    int count;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
