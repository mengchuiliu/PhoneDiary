package com.rdcx.bean;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/5/9 0009.
 *
 * @author mengchuiliu
 */
public class TaskRankFriend {
    public String userId;
    public String nickName;
    public String taskCount;
    public Bitmap taskIcon;

    @Override
    public String toString() {
        return "TaskRankFriend{" +
                "nickName='" + nickName + '\'' +
                ", taskCount='" + taskCount + '\'' +
                ", taskIcon=" + taskIcon +
                '}';
    }
}
