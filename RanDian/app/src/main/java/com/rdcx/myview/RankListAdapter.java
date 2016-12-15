package com.rdcx.myview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rdcx.bean.AppRankInfo;
import com.rdcx.randian.R;
import com.rdcx.tools.DB;
import com.rdcx.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/3/21 0021.
 *
 * @author mengchuiliu
 */
public class RankListAdapter extends BaseAdapter {
    private Context context;
    private List<AppRankInfo> list;
    private int type = 1;

    public RankListAdapter(Context context) {
        this.context = context;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setListData(ArrayList<AppRankInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RankHolder holder;
        if (convertView == null) {
            holder = new RankHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_rank_list, null);
            holder.rank_icon = (ImageView) convertView.findViewById(R.id.rank_icon);
            holder.number = (TextView) convertView.findViewById(R.id.number);
            holder.appName = (TextView) convertView.findViewById(R.id.rank_name);
            holder.appContent = (TextView) convertView.findViewById(R.id.rank_content);
            holder.flRank = (FrameLayout) convertView.findViewById(R.id.fl_rank);
            convertView.setTag(holder);
        } else {
            holder = (RankHolder) convertView.getTag();
        }
        AppRankInfo appRankInfo = list.get(position);
        if (type == 1) {
            switch (position) {
                case 0:
                    holder.number.setBackgroundResource(R.mipmap.first_num);
                    holder.flRank.setForeground(context.getResources().getDrawable(R.mipmap.first_icon));
                    break;
                case 1:
                    holder.number.setBackgroundResource(R.mipmap.second_num);
                    holder.flRank.setForeground(context.getResources().getDrawable(R.mipmap.second_icon));
                    break;
                case 2:
                    holder.number.setBackgroundResource(R.mipmap.three_num);
                    holder.flRank.setForeground(context.getResources().getDrawable(R.mipmap.three_icon));
                    break;
                default:
                    holder.number.setBackgroundResource(R.color.transparent);
                    holder.flRank.setForeground(null);
                    break;
            }
            if (appRankInfo.number > 3) {
                holder.number.setText("\t" + appRankInfo.number + ". ");
            } else if (appRankInfo.number >= 10) {
                holder.number.setText(appRankInfo.number + ". ");
            } else {
                holder.number.setText("\t" + appRankInfo.number + "  ");
            }
            if (appRankInfo.appIcon != null) {
                holder.rank_icon.setImageBitmap(Utils.toRoundBitmap(appRankInfo.appIcon));
            } else {
                int i = new Random().nextInt(4) + 1;
                switch (i) {
                    case 1:
                        holder.rank_icon.setImageResource(R.mipmap.random_1);
                        break;
                    case 2:
                        holder.rank_icon.setImageResource(R.mipmap.random_2);
                        break;
                    case 3:
                        holder.rank_icon.setImageResource(R.mipmap.random_3);
                        break;
                    case 4:
                        holder.rank_icon.setImageResource(R.mipmap.random_4);
                        break;
                    default:
                        holder.rank_icon.setImageResource(R.mipmap.randian);
                        break;
                }
            }
            holder.appName.setText(appRankInfo.appName);
            holder.appContent.setText(DB.getTimeByRank(appRankInfo.total));
        } else {
            if (appRankInfo.appIcon != null) {
                holder.rank_icon.setImageBitmap(appRankInfo.appIcon);
            }
            holder.number.setBackgroundResource(R.color.transparent);
            holder.flRank.setForeground(null);
            holder.number.setText(String.format("%3d.", appRankInfo.number));
            holder.number.setText(appRankInfo.number + ".");
            holder.appName.setText(appRankInfo.appName.replaceAll("(^\\s*)|(\\s*$)|(\\(.*\\))", ""));
            holder.appContent.setText(appRankInfo.appCount + "人使用" + "\n" + "人均" + (DB.getTimeByRank(appRankInfo.total / appRankInfo.appCount) + "/天"));
        }
        return convertView;
    }

    class RankHolder {
        TextView number, appName, appContent;
        ImageView rank_icon;
        FrameLayout flRank;
    }

}
