package com.rdcx.randian;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdatePswActivity extends AppCompatActivity implements View.OnClickListener {
    EditText new_psw, new_psw_again;
    ImageView update_psw_show;
    boolean flag = false;//密码显示
    private DataInterface dataInterface;//数据接口
    private Toast toast;

    String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_psw);
        ((MyApplication) getApplication()).addActivity(this);

        dataInterface = new NetDataGetter(getApplicationContext());
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        initView();
    }

    private void initView() {
        new_psw = (EditText) findViewById(R.id.new_psw);
        new_psw_again = (EditText) findViewById(R.id.new_psw_again);
        update_psw_show = (ImageView) findViewById(R.id.update_psw_show);

        findViewById(R.id.update_back).setOnClickListener(this);
        findViewById(R.id.up_frame).setOnClickListener(this);

        Button save = (Button) findViewById(R.id.save_psw);
        save.setOnClickListener(this);
        if (Build.VERSION.SDK_INT < 22) {
            Utils.controlKeyboardLayout(findViewById(R.id.root), save);
        }
    }

    long lastClick = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_back:
                UpdatePswActivity.this.finish();
                break;
            case R.id.up_frame:
                if (!flag) {
                    // 显示密码
                    update_psw_show.setImageResource(R.mipmap.psw_show);
                    new_psw_again.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    //隐藏密码
                    update_psw_show.setImageResource(R.mipmap.psw_gone);
                    new_psw_again.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                // 使光标始终在最后位置
                Editable etable = new_psw_again.getText();
                Selection.setSelection(etable, etable.length());
                flag = !flag;
                break;
            case R.id.save_psw:
                if (System.currentTimeMillis() - lastClick < 2000) {
                    return;
                }
                lastClick = System.currentTimeMillis();

                String psw = new_psw.getText().toString().trim();
                String pswAgain = new_psw_again.getText().toString().trim();
                if (psw.length() < 6) {
                    toast.setText("请输入六位及以上密码");
                    toast.show();
                } else if (Utils.StringFilter(psw).length() != psw.length()) {
                    toast.setText("密码只能输入字母、数字和下划线");
                    toast.show();
                } else if (!psw.equals(pswAgain)) {
                    toast.setText("两次输入的密码不一致");
                    toast.show();
                } else {
                    dataInterface.forgetPsw(phoneNumber, Utils.getMD5Str(psw), "", "",
                            new NetManager.DataArray() {
                                @Override
                                public void getServiceData(JSONArray jsonArray) {
                                    if (jsonArray.length() > 0) {
                                        try {
                                            JSONObject object = jsonArray.getJSONObject(0);
                                            String resp = object.getString("resp");
                                            String content = object.getString("msg");
                                            if (resp.equals("000000")) {
                                                // 忘记密码中保存密码成功
                                                Toast.makeText(UpdatePswActivity.this, "密码修改成功",
                                                        Toast.LENGTH_SHORT).show();
                                                UpdatePswActivity.this.finish();
                                            } else {
                                                Toast.makeText(UpdatePswActivity.this, content,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }, errorListener);
                }
                break;
        }
    }

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.e("my_log", "服务器连接失败==>" + volleyError.getMessage());
            if (Utils.isNetworkAvailable(getApplicationContext())) {
                toast.setText("网络异常!");
                toast.show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
            }
        }
    };

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
