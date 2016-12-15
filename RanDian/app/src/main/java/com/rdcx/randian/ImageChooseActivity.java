package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.rdcx.bean.DiaryImageInfo;
import com.rdcx.myview.ImageChooseAdapter;
import com.rdcx.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 图片选择
 */
public class ImageChooseActivity extends AppCompatActivity implements View.OnClickListener {
    List<DiaryImageInfo> mDataList = new ArrayList<>();
    String mBucketName;
    int availableSize;
    TextView mBucketNameTv, cancelTv, iamge_complete;
    private ImageChooseAdapter mAdapter;
    private HashMap<String, DiaryImageInfo> selectedImgs = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choose);
        ((MyApplication) getApplication()).addActivity(this);
        mDataList = (List<DiaryImageInfo>) getIntent().getSerializableExtra("iamge_list");
        if (mDataList == null) {
            mDataList = new ArrayList<>();
        }
        mBucketName = getIntent().getStringExtra("bucketName");
        if (TextUtils.isEmpty(mBucketName)) {
            mBucketName = "请选择";
        }
        availableSize = getIntent().getIntExtra("AvailableSize", Constants.MAX_IMAGE_SIZE);
        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        mBucketNameTv = (TextView) findViewById(R.id.bucket_name);
        mBucketNameTv.setText(mBucketName);
        iamge_complete = (TextView) findViewById(R.id.iamge_complete);
        iamge_complete.setText("完成" + "(" + selectedImgs.size() + "/" + availableSize + ")");
        cancelTv = (TextView) findViewById(R.id.image_back);

        GridView mGridView = (GridView) findViewById(R.id.iamge_gv);
        mAdapter = new ImageChooseAdapter(ImageChooseActivity.this, mDataList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiaryImageInfo item = mDataList.get(position);
                if (item.isSelected) {
                    item.isSelected = false;
                    selectedImgs.remove(item.imageId);
                } else {
                    if (selectedImgs.size() >= availableSize) {
                        Toast.makeText(ImageChooseActivity.this, "最多选择" + availableSize + "张图片",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    item.isSelected = true;
                    selectedImgs.put(item.imageId, item);
                }
                iamge_complete.setText("完成" + "(" + selectedImgs.size() + "/" + availableSize + ")");
                mAdapter.notifyDataSetChanged();
            }
        });

        cancelTv.setOnClickListener(this);
        iamge_complete.setOnClickListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back://取消
                finish();
                break;
            case R.id.iamge_complete://完成
                Intent intent = new Intent();
                intent.putExtra("noEditor", true);
                intent.putExtra("image_list", new ArrayList<>(selectedImgs.values()));
                setResult(8, intent);
                finish();
                break;
        }
    }
}
