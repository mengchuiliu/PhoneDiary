package com.rdcx.randian.wxapi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rdcx.randian.HomeActivity;
import com.rdcx.randian.LoginActivity;
import com.rdcx.randian.MyApplication;
import com.rdcx.tools.SP;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/12/11 0011.
 *
 * @author mengchuiliu
 */
public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    String token;
    ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
        ((MyApplication) getApplication()).addActivity(this);
        MyApplication.api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        MyApplication.api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        // 微信请求第三方返回
    }

    @Override
    public void onResp(BaseResp resp) {
        // 第三方请求微信返回
        // AppSecret：a64444b565d22bdf91a69ae64c012999
        //
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Log.e("my_log", "==微信发送成功==");
                if (MyApplication.flag) {
//                    progressDialog = ProgressDialog.show(this, "", "登录中...");
//                    progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//
//                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                                if (progressDialog.isShowing()) {
//                                    progressDialog.dismiss();
//                                }
//                            }
//                            return false;
//                        }
//                    });
                    String code = ((SendAuth.Resp) resp).token;
                    getWXId(code);
                } else {
                    Toast.makeText(WXEntryActivity.this, "微信分享成功!", Toast.LENGTH_SHORT).show();
                    if (MyApplication.wxType == 1) {
                        startActivity(new Intent(WXEntryActivity.this, HomeActivity.class));
                        WXEntryActivity.this.finish();
                    } else if (MyApplication.wxType == 2) {
                        WXEntryActivity.this.finish();
                    } else if (MyApplication.wxType == 3) {
                        WXEntryActivity.this.finish();
                    }
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if (MyApplication.flag) {
                    startActivity(new Intent(WXEntryActivity.this, LoginActivity.class));
                }
                Toast.makeText(WXEntryActivity.this, "微信访问取消!", Toast.LENGTH_SHORT).show();
                WXEntryActivity.this.finish();
                Log.e("my_log", "==微信发送取消==");
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                if (MyApplication.flag) {
                    startActivity(new Intent(WXEntryActivity.this, LoginActivity.class));
                }
                Toast.makeText(WXEntryActivity.this, "微信拒绝访问!", Toast.LENGTH_SHORT).show();
                WXEntryActivity.this.finish();
                Log.e("my_log", "==微信发送被拒绝==");
                break;
            default:
                if (MyApplication.flag) {
                    startActivity(new Intent(WXEntryActivity.this, LoginActivity.class));
                }
                Toast.makeText(WXEntryActivity.this, "微信访问返回,请稍后再试!", Toast.LENGTH_SHORT).show();
                WXEntryActivity.this.finish();
                Log.e("my_log", "==发送返回==");
                break;
        }
    }

    private void getWXId(final String code) {
        RequestQueue mQueue = Volley.newRequestQueue(WXEntryActivity.this);
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + MyApplication.APP_ID + "&secret="
                + "94a67cb790f0a61cc5d4e0ded41558e6" + "&code=" + code + "&grant_type=authorization_code";
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            token = jsonObject.getString("openid");
                            SP.set(WXEntryActivity.this, "weixinToken", token);
                            WXEntryActivity.this.finish();
//                            new NetDataGetter(getApplicationContext()).login(MyApplication.getPhoneStr(WXEntryActivity.this), "", token, "1",
//                                    token,
//                                    new NetManager.DataArray() {
//                                        @Override
//                                        public void getServiceData(JSONArray jsonArray) {
//                                            if (progressDialog != null && progressDialog.isShowing()) {
//                                                progressDialog.dismiss();
//                                            }
//                                            int userId;
//                                            String nickName;
//                                            String photoPath;
//                                            long createTime;
//                                            //登陆成功
//                                            try {
//                                                JSONObject object = jsonArray.getJSONObject(0);
//                                                String resp = object.getString("resp");
//                                                if (resp.equals("000000")) {
//                                                    Toast.makeText(getApplicationContext(), "登录成功!", Toast
//                                                            .LENGTH_SHORT).show();
//                                                    JSONObject jsonObject = object.getJSONObject("userInfo");
//                                                    userId = jsonObject.getInt("id");
//                                                    nickName = jsonObject.getString("phoneName");
//                                                    photoPath = jsonObject.getString("photoPath");
//                                                    JSONObject jsonObject2 = jsonObject.getJSONObject("createDate");
//                                                    createTime = jsonObject2.getLong("time");
//
//                                                    SP.set(WXEntryActivity.this, SP.USER_ID, "" + userId);
//                                                    SP.set(WXEntryActivity.this, "nickName", nickName);
//                                                    SP.set(WXEntryActivity.this, "photoPath", photoPath);
//                                                    SP.set(WXEntryActivity.this, "login_type", "1");
//                                                    SP.set(WXEntryActivity.this, "token", token);
//                                                    SP.set(WXEntryActivity.this, "createTime", createTime);
//                                                    SP.set(WXEntryActivity.this, "isLogin", true);
//
//                                                    //第一次登录下载日记本数据
//                                                    boolean DownloadDiary = SP.getBoolean(WXEntryActivity.this, "DownloadDiary", true);
//                                                    if (DownloadDiary) {
//                                                        //下载日记本
//                                                        new Thread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                Download.downloadDiary(WXEntryActivity.this);
//                                                            }
//                                                        }).start();
//                                                    }
//                                                    // 登陆成功跳转页面
//                                                    boolean isFirstLogin = SP.getBoolean(WXEntryActivity.this, "isFirstLogin", false);
//                                                    if (isFirstLogin && TextUtils.isEmpty(nickName.trim())) {
//                                                        startActivity(new Intent(WXEntryActivity.this, NickNameActivity.class));
//                                                    } else {
//                                                        // 登陆成功跳转页面
//                                                        startActivity(new Intent(WXEntryActivity.this, HomeActivity.class));
//                                                    }
//                                                    WXEntryActivity.this.finish();
//                                                } else if (resp.equals("000220")) {
//                                                    // 绑定设备
//                                                    String msg = object.getString("msg");
//                                                    MessageTools.showMessageOKCancel(WXEntryActivity.this, msg, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            BindingTools.bindingDevice(WXEntryActivity.this, "1", "", token);
//                                                        }
//                                                    });
//                                                } else {
//                                                    String msg = object.getString("msg");
//                                                    msg = (msg != null && msg.length() > 0) ? msg : "登录失败!请稍后重试!";
//                                                    Toast.makeText(WXEntryActivity.this, msg, Toast.LENGTH_LONG).show();
//                                                    WXEntryActivity.this.finish();
//                                                }
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }, new Response.ErrorListener() {
//                                        @Override
//                                        public void onErrorResponse(VolleyError volleyError) {
//                                            if (Utils.isNetworkAvailable(getApplicationContext())) {
//                                                Toast.makeText(getApplicationContext(),
//                                                        "登录失败，请稍后重试！", Toast.LENGTH_LONG).show();
//                                            } else {
//                                                Toast.makeText(getApplicationContext(),
//                                                        "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
//                                            }
//                                        }
//                                    });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "微信登录失败！", Toast.LENGTH_LONG).show();
                startActivity(new Intent(WXEntryActivity.this, LoginActivity.class));
                WXEntryActivity.this.finish();
            }
        });
        mQueue.add(stringRequest);
    }

}
