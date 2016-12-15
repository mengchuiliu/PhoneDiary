package com.rdcx.tools;

import android.text.format.DateFormat;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/4 0004.
 * <p/>
 * 日记对象
 */
public class Diary implements Serializable {

    private static final long serialVersionUID = 3661334468112055777L;
    /**
     * ID
     */
    public int id;

    /**
     * 时间
     */
    public long time;

    /**
     * 编辑文本
     */
    public String text;

    /**
     * 图片路径
     */
    public String path;

    /**
     * 维度与数据组合成的JSON格式
     */
    public String data;

    /**
     * 维度组合文本
     */
    public String datatext;

    /**
     * 数据是否上传到服务器,0表示没上传，1表示上传
     */
    public int upload;

    /**
     * 服务器当前时间
     */
    public long nowDate;

    @Override
    public String toString() {
        return "Diary{" +
                "id=" + id +
                ", time=" + DateFormat.format("yyyy-MM-dd HH:mm:ss", time) +
                ", text='" + text + '\'' +
                ", path='" + path + '\'' +
                ", data='" + data + '\'' +
                ", datatext='" + datatext + '\'' +
                ", upload=" + upload +
                ", nowDate=" + DateFormat.format("yyyy-MM-dd HH:mm:ss", nowDate) +
                '}';
    }
}
