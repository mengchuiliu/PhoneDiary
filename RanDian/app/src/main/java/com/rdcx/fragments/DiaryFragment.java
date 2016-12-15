package com.rdcx.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rdcx.randian.DiaryEditorActivity;
import com.rdcx.randian.DiaryShow;
import com.rdcx.randian.R;
import com.rdcx.service.DiaryIconUpload;
import com.rdcx.tools.DB;
import com.rdcx.tools.Diary;
import com.rdcx.tools.SP;
import com.rdcx.tools.Upload;
import com.rdcx.utils.DateUtil;
import com.rdcx.utils.ImageDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/14 0014.
 * 日记本
 *
 * @author mengchuiliu
 */
@SuppressLint("HandlerLeak")
public class DiaryFragment extends Fragment {
    View view;
    TextView diary_date, empty;
    ListView listView;
    List<Diary> diaryList = new ArrayList<>();
    DiaryAdapter adapter;
    LinearLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_diary, null);
        diaryList = DB.selectDiary(getActivity(), 0, 0);
        Upload.uploadDiary(getContext(), diaryList);
        empty = (TextView) view.findViewById(R.id.empty);
        layout = (LinearLayout) view.findViewById(R.id.ll_diary);
        init(view);
        return view;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    diaryList = DB.selectDiary(getActivity(), 0, 0);
                    for (Diary diary : diaryList) {
                        if (!TextUtils.isEmpty(diary.path)) {
                            try {
                                JSONArray jsonArray = new JSONArray(diary.path);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String str = jsonObject.getString("locaPath");
                                    if (TextUtils.isEmpty(str) || !fileIsExists(str)) {
                                        layout.setVisibility(View.VISIBLE);
                                        new DiaryIconUpload(diary, getActivity(), handler, i).execute();
                                    }
                                }
                            } catch (JSONException e) {
                                handler.sendEmptyMessage(2);
                                e.printStackTrace();
                            }
                        }
                    }
                    handler.sendEmptyMessage(2);
                    break;
                case 2:
                    layout.setVisibility(View.GONE);
                    if (diaryList.size() > 0) {
                        empty.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case 3:
                    try {
                        diaryList = DB.selectDiary(getActivity(), 0, 0);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (diaryList.size() == 0) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
        //日记数据下载中
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = SP.getBoolean(getActivity(), "DownloadDiary", true);
                    if (flag) {
                        handler.postDelayed(this, 500);
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                } catch (Exception e) {
                    Log.e("test", "DiaryFragment onResume postDelayed cause an Exception=>:", e);
                }
            }
        }, 500);
    }

    private void init(View view) {
        diary_date = (TextView) view.findViewById(R.id.diary_date);
        listView = (ListView) view.findViewById(R.id.diary_list);
        view.findViewById(R.id.diary_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DiaryEditorActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        adapter = new DiaryAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DiaryShow.class);
                intent.putExtra("diary", diaryList.get(position));
                intent.putExtra("isEditor", true);
                startActivityForResult(intent, 99);
            }
        });
        diary_date.setText(DateUtil.getCurDate());
    }

    class DiaryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return diaryList == null ? 0 : diaryList.size();
        }

        @Override
        public Object getItem(int position) {
            return diaryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder myHolder;
            if (convertView == null) {
                myHolder = new MyHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.diary_item, null);
                myHolder.day = (TextView) convertView.findViewById(R.id.day);
                myHolder.date = (TextView) convertView.findViewById(R.id.date);
                myHolder.week = (TextView) convertView.findViewById(R.id.week);
                myHolder.dimension = (TextView) convertView.findViewById(R.id.dimension);
                myHolder.diary_text = (TextView) convertView.findViewById(R.id.diary_text);
                myHolder.diary_time = (TextView) convertView.findViewById(R.id.diary_time);
                myHolder.gridView = (GridView) convertView.findViewById(R.id.diary_gv);
                myHolder.diary_ll = (LinearLayout) convertView.findViewById(R.id.diary_ll);
                convertView.setTag(myHolder);
            } else {
                myHolder = (MyHolder) convertView.getTag();
            }
            long time = diaryList.get(position).time;
            if (TextUtils.isEmpty(diaryList.get(position).text)) {
                myHolder.diary_text.setVisibility(View.GONE);
            } else {
                myHolder.diary_text.setVisibility(View.VISIBLE);
                myHolder.diary_text.setText(diaryList.get(position).text);
            }
            myHolder.day.setText(DateUtil.getCurTime(time));
            myHolder.day.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            myHolder.week.setText(DateUtil.getWeekOfDate(time));
            myHolder.date.setText(DateUtil.getDiaryDate(time));
            myHolder.diary_time.setText(DateUtil.getHourAndMin(time));

            if (TextUtils.isEmpty(diaryList.get(position).datatext)) {
                myHolder.dimension.setVisibility(View.GONE);
            } else {
//                if (SP.getBoolean(getActivity(), "YesterDayState", false) && position == 0) {
//                    myHolder.dimension.setSingleLine(false);
//                } else {
//                    myHolder.dimension.setSingleLine(true);
//                }
                if (diaryList.get(position).datatext.contains("昨日账单")) {
                    myHolder.dimension.setSingleLine(false);
                } else {
                    myHolder.dimension.setSingleLine(true);
                }
                myHolder.dimension.setVisibility(View.VISIBLE);
                myHolder.dimension.setText(diaryList.get(position).datatext);
            }

            List<String> stringList = new ArrayList<>();
            try {
                if (!TextUtils.isEmpty(diaryList.get(position).path)) {
                    JSONArray jsonArray = new JSONArray(diaryList.get(position).path);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (jsonArray.optJSONObject(0) == null) {
                            stringList.add(jsonArray.getString(i));
                        } else {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            stringList.add(jsonObject.getString("locaPath"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (stringList.size() == 0) {
                myHolder.diary_ll.setVisibility(View.GONE);
            } else {
                myHolder.diary_ll.setVisibility(View.VISIBLE);
                myHolder.gridView.setAdapter(new GridAdapter(stringList));
                myHolder.gridView.setClickable(false);
                myHolder.gridView.setPressed(false);
                myHolder.gridView.setEnabled(false);
            }
            return convertView;
        }

        class MyHolder {
            TextView day, date, week, dimension, diary_text, diary_time;
            GridView gridView;
            LinearLayout diary_ll;
        }
    }

    class GridAdapter extends BaseAdapter {
        private List<String> strings = new ArrayList<>();

        public GridAdapter(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public int getCount() {
            if (strings.size() > 3) {
                return 3;
            }
            return strings.size();
        }

        @Override
        public Object getItem(int position) {
            return strings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 所有Item展示不满一页，就不进行ViewHolder重用了，避免了一个拍照以后添加图片按钮被覆盖的奇怪问题
            convertView = View.inflate(getActivity(), R.layout.item_diary_image, null);
            ImageView imageIv = (ImageView) convertView.findViewById(R.id.item_grid_image);
            String path = strings.get(position);
            ImageDisplayer.getInstance(getActivity()).displayBmp(imageIv, null, path);
            return convertView;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (diaryList != null) {
                diaryList.clear();
            }
            diaryList = DB.selectDiary(getActivity(), 0, 0);
            adapter.notifyDataSetChanged();
        }
    }

    public boolean onKeyDown() {
        if (layout.getVisibility() == View.VISIBLE) {
            layout.setVisibility(View.GONE);
            return true;
        } else {
            return false;
        }
    }

    public void updataAdapter() {
        handler.sendEmptyMessage(3);
    }

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
