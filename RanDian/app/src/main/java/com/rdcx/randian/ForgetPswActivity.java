package com.rdcx.randian;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.PermissionTools;
import com.rdcx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ForgetPswActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText forget_phone, forget_sms;
    private Button forget_send_sms;
    private Toast toast;
    private DataInterface dataInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_psw);
        ((MyApplication) getApplication()).addActivity(this);
        dataInterface = new NetDataGetter(getApplicationContext());
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        initView();
    }

    private void initView() {
        forget_phone = (EditText) findViewById(R.id.forget_phone);
        forget_sms = (EditText) findViewById(R.id.forget_sms);

        forget_send_sms = (Button) findViewById(R.id.forget_send_sms);
        forget_send_sms.setOnClickListener(this);
        findViewById(R.id.forget_back).setOnClickListener(this);

        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);
        if (Build.VERSION.SDK_INT < 22) {
            Utils.controlKeyboardLayout(findViewById(R.id.root), next);
        }
    }

    long lastClick = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forget_back:
                ForgetPswActivity.this.finish();
                break;
            case R.id.forget_send_sms:
                final String phone = forget_phone.getText().toString().trim();
                if (phone.length() == 11) {
                    forget_send_sms.setClickable(false);
                    forget_send_sms.setBackgroundResource(R.drawable.btn_tou);
                    dataInterface.phoneExist(phone, new NetManager.DataArray() {
                        @Override
                        public void getServiceData(JSONArray jsonArray) {
                            if (jsonArray.length() > 0) {
                                try {
                                    JSONObject object = jsonArray.getJSONObject(0);
                                    String resp = object.getString("resp");
                                    if (resp.equals("000200")) {
                                        dataInterface.sendSMS(phone, new NetManager.DataArray() {
                                            @Override
                                            public void getServiceData(JSONArray jsonArray) {
                                                if (jsonArray.length() > 0) {
                                                    try {
                                                        JSONObject object = jsonArray.getJSONObject(0);
                                                        String resp = object.getString("resp");
                                                        if (resp.equals("000090")) {
                                                            // 短信发送成功
                                                            Utils.timing(handler, 0);
                                                        } else {
                                                            forget_send_sms.setClickable(true);
                                                            forget_send_sms.setBackgroundResource(R.drawable.login_btn);
                                                            Toast.makeText(ForgetPswActivity.this, "手机号码有误，请重新输入",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }, errorListener);
                                    } else {
                                        forget_send_sms.setClickable(true);
                                        forget_send_sms.setBackgroundResource(R.drawable.login_btn);
                                        toast.setText("手机号码未注册,请先注册!");
                                        toast.show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, errorListener);
                } else {
                    // 手机号码输入有误
                    toast.setText("手机号码有误");
                    toast.show();
                }
                break;
            case R.id.next:
                if (System.currentTimeMillis() - lastClick < 2000) {
                    return;
                }
                lastClick = System.currentTimeMillis();
                final String phoneNumber = forget_phone.getText().toString().trim();
                String sms = forget_sms.getText().toString().trim();
                if (phoneNumber.length() != 11) {
                    toast.setText("手机号码有误");
                    toast.show();
                } else if (sms.equals("")) {
                    toast.setText("短信验证码错误");
                    toast.show();
                } else {
                    dataInterface.forgetPsw(phoneNumber, "", sms, "1", new NetManager.DataArray() {
                        @Override
                        public void getServiceData(JSONArray jsonArray) {
                            if (jsonArray.length() > 0) {
                                try {
                                    JSONObject object = jsonArray.getJSONObject(0);
                                    String resp = object.getString("resp");
                                    String content = object.getString("msg");
                                    if (resp.equals("000000")) {
                                        Intent intent = new Intent(ForgetPswActivity.this,
                                                UpdatePswActivity.class);
                                        intent.putExtra("phoneNumber", phoneNumber);
                                        startActivity(intent);
                                        ForgetPswActivity.this.finish();
                                    } else {
                                        Toast.makeText(ForgetPswActivity.this, content,
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (msg.arg1 == 0) {
                        forget_send_sms.setClickable(true);
                        forget_send_sms.setText("再次发送");
                        forget_send_sms.setBackgroundResource(R.drawable.login_btn);
                    } else {
                        forget_send_sms.setText(msg.arg1 + "秒");
                    }
                    break;
                case 1:
                    if (msg.obj.toString() != null) {
                        forget_sms.setText(msg.obj.toString());
                    } else {
                        Toast.makeText(ForgetPswActivity.this, "自动填写验证码失败!",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionTools.checkPermission(ForgetPswActivity.this, android.Manifest.permission.READ_SMS, "", true)) {
            getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, c);
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PermissionTools.checkPermission(ForgetPswActivity.this, android.Manifest.permission.READ_SMS, "", true)) {
            this.getContentResolver().unregisterContentObserver(c);
        }
    }

    ContentObserver c = new ContentObserver(handler) {
        @SuppressWarnings("deprecation")
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (!PermissionTools.checkPermission(ForgetPswActivity.this, android.Manifest.permission.READ_SMS, "", true)) {
                return;
            }

            Uri uri = Uri.parse("content://sms/inbox");
            String[] projection = new String[]{"address", "person", "body"};
            String sortOrder = "_id desc";

            String phone = forget_phone.getText().toString().trim();
            if (phone.equals("")) {
                return;
            }
            Cursor cursor = null;
            if (Utils.getMobileType(phone) != null) {
                cursor = managedQuery(uri, projection, "address = ?",
                        new String[]{Utils.getMobileType(phone)}, sortOrder);
            }
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String body = cursor.getString(cursor.getColumnIndex("body")).replaceAll("\n", " ");
                Message message = new Message();
                message.what = 1;
                message.obj = Utils.getYZM(body, 6);
                handler.sendMessage(message);
            }
            if (Build.VERSION.SDK_INT < 14) {
                assert cursor != null;
                cursor.close();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
