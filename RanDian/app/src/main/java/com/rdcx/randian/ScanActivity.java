package com.rdcx.randian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.rdcx.animation.SmoothAnimation;
import com.rdcx.myview.ScanProgressBar;
import com.rdcx.tools.SyncThread;
import com.umeng.analytics.MobclickAgent;

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ((MyApplication) getApplication()).addActivity(this);

//        ScanProgressBar scanProgressBar = (ScanProgressBar) findViewById(R.id.scanProgressBar);
//        SmoothAnimation animation = new SmoothAnimation(scanProgressBar, 0, 100);
//        animation.setDuration(3000);
//        animation.setInterpolator(new LinearInterpolator());
//        scanProgressBar.startAnimation(animation);
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                // TODO
//                // 动画执行完成
//                Intent intent = new Intent(ScanActivity.this, WebHtmlActivity.class);
//                intent.putExtra("url", "share1.html");
//                startActivity(intent);
//                finish();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        SyncThread.start(this, SyncThread.TYPE_ONE_HOUR);
        
        Intent intent = new Intent(ScanActivity.this, WebHtmlActivity.class);
        intent.putExtra("url", "share1.html");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
