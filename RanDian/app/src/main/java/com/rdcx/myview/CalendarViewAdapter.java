package com.rdcx.myview;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class CalendarViewAdapter extends PagerAdapter {
    private View[] views;

    public CalendarViewAdapter(View[] views) {
        super();
        this.views = views;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (container.getChildCount() == views.length) {
            container.removeView(views[position % views.length]);
        }
        container.addView(views[position % views.length], 0);
        return views[position % views.length];
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(container);
    }

    public View[] getAllItems() {
        return views;
    }

}  
