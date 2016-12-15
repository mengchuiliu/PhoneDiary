package com.rdcx.randian;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.rdcx.tools.DB;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiaryEditorActivity extends AppCompatActivity implements View.OnClickListener {
    GridView gridView;
    String[] datas = new String[]{"通话量", "通话数", "照片", "社交", "购物", "足迹", "应用", "时光轴"};
    List<Holder> list = new ArrayList<>();
    HashMap<Integer, String> hash;
    boolean hashAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_editor);
        ((MyApplication) getApplication()).addActivity(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                hash = new HashMap<>();
                for (int i = 0; i < datas.length; i++) {
                    hash.put(i, null);
                }
                DB.findDimension(DiaryEditorActivity.this, hash, DB.TYPE_TODAY);
                hashAlready = true;
            }
        }).start();

        for (int i = 0; i < datas.length; i++) {
            Holder holder = new Holder();
            holder.isC = true;
            holder.loc = i;
            list.add(holder);
        }
        init();
    }

    private void init() {
        final EdAdapter adapter = new EdAdapter(this);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setPosition(position);
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.ed_back).setOnClickListener(this);
        findViewById(R.id.ed_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ed_back:
                this.finish();
                break;
            case R.id.ed_bt://下一步
                if (!hashAlready) {
                    Toast.makeText(DiaryEditorActivity.this, "数据正在加载中,请稍后重试！", Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<Integer, String> hashMap = new HashMap<>();//保存用户需要的维度
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isC) {
                        hashMap.put(i, hash.get(i));
                    }
                }
                Intent intent = new Intent(DiaryEditorActivity.this, DiaryShow.class);
                intent.putExtra("map", hashMap);
                intent.putExtra("clear", true);
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            setResult(-1);
            this.finish();
        }
    }

    class EdAdapter extends BaseAdapter {
        private Context context;
        private int selectPosition;

        public EdAdapter(Context context) {
            this.context = context;
        }

        public void setPosition(int selectPosition) {
            this.selectPosition = selectPosition;
        }

        @Override
        public int getCount() {
            return datas.length;
        }

        @Override
        public Object getItem(int position) {
            return datas[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_ed_diary, null);
                holder.tv_name = (TextView) convertView.findViewById(R.id.ed_name);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.choose_name);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.tv_name.setText(datas[position]);
            if (selectPosition == position) {
                if (holder.checkBox.isChecked()) {
                    holder.checkBox.setChecked(false);
                    list.get(position).isC = false;
                } else {
                    holder.checkBox.setChecked(true);
                    list.get(position).isC = true;
                }
            }
            return convertView;
        }
    }

    class Holder {
        TextView tv_name;
        CheckBox checkBox;
        int loc;
        boolean isC;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
