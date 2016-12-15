package com.rdcx.randian;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;
import com.rdcx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackActivity extends AppCompatActivity implements OnClickListener {
    EditText advice, contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ((MyApplication) getApplication()).addActivity(this);
        initView();
    }

    private void initView() {
        advice = (EditText) findViewById(R.id.edit_advice);
        contact = (EditText) findViewById(R.id.edit_contact);
        findViewById(R.id.btn_submit).setOnClickListener(this);
        findViewById(R.id.feed_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feed_back:
                FeedbackActivity.this.finish();
                break;
            case R.id.btn_submit:
                String content = advice.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(getApplicationContext(), "请先填写您建议！", Toast.LENGTH_SHORT).show();
                } else {
                    DataInterface dataInterface = new NetDataGetter(getApplicationContext());
                    dataInterface.feedBack(SP.getString(FeedbackActivity.this, SP.USER_ID, "-1"), "", content,
                            new NetManager.DataArray() {
                                @Override
                                public void getServiceData(JSONArray jsonArray) {
                                    try {
                                        JSONObject object = jsonArray.getJSONObject(0);
                                        String resp = object.getString("resp");
                                        if (resp.equals("000000")) {

                                            TaskTools.addStageTask(FeedbackActivity.this, 1011); // 完成阶段任务 1011：吐槽“手机日记”

                                            Toast.makeText(FeedbackActivity.this, "提交成功",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "意见提交失败,请稍后重试!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (Utils.isNetworkAvailable(getApplicationContext())) {
                                        Toast.makeText(getApplicationContext(),
                                                "网络异常!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
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
