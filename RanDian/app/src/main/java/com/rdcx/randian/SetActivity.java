package com.rdcx.randian;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.SP;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SetActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ((MyApplication) getApplication()).addActivity(this);
        initView();
    }

    private void initView() {
        findViewById(R.id.set_back).setOnClickListener(this);
        findViewById(R.id.set_portrait).setOnClickListener(this);
        findViewById(R.id.set_account_safe).setOnClickListener(this);
        findViewById(R.id.set_protect).setOnClickListener(this);
        findViewById(R.id.set_question).setOnClickListener(this);
        findViewById(R.id.feedback).setOnClickListener(this);
        findViewById(R.id.set_update).setOnClickListener(this);
        findViewById(R.id.about).setOnClickListener(this);
        findViewById(R.id.ll_logout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_back://返回
                SetActivity.this.finish();
                break;
            case R.id.set_portrait://头像
                startActivity(new Intent(SetActivity.this, UpdateUserInfo.class));
                break;
            case R.id.set_account_safe: // 账户安全
                startActivity(new Intent(SetActivity.this, AccountSafeActivity.class));
                break;
            case R.id.set_protect://进程保护
                Intent intent3 = new Intent(SetActivity.this, WebHtmlActivity.class);
                intent3.putExtra("program", true);
                startActivity(intent3);
                break;
            case R.id.set_question://常见问题
                Intent intent = new Intent(SetActivity.this, WebHtmlActivity.class);
                intent.putExtra("question", true);
                startActivity(intent);
                break;
            case R.id.feedback://意见反馈
                startActivity(new Intent(SetActivity.this, FeedbackActivity.class));
                break;
            case R.id.set_update://更新
                startActivity(new Intent(SetActivity.this, VersionUpdate.class));
                break;
            case R.id.about://关于
                startActivity(new Intent(SetActivity.this, AboutActivity.class));
                break;
            case R.id.ll_logout://注销
                AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
                builder.setTitle("提示").setMessage("\n\t您确定要注销此账户吗？\n");
                builder.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataInterface dataInterface = new NetDataGetter(getApplicationContext());
                                dataInterface.loginOut(SP.getString(SetActivity.this, SP.USER_ID, ""),
                                        new NetManager.DataArray() {
                                            @Override
                                            public void getServiceData(JSONArray jsonArray) {
                                                try {
                                                    JSONObject object = jsonArray.getJSONObject(0);
                                                    String resp = object.getString("resp");
                                                    if (resp.equals("000000")) {
                                                        //登出
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {

                                            }
                                        });
                                ((MyApplication) getApplication()).actFinisih();
                                SP.set(SetActivity.this, "isLogin", false);
                                startActivity(new Intent(SetActivity.this, LoginActivity.class));
                                SetActivity.this.finish();
                            }
                        }).setNegativeButton("取消", null);
                builder.create().show();
                break;
        }
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
