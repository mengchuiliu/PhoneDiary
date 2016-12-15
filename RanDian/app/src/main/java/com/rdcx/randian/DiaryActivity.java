package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rdcx.tools.DB;
import com.rdcx.tools.Diary;
import com.rdcx.utils.DateUtil;
import com.rdcx.utils.ImageDisplayer;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DiaryActivity extends AppCompatActivity implements OnClickListener {
    TextView diary_date, empty;
    ListView listView;
    List<Diary> diaryList = new ArrayList<>();
    DiaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        ((MyApplication) getApplication()).addActivity(this);
        diaryList = DB.selectDiary(DiaryActivity.this, 0, 0);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (diaryList.size() == 0) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    private void init() {
        diary_date = (TextView) findViewById(R.id.diary_date);
        listView = (ListView) findViewById(R.id.diary_list);
        empty = (TextView) findViewById(R.id.empty);
        findViewById(R.id.diary_add).setOnClickListener(this);
        adapter = new DiaryAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DiaryActivity.this, DiaryShow.class);
                intent.putExtra("diary", diaryList.get(position));
                intent.putExtra("isEditor", true);
                startActivityForResult(intent, 99);
            }
        });
        diary_date.setText(DateUtil.getCurDate());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.diary_add://添加日记
                Intent intent = new Intent(DiaryActivity.this, DiaryEditorActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (diaryList != null) {
                diaryList.clear();
            }
            diaryList = DB.selectDiary(DiaryActivity.this, 0, 0);
            adapter.notifyDataSetChanged();
        }
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
                convertView = LayoutInflater.from(DiaryActivity.this).inflate(R.layout.diary_item, null);
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
                myHolder.dimension.setVisibility(View.VISIBLE);
                myHolder.dimension.setText(diaryList.get(position).datatext);
            }

            List<String> stringList = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(diaryList.get(position).path);
                for (int i = 0; i < jsonArray.length(); i++) {
                    stringList.add(jsonArray.getString(i));
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
            convertView = View.inflate(DiaryActivity.this, R.layout.item_diary_image, null);
            ImageView imageIv = (ImageView) convertView.findViewById(R.id.item_grid_image);
            String path = strings.get(position);
            ImageDisplayer.getInstance(DiaryActivity.this).displayBmp(imageIv, null, path);
            return convertView;
        }
    }
}
