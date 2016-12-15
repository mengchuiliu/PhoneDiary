package com.rdcx.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.rdcx.bean.DiaryImageInfo;
import com.rdcx.randian.R;
import com.rdcx.utils.Constants;
import com.rdcx.utils.ImageDisplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/2 0002.
 *
 * @author mengchuiliu
 */
public class DiaryImageAdapter extends BaseAdapter {
    private List<DiaryImageInfo> mDataList = new ArrayList<>();
    private Context mContext;

    public DiaryImageAdapter(Context context) {
        this.mContext = context;

    }

    public void setData(List<DiaryImageInfo> dataList) {
        this.mDataList = dataList;
    }

    @Override
    public int getCount() {
        // 多返回一个用于展示添加图标
        if (mDataList == null) {
            return 1;
        } else if (mDataList.size() >= Constants.MAX_IMAGE_SIZE) {
            return Constants.MAX_IMAGE_SIZE;
        } else {
            return mDataList.size() + 1;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mDataList != null && mDataList.size() == Constants.MAX_IMAGE_SIZE) {
            return mDataList.get(position);
        } else if (mDataList == null || position - 1 < 0 || position > mDataList.size()) {
            return null;
        } else {
            return mDataList.get(position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 所有Item展示不满一页，就不进行ViewHolder重用了，避免了一个拍照以后添加图片按钮被覆盖的奇怪问题
        convertView = View.inflate(mContext, R.layout.item_diary_image, null);
        ImageView imageIv = (ImageView) convertView.findViewById(R.id.item_grid_image);

        if (isShowAddItem(position)) {
            imageIv.setImageResource(R.drawable.btn_add_pic);
        } else {
            final DiaryImageInfo item = mDataList.get(position);
            ImageDisplayer.getInstance(mContext).displayBmp(imageIv, item.thumbnailPath, item.sourcePath);
        }
        return convertView;
    }

    private boolean isShowAddItem(int position) {
        int size = mDataList == null ? 0 : mDataList.size();
        return position == size;
    }
}
