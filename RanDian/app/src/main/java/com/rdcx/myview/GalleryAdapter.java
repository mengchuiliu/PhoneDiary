package com.rdcx.myview;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter {

    private Context mContext;
    private int width;
    private int[] mImageIds;

    public GalleryAdapter(Context context, int[] ids) {
        mContext = context;
        mImageIds = ids;

        DisplayMetrics dm = mContext.getApplicationContext().getResources()
                .getDisplayMetrics();
        width = dm.widthPixels / 6;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mImageIds[position % mImageIds.length]);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new Gallery.LayoutParams(width, width * 6 / 10));
        return imageView;
    }
}
