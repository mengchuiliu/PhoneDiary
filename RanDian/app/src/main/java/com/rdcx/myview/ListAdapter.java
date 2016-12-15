package com.rdcx.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rdcx.bean.AppDate;
import com.rdcx.randian.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Administrator on 2015/12/4 0004.
 * 主页排行榜显示
 *
 * @author mengchuiliu
 */
public class ListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<AppDate> list;

    public ListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setDate(ArrayList<AppDate> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.list_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.image);
            holder.bar = (AppProgressBar) convertView.findViewById(R.id.progressBar);
            holder.textView = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        AppDate appDate = list.get(position);
        holder.icon.setImageDrawable(appDate.getIcon());
        holder.bar.setProgressSmooth(appDate.getPercent());
        holder.textView.setText(String.format("%4d分%2d秒\n打开%d次", (appDate.getTime() / 60), (appDate.getTime() % 60), appDate.getCount()));
        return convertView;
    }

    class Holder {
        ImageView icon;
        AppProgressBar bar;
        TextView textView;
    }
}
