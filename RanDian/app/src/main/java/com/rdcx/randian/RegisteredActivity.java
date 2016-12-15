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
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.SP;
import com.rdcx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisteredActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText reg_phone, reg_password, sms_yanzheng;
    private ImageView reg_show;
    private Button send_sms;
    boolean flag = false;
    private Toast toast;
    private DataInterface dataInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);
        ((MyApplication) getApplication()).addActivity(this);

        dataInterface = new NetDataGetter(getApplicationContext());
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        initView();
    }

    private void initView() {
        reg_phone = (EditText) findViewById(R.id.reg_phone);
        reg_password = (EditText) findViewById(R.id.reg_password);
        sms_yanzheng = (EditText) findViewById(R.id.sms_yanzheng);
        reg_show = (ImageView) findViewById(R.id.reg_show);
        send_sms = (Button) findViewById(R.id.send_sms);
        TextView regset_text = (TextView) findViewById(R.id.regset_text);
        regset_text.setMovementMethod(LinkMovementMethod.getInstance());
        send_sms.setOnClickListener(this);
        findViewById(R.id.registered_back).setOnClickListener(this);
        findViewById(R.id.reg_frame).setOnClickListener(this);

        Button register_now = (Button) findViewById(R.id.register_now);
        register_now.setOnClickListener(this);
        if (Build.VERSION.SDK_INT < 22) {
            Utils.controlKeyboardLayout(findViewById(R.id.root), register_now);
        }
    }

    long lastClick = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registered_back://返回
                this.finish();
                break;
            case R.id.reg_frame://查看密码
                if (!flag) {
                    // 显示
                    reg_show.setImageResource(R.mipmap.psw_show);
                    reg_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    reg_show.setImageResource(R.mipmap.psw_gone);
                    reg_password.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                // 使光标始终在最后位置
                Editable etable = reg_password.getText();
                Selection.setSelection(etable, etable.length());
                flag = !flag;
                break;
            case R.id.send_sms://发送短信验证码
                final String phone = reg_phone.getText().toString().trim();
                if (phone.length() == 11) {
                    send_sms.setClickable(false);
                    send_sms.setBackgroundResource(R.drawable.btn_tou);
                    dataInterface.phoneExist(phone, new NetManager.DataArray() {
                        @Override
                        public void getServiceData(JSONArray jsonArray) {
                            if (jsonArray.length() > 0) {
                                try {
                                    JSONObject object = jsonArray.getJSONObject(0);
                                    String resp = object.getString("resp");
                                    if (resp.equals("000200")) {
                                        send_sms.setClickable(true);
                                        send_sms.setBackgroundResource(R.drawable.login_btn);
                                        toast.setText("手机号码已注册");
                                        toast.show();
                                    } else {
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
                                                            send_sms.setClickable(true);
                                                            send_sms.setBackgroundResource(R.drawable.login_btn);
                                                            Toast.makeText(RegisteredActivity.this, "请输入正确的手机号码",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }, errorListener);
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
            case R.id.register_now://注册
                if (System.currentTimeMillis() - lastClick < 2000) {
                    return;
                }
                lastClick = System.currentTimeMillis();

                final String phoneNumber = reg_phone.getText().toString().trim();
                final String psw = reg_password.getText().toString().trim();
                final String sms = sms_yanzheng.getText().toString().trim();
                if (phoneNumber.length() != 11) {
                    toast.setText("请输入正确的手机号码");
                    toast.show();
                    return;
                }
                dataInterface.phoneExist(phoneNumber, new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        if (jsonArray.length() > 0) {
                            try {
                                JSONObject object = jsonArray.getJSONObject(0);
                                String resp = object.getString("resp");
                                if (resp.equals("000200")) {
                                    toast.setText("手机号码已注册");
                                    toast.show();
                                } else {
                                    if (psw.length() < 6) {
                                        toast.setText("请输入六位及以上密码");
                                        toast.show();
                                    } else if (Utils.StringFilter(psw).length() != psw.length()) {
                                        toast.setText("密码只能输入字母、数字和下划线");
                                        toast.show();
                                    } else if (sms.equals("")) {
                                        toast.setText("短信验证码错误");
                                        toast.show();
                                    } else {
                                        dataInterface.registered(MyApplication.getPhoneStr(RegisteredActivity.this), phoneNumber, Utils.getMD5Str
                                                (psw), sms, new NetManager.DataArray() {
                                            @Override
                                            public void getServiceData(JSONArray jsonArray) {
                                                if (jsonArray.length() > 0) {
                                                    try {
                                                        JSONObject object = jsonArray.getJSONObject(0);
                                                        String resp = object.getString("resp");
                                                        String content = object.getString("msg");
                                                        if (resp.equals("000000")) {
                                                            Toast.makeText(RegisteredActivity.this, "注册成功!", Toast.LENGTH_SHORT).show();
                                                            JSONObject jsonObject = object.getJSONObject("userInfo");
                                                            int userId = jsonObject.getInt("id");
                                                            SP.set(RegisteredActivity.this, SP.USER_ID, "" + userId);
                                                            SP.set(RegisteredActivity.this, "userPhone", phoneNumber);
                                                            SP.set(RegisteredActivity.this, "userPassWord", Utils.getMD5Str(psw));
                                                            startActivity(new Intent(RegisteredActivity.this, NickNameActivity.class));
                                                            RegisteredActivity.this.finish();
                                                        } else {
                                                            toast.setText(content);
                                                            toast.show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }, errorListener);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, errorListener);
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
                        send_sms.setClickable(true);
                        send_sms.setText("再次发送");
                        send_sms.setBackgroundResource(R.drawable.login_btn);
                    } else {
                        send_sms.setText(msg.arg1 + "秒");
                    }
                    break;
                case 1:
                    if (msg.obj.toString() != null) {
                        sms_yanzheng.setText(msg.obj.toString());
                    } else {
                        Toast.makeText(RegisteredActivity.this, "自动填写验证码失败!",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, c);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getContentResolver().unregisterContentObserver(c);
    }

    ContentObserver c = new ContentObserver(handler) {
        @SuppressWarnings("deprecation")
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Uri uri = Uri.parse("content://sms/inbox");
            String[] projection = new String[]{"address", "person", "body"};
            String sortOrder = "_id desc";

            String phone = reg_phone.getText().toString().trim();
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
