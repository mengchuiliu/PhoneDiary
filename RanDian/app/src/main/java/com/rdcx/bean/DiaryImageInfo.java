package com.rdcx.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/2 0002.
 *
 * @author mengchuiliu
 */
public class DiaryImageInfo implements Serializable {
    private static final long serialVersionUID = -8536360241466293216L;
    public String imageId;
    public String thumbnailPath;//缩略图路径
    public String sourcePath;//图片路径
    public boolean isSelected = false;//是否已被选中

}
