package com.rdcx.randian;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rdcx.tools.TaskTools;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class VersionUpdate extends AppCompatActivity implements OnClickListener {
    TextView tv_new, tv_current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_update);
        ((MyApplication) getApplication()).addActivity(this);
        initView();
    }

    private void initView() {
        tv_new = (TextView) findViewById(R.id.tv_new);
        tv_current = (TextView) findViewById(R.id.tv_current);
        String versionName = "";
        try {
            // 当前版本的版本号
            PackageInfo info = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), 0);
            versionName = info.versionName;
            tv_current.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        findViewById(R.id.version_back).setOnClickListener(this);
        findViewById(R.id.btn_check_update).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.version_back:
                VersionUpdate.this.finish();
                break;
            case R.id.btn_check_update:
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateOnlyWifi(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        switch (updateStatus) {
                            case UpdateStatus.Yes: // has update
                                UmengUpdateAgent.showUpdateDialog(VersionUpdate.this, updateInfo);
                                
                                TaskTools.addStageTask(VersionUpdate.this, 1010); // 完成阶段任务 1010：升级手机日记 1 次

                                break;
                            case UpdateStatus.No: // has no update
                                tv_new.setVisibility(View.VISIBLE);
                                break;
                            case UpdateStatus.NoneWifi: // none wifi
                                Toast.makeText(VersionUpdate.this, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
                                break;
                            case UpdateStatus.Timeout: // time out
                                Toast.makeText(VersionUpdate.this, "超时", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                UmengUpdateAgent.update(this);
//                UmengUpdateAgent.forceUpdate(VersionUpdate.this);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        tv_new.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
