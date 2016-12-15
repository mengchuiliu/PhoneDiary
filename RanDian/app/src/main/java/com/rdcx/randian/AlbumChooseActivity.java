package com.rdcx.randian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rdcx.bean.ImageBucket;
import com.rdcx.myview.AlbumAdapter;
import com.rdcx.utils.Constants;
import com.rdcx.utils.ImageFetcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlbumChooseActivity extends AppCompatActivity {
    private ImageFetcher mHelper;
    private List<ImageBucket> mDataList = new ArrayList<>();
    ListView mListView;
    AlbumAdapter mAdapter;
    private int availableSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_choose);
        ((MyApplication) getApplication()).addActivity(this);
        mHelper = ImageFetcher.getInstance(getApplicationContext());
        initData();
        initView();
    }

    private void initData() {
        mDataList = mHelper.getImagesBucketList(false);
        availableSize = getIntent().getIntExtra("AvailableSize", Constants.MAX_IMAGE_SIZE);
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.albums_list);
        mAdapter = new AlbumAdapter(this, mDataList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AlbumChooseActivity.this, ImageChooseActivity.class);
                intent.putExtra("iamge_list", (Serializable) mDataList.get(position).imageList);
                intent.putExtra("bucketName", mDataList.get(position).bucketName);
                intent.putExtra("AvailableSize", availableSize);
                startActivityForResult(intent, 1);
            }
        });

        findViewById(R.id.album_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumChooseActivity.this.finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 8) {
            if (data != null) {
                setResult(6, data);
                this.finish();
            }
        }
    }
}
