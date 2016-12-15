package com.rdcx.randian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/5/5 0005.
 * <p/>
 * 账户安全发送短信验证码的页面
 */
public class AccountSmsActivity extends Activity implements View.OnClickListener {

    private String phoneNumber;
    private String title;
    private String button;
    private String smsNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_sms);

        initParams();
        initViews();
    }

    private void initParams() {
        Intent intent = getIntent();
        if (intent != null) {
            phoneNumber = intent.getStringExtra("phoneNumber");
            title = intent.getStringExtra("title");
            button = intent.getStringExtra("button");
        } else {
            phoneNumber = "";
            title = "";
            button = "";
        }
    }

    private void initViews() {
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(title);

        textView = (TextView) findViewById(R.id.next);
        textView.setText(button);

        if (phoneNumber != null && phoneNumber.length() > 0) {
            findViewById(R.id.phone_number_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.phone_number_layout).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.set_back).setOnClickListener(this);
        findViewById(R.id.forget_send_sms).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
    }

    private NetManager.DataArray success = new NetManager.DataArray() {
        @Override
        public void getServiceData(JSONArray jsonArray) {
            try {
                JSONObject jo = jsonArray.getJSONObject(0);
                if ("000090".equals(jo.getString("resp"))) {
                    Toast.makeText(AccountSmsActivity.this, "短信验证码发送成功，请注意查收。", Toast.LENGTH_LONG).show();
                } else {
                    throw new JSONException("");
                }
            } catch (JSONException e) {
                Toast.makeText(AccountSmsActivity.this, "短信验证码发送失败，请稍后再试。", Toast.LENGTH_LONG).show();
                Log.e("test", "AccountSmsActivity success cause an Exception=>:", e);
            }
        }
    };

    private Response.ErrorListener error = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (Utils.isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "网络异常，请稍候再试。", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.arg1 <= 0) {
                Button button = (Button) findViewById(R.id.forget_send_sms);
                button.setText("再次发送");
                button.setBackgroundResource(R.drawable.login_btn);
                button.setClickable(true);
            } else {
                Button button = (Button) findViewById(R.id.forget_send_sms);
                button.setText(msg.arg1 + "秒");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_back:
                finish();
                break;
            case R.id.forget_send_sms:
                if (phoneNumber != null && phoneNumber.length() > 0) {
                    Button button = (Button) findViewById(R.id.forget_send_sms);
                    button.setClickable(false);
                    button.setBackgroundResource(R.drawable.btn_tou);
                    Utils.timing(handler, 0);
                    DB.getDataInterface(this).sendSMS(phoneNumber, success, error);
                } else {
                    EditText editText = (EditText) findViewById(R.id.forget_phone);
                    if (editText.getText() != null && (phoneNumber = editText.getText().toString()).length() > 0 && Pattern.matches("^1\\d{10}$", phoneNumber)) {
                        Button button = (Button) findViewById(R.id.forget_send_sms);
                        button.setClickable(false);
                        button.setBackgroundResource(R.drawable.btn_tou);
                        Utils.timing(handler, 0);
                        DB.getDataInterface(this).sendSMS(phoneNumber, success, error);
                    } else {
                        editText.requestFocus();
                        Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.next:
                EditText editText = (EditText) findViewById(R.id.forget_sms);
                if (editText.getText() != null && (smsNumber = editText.getText().toString()).length() > 0 && Pattern.matches("^\\d{6}$", smsNumber) && phoneNumber != null && Pattern.matches("^1\\d{10}$", phoneNumber)) {
                    Intent data = new Intent();
                    data.putExtra("phoneNumber", phoneNumber);
                    data.putExtra("smsNumber", smsNumber);
                    setResult(0, data);
                    finish();
                } else {
                    editText.requestFocus();
                    Toast.makeText(this, "请输入正确短信验证码", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

}
