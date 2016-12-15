package com.rdcx.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rdcx.bean.DiaryImageInfo;
import com.rdcx.bean.TaskRankFriend;
import com.rdcx.randian.R;
import com.rdcx.utils.Constants;
import com.rdcx.utils.ImageDisplayer;
import com.rdcx.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/3/2 0002.
 *
 * @author mengchuiliu
 */
public class TaskRankAdapter extends BaseAdapter {
    private List<TaskRankFriend> mDataList = new ArrayList<>();
    private Context mContext;

    public TaskRankAdapter(Context context) {
        this.mContext = context;

    }

    public void setData(List<TaskRankFriend> dataList) {
        this.mDataList = dataList;
    }

    @Override
    public int getCount() {
        if (mDataList.size() >= Constants.MAX_IMAGE_SIZE) {
            return Constants.MAX_IMAGE_SIZE;
        } else {
            return mDataList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mDataList == null ? null : mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 所有Item展示不满一页，就不进行ViewHolder重用了，避免了一个拍照以后添加图片按钮被覆盖的奇怪问题
        convertView = View.inflate(mContext, R.layout.item_task_icon, null);
        ImageView imageIv = (ImageView) convertView.findViewById(R.id.item_grid_image);
        ImageView imageShow = (ImageView) convertView.findViewById(R.id.rank_show);
        TextView task_nick = (TextView) convertView.findViewById(R.id.task_nick);
        TextView task_count = (TextView) convertView.findViewById(R.id.task_count);
        TaskRankFriend item = mDataList.get(position);
        task_nick.setText(item.nickName);
        task_count.setText(item.taskCount);
        if (item.taskIcon != null) {
            imageIv.setImageBitmap(Utils.toRoundBitmap(item.taskIcon));
        } else {
            int i = new Random().nextInt(4) + 1;
            switch (i) {
                case 1:
                    imageIv.setImageResource(R.mipmap.random_1);
                    break;
                case 2:
                    imageIv.setImageResource(R.mipmap.random_2);
                    break;
                case 3:
                    imageIv.setImageResource(R.mipmap.random_3);
                    break;
                case 4:
                    imageIv.setImageResource(R.mipmap.random_4);
                    break;
                default:
                    imageIv.setImageResource(R.mipmap.randian);
                    break;
            }
        }
        if (position == 0) {
            imageShow.setVisibility(View.VISIBLE);
            imageShow.setImageResource(R.mipmap.task_one);
        } else if (position == 1) {
            imageShow.setVisibility(View.VISIBLE);
            imageShow.setImageResource(R.mipmap.task_two);
        } else if (position == 2) {
            imageShow.setVisibility(View.VISIBLE);
            imageShow.setImageResource(R.mipmap.task_three);
        } else {
            imageShow.setVisibility(View.GONE);
        }
        return convertView;
    }
}
