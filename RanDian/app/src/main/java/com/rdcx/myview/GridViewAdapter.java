package com.rdcx.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rdcx.bean.WeekDate;
import com.rdcx.randian.R;

import java.util.ArrayList;


/**
 * Created by Administrator on 2015/11/30 0030.
 *
 * @author mengchuiliu
 */
public class GridViewAdapter extends BaseAdapter {
    private ArrayList<WeekDate> data;
    private Context context;
    private int selectPosition = -1;
    private int type = -1;//区分月和周
    private int currentPosition = -1;

    public GridViewAdapter(Context context, int type) {
        this.context = context;
        this.type = type;
    }

    public void setData(ArrayList<WeekDate> data) {
        this.data = data;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
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
            if (type == 1) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_month, null);
                holder.textView = (TextView) convertView.findViewById(R.id.item_tv);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_week, null);
                holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
                holder.tv_num = (TextView) convertView.findViewById(R.id.tv_week);
            }
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        if (type == 1) {
            holder.textView.setText(data.get(position).getMonth());
        } else {
            WeekDate weekDate = data.get(position);
            holder.tv_date.setText(weekDate.getWeekFirstDay() + "-" + weekDate.getWeekEndDay());
            holder.tv_num.setText(weekDate.getWeekNumber() + "周");
        }

        if (position == selectPosition) {
            if (type == 1) {
                holder.textView.setSelected(true);
                // 0x8800BFFF蓝色
                holder.textView.setTextColor(Color.WHITE);
                holder.textView.setBackgroundResource(R.drawable.month_circle);
            } else {
                holder.tv_num.setSelected(true);
                holder.tv_num.setTextColor(Color.WHITE);
                holder.tv_date.setTextColor(Color.WHITE);
                holder.tv_num.setBackgroundResource(R.drawable.month_circle);
                holder.tv_date.setBackgroundColor(Color.TRANSPARENT);
            }
        } else if (position == currentPosition) {
            if (type == 1) {
                holder.textView.setTextColor(context.getResources().getColor(R.color.gray_light));
                holder.textView.setBackgroundResource(R.drawable.choose_circle);
            } else {
                holder.tv_num.setTextColor(context.getResources().getColor(R.color.gray_light));
                holder.tv_date.setTextColor(context.getResources().getColor(R.color.gray_light));
                holder.tv_num.setBackgroundResource(R.drawable.choose_circle);
                holder.tv_date.setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            if (type == 1) {
                holder.textView.setSelected(false);
                // 0x8800BFFF蓝色
                holder.textView.setTextColor(context.getResources().getColor(R.color.gray_light));
                holder.textView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                holder.tv_num.setSelected(false);
                holder.tv_num.setTextColor(context.getResources().getColor(R.color.gray_light));
                holder.tv_date.setTextColor(context.getResources().getColor(R.color.gray_light));
                holder.tv_num.setBackgroundColor(Color.TRANSPARENT);
                holder.tv_date.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        return convertView;
    }

    class Holder {
        TextView textView, tv_date, tv_num;
    }
}
