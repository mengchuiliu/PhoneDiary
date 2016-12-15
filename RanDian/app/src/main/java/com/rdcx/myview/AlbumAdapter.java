package com.rdcx.myview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rdcx.bean.ImageBucket;
import com.rdcx.randian.R;
import com.rdcx.utils.ImageDisplayer;

import java.util.List;

/**
 * Created by Administrator on 2016/3/3 0003.
 *
 * @author mengchuiliu
 */
public class AlbumAdapter extends BaseAdapter {
    private List<ImageBucket> mDataList;
    private Context mContext;

    public AlbumAdapter(Context context, List<ImageBucket> dataList) {
        this.mContext = context;
        this.mDataList = dataList;
    }


    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_album_list, null);
            mHolder.iconIv = (ImageView) convertView.findViewById(R.id.cover);
            mHolder.titleTv = (TextView) convertView.findViewById(R.id.album_title);
            mHolder.countTv = (TextView) convertView.findViewById(R.id.album_count);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        final ImageBucket item = mDataList.get(position);

        if (item.imageList != null && item.imageList.size() > 0) {
            String thumbPath = item.imageList.get(0).thumbnailPath;
            String sourcePath = item.imageList.get(0).sourcePath;
            ImageDisplayer.getInstance(mContext).displayBmp(mHolder.iconIv, thumbPath, sourcePath);
        } else {
            mHolder.iconIv.setImageBitmap(null);
        }
        mHolder.titleTv.setText(item.bucketName);
        mHolder.countTv.setText(item.count + "张");
        return convertView;
    }

    class ViewHolder {
        private ImageView iconIv;
        private TextView titleTv;
        private TextView countTv;
    }
}
