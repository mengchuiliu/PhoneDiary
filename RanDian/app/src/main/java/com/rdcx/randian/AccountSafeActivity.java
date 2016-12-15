package com.rdcx.randian;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.MessageTools;
import com.rdcx.tools.SP;
import com.rdcx.utils.AccessTokenKeeper;
import com.rdcx.utils.Utils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/4/14 0014.
 * <p/>
 * 账户安全
 */
public class AccountSafeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String BINDED = "已绑定";

    private static final String UNBIND = "未绑定";

    private static final int BIND_PHONE = 0;
    private static final int UNBIND_PHONE = 1;
    private static final int UNBIND_DEVICE = 2;

    private ProgressDialog progressDialog;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_safe);
        ((MyApplication) getApplication()).addActivity(this);

        try {
            queryBindState();
        } catch (Exception e) {
            Log.e("test", "AccountSafeActivity initBindState error=>:", e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        String weixinToken = SP.getString(this, "weixinToken", "");
        if (weixinToken.length() > 0) {
            SP.set(this, "weixinToken", "");
            showProgressDialog(null);
            token = weixinToken;
            bind("1", token, null, "");
        }
    }

    private void queryBindState() {

        showProgressDialog("状态刷新中...");

        DB.getDataInterface(this).login(MyApplication.getPhoneStr(this),
                SP.getString(this, "phoneNumber", ""),
                SP.getString(this, "password", ""),
                SP.getString(this, "login_type", ""),
                SP.getString(this, "token", ""), new NetManager.DataArray() {
                    @Override
                    public void getServiceData(org.json.JSONArray jsonArray) {
                        try {
                            org.json.JSONObject object = jsonArray.getJSONObject(0);
                            String resp = object.getString("resp");
                            if (resp.equals("000000")) {

                                String phoneNumber = object.getJSONObject("userInfo").getString("phoneNumber");
                                SP.set(AccountSafeActivity.this, "phoneNumber", phoneNumber);

                                org.json.JSONArray modelList = object.getJSONObject("userInfo").getJSONArray("modelList");
                                String bindState = modelList == null ? "[]" : modelList.toString();
                                SP.set(AccountSafeActivity.this, SP.BIND_STATE, bindState);

                            } else {
                                Toast.makeText(AccountSafeActivity.this, "刷新用户绑定状态时失败。", Toast.LENGTH_SHORT).show();
                            }
                            initBindState();
                        } catch (JSONException e) {
                            SP.set(AccountSafeActivity.this, SP.BIND_STATE, "[]");
                            initBindState();
                        }
                    }
                }, errorFunc);
    }

    private JSONObject getModel(JSONArray bindState, int type) {
        for (int i = 0; i < bindState.size(); i++) {
            JSONObject model = bindState.getJSONObject(i);
            int modelType = model.getInteger("type");
            if (modelType == type) {
                return model;
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        return jsonObject;
    }

    private void initBindState() {

        String bindState = SP.getString(this, SP.BIND_STATE, "[]");
        JSONArray bindStateJA = JSONArray.parseArray(bindState);

        int[] ids = new int[]{R.id.set_back, R.id.bind_phone_number, R.id.bind_phone_device, R.id.bind_qq, R.id.bind_weixin, R.id.bind_weibo};
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(this);
            }
            JSONObject jo = null;
            if (view instanceof RelativeLayout) {
                switch (id) {
                    case R.id.bind_phone_number:
                        String phoneNumber = SP.getString(this, "phoneNumber", "");
                        if (phoneNumber.length() > 0) {
                            ((TextView) ((RelativeLayout) view).getChildAt(0)).setText("已绑定手机    " + phoneNumber.replaceAll("^(\\d{3})\\d{4}(\\d{4})$", "$1****$2"));
                            ((TextView) ((RelativeLayout) view).getChildAt(1)).setText(BINDED);
                            view.setTag(true);
                        } else {
                            ((TextView) ((RelativeLayout) view).getChildAt(0)).setText("未绑定手机");
                            ((TextView) ((RelativeLayout) view).getChildAt(1)).setText(UNBIND);
                            view.setTag(false);
                        }
                        break;
                    case R.id.bind_qq:
                        jo = getModel(bindStateJA, 0);
                        jo.put("text", "QQ");
                    case R.id.bind_weixin:
                        if (jo == null) {
                            jo = getModel(bindStateJA, 1);
                            jo.put("text", "微信");
                        }
                    case R.id.bind_weibo:
                        if (jo == null) {
                            jo = getModel(bindStateJA, 2);
                            jo.put("text", "微博");
                        }
                        view.setTag(jo);
                        if (jo.getString("token") == null) {
                            ((TextView) ((RelativeLayout) view).getChildAt(1)).setText(UNBIND);
                        } else {
                            ((TextView) ((RelativeLayout) view).getChildAt(1)).setText(BINDED);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        hideProgressDialog();

    }

    @Override
    public void onClick(View v) {
        Object tag;
        switch (v.getId()) {
            case R.id.set_back: // 返回按钮
                finish();
                break;
            case R.id.bind_phone_number:
                if ((tag = v.getTag()) != null && tag instanceof Boolean) {
                    if ((boolean) tag) {
                        Intent intent = new Intent(this, AccountSmsActivity.class);
                        String phoneNumber = SP.getString(this, "phoneNumber", "");
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("title", "解绑手机号码");
                        intent.putExtra("button", "解绑");
                        startActivityForResult(intent, UNBIND_PHONE);
                    } else {
                        Intent intent = new Intent(this, AccountSmsActivity.class);
                        intent.putExtra("title", "绑定手机号码");
                        intent.putExtra("button", "绑定");
                        startActivityForResult(intent, BIND_PHONE);
                    }
                }
                break;
            case R.id.bind_phone_device:
                MessageTools.showMessageOKCancel(this, "您真的要解除对本设备的绑定吗？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog(null);
                        String phoneNumber = SP.getString(AccountSafeActivity.this, "phoneNumber", "");
                        if (phoneNumber.length() > 0) {
                            Intent intent = new Intent(AccountSafeActivity.this, AccountSmsActivity.class);
                            intent.putExtra("phoneNumber", phoneNumber);
                            intent.putExtra("title", "解绑设备");
                            intent.putExtra("button", "解绑");
                            startActivityForResult(intent, UNBIND_DEVICE);
                        } else {
                            DB.getDataInterface(AccountSafeActivity.this).loginOut(SP.getString(AccountSafeActivity.this, SP.USER_ID, ""),
                                    new NetManager.DataArray() {
                                        @Override
                                        public void getServiceData(org.json.JSONArray jsonArray) {
                                            try {
                                                org.json.JSONObject object = jsonArray.getJSONObject(0);
                                                String resp = object.getString("resp");
                                                if (resp.equals("000000")) {
                                                    SP.set(AccountSafeActivity.this, "isLogin", false);
                                                    unbind("4", "", "");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, errorFunc);
                        }
                    }
                });
                break;
            case R.id.bind_qq: // 解绑或绑定QQ
            case R.id.bind_weixin:
            case R.id.bind_weibo:
                if ((tag = v.getTag()) != null && tag instanceof JSONObject) {
                    if (((JSONObject) tag).getString("token") == null) {
                        final int type = ((JSONObject) tag).getInteger("type");
                        if (type == 0) { // 绑定QQ
                            if (!MyApplication.mTencent.isSessionValid()) {
                                showProgressDialog(null);
                                MyApplication.mTencent.login(this, "all", new BaseUiListener());
                            } else {
                                // 注销登陆
                                MyApplication.mTencent.logout(this);
                            }
                            break;
                        } else if (type == 1) { // 绑定微信
                            showProgressDialog(null);
                            MyApplication.flag = true;
                            SP.set(this, "weixinToken", "");
                            final SendAuth.Req req = new SendAuth.Req();
                            req.scope = "snsapi_userinfo";
                            req.state = "wechat_sdk_demo_test";
                            MyApplication.api.sendReq(req);
                            if (MyApplication.api.sendReq(req)) {
                                hideProgressDialog();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), "微信授权失败", Toast.LENGTH_SHORT).show();
                            }
                        } else if (type == 2) { // 绑定微博
                            showProgressDialog(null);
                            SsoHandler mSsoHandler = new SsoHandler(this, MyApplication.mAuthInfo);
                            mSsoHandler.authorize(new WeiboAuthListener() {
                                @Override
                                public void onWeiboException(WeiboException arg0) {
                                    hideProgressDialog();
                                    Toast.makeText(getApplicationContext(), "微博授权时出现异常。", Toast.LENGTH_SHORT).show();
                                }

                                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onComplete(Bundle values) {
                                    Oauth2AccessToken mAccessToken = Oauth2AccessToken.parseAccessToken(values);
                                    if (mAccessToken.isSessionValid()) {
                                        AccessTokenKeeper.writeAccessToken(AccountSafeActivity.this, mAccessToken);
                                        String uid = mAccessToken.getUid();
                                        token = uid;
                                        token = mAccessToken.getToken();
                                        // 保存Token
                                        bind("2", token, null, "");
                                    } else {
                                        // 当您注册的应用程序签名不正确时，就会收到错误Code，请确保签名正确
                                        String code = values.getString("code", "");
                                        Log.e("my_log", "新浪Code错误" + code);
                                        hideProgressDialog();
                                        Toast.makeText(getApplicationContext(), "微博授权失败", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    hideProgressDialog();
                                    Toast.makeText(getApplicationContext(), "微博取消授权", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        final int type = ((JSONObject) tag).getInteger("type");
                        final String token = ((JSONObject) tag).getString("token");
                        String text = ((JSONObject) tag).getString("text");
                        MessageTools.showMessageOKCancel(this, "您确定要解除对 " + text + " 账号的绑定吗？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showProgressDialog(null);
                                unbind(String.valueOf(type), token, "");
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    private NetManager.DataArray successFunc = new NetManager.DataArray() {
        @Override
        public void getServiceData(org.json.JSONArray jsonArray) {
            Log.d("test", "AccountSafeActivity successFunc jsonArray=>:" + jsonArray);
            try {
                org.json.JSONObject object = jsonArray.getJSONObject(0);
                String resp = object.getString("resp");
                if (resp.equals("000000")) {
                    Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_SHORT).show();
                    queryBindState();
                } else {
                    hideProgressDialog();
                    String msg = object.getString("msg");
                    msg = msg == null ? "失败" : msg;
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
                if (!SP.getBoolean(getApplicationContext(), "isLogin", false)) {
                    SP.set(getApplicationContext(), SP.USER_ID, "");
                    startActivity(new Intent(AccountSafeActivity.this, LoginActivity.class));
                }
            } catch (JSONException e) {
                Log.e("test", "AccountSafeActivity successFunc cause an Exception=>:", e);
                hideProgressDialog();
            }
        }
    };

    private Response.ErrorListener errorFunc = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            hideProgressDialog();
            if (Utils.isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "网络异常，请稍候再试。", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void unbind(String logType, String token, String smsNumber) {
        String userId = SP.getString(this, SP.USER_ID, "");
        String phoneNumber = SP.getString(this, "phoneNumber", "");
        String login_type = SP.getString(this, "login_type", "");
        if (login_type.equals(logType)) {
            hideProgressDialog();
            Toast.makeText(this, "当前登陆方式不能解绑", Toast.LENGTH_SHORT).show();
        } else {
            DB.getDataInterface(this).unbinding(userId, logType, phoneNumber, token, smsNumber, successFunc, errorFunc);
        }
    }

    public void bind(String logType, String token, String phoneNumber, String smsNumber) {
        String userId = SP.getString(this, SP.USER_ID, "");
        DB.getDataInterface(this).binding(userId, logType == null ? "" : logType, logType == null ? "" : logType, phoneNumber == null ? "" : phoneNumber, token == null ? "" : token, "", smsNumber == null ? "" : smsNumber, successFunc, errorFunc);
    }

    private void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, "", msg == null ? "请稍候..." : msg);
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                    return false;
                }
            });
        } else {
            progressDialog.setMessage(msg == null ? "请稍候..." : msg);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    // qq信息接口
    class BaseUiListener implements IUiListener {
        // 取消授权
        @Override
        public void onCancel() {
            hideProgressDialog();
            Toast.makeText(getApplicationContext(), "QQ取消授权", Toast.LENGTH_SHORT).show();
        }

        // 登陆成功
        @Override
        public void onComplete(Object response) {
            try {
                org.json.JSONObject jsonObject = (org.json.JSONObject) response;
                token = jsonObject.getString("access_token");
                bind("0", token, null, "");
            } catch (JSONException e) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "QQ授权登录失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(UiError error) {
            hideProgressDialog();
            Toast.makeText(getApplicationContext(), "QQ授权登录失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || data.getExtras() == null) {
            hideProgressDialog();
            return;
        }
        String smsNumber;
        String phoneNumber;
        switch (requestCode) {
            case BIND_PHONE:
                smsNumber = data.getStringExtra("smsNumber");
                phoneNumber = data.getStringExtra("phoneNumber");
                Log.d("test", "AccountSafeActivity phoneNumber=>:" + phoneNumber + ",smsNumber=>:" + smsNumber);
                if (smsNumber != null && Pattern.matches("\\d{6}", smsNumber) && phoneNumber != null && Pattern.matches("1\\d{10}", phoneNumber)) {
                    bind("3", "", phoneNumber, smsNumber);
                } else {
                    hideProgressDialog();
                }
                break;
            case UNBIND_PHONE:
                smsNumber = data.getStringExtra("smsNumber");
                if (smsNumber != null && Pattern.matches("\\d{6}", smsNumber)) {
                    unbind("3", "", smsNumber);
                } else {
                    hideProgressDialog();
                }
                break;
            case UNBIND_DEVICE:
                final String unbindDeviceSmsNumber = data.getStringExtra("smsNumber");
                if (unbindDeviceSmsNumber != null && Pattern.matches("\\d{6}", unbindDeviceSmsNumber)) {
                    DB.getDataInterface(AccountSafeActivity.this).loginOut(SP.getString(AccountSafeActivity.this, SP.USER_ID, ""),
                            new NetManager.DataArray() {
                                @Override
                                public void getServiceData(org.json.JSONArray jsonArray) {
                                    try {
                                        org.json.JSONObject object = jsonArray.getJSONObject(0);
                                        String resp = object.getString("resp");
                                        if (resp.equals("000000")) {
                                            SP.set(AccountSafeActivity.this, "isLogin", false);
                                            unbind("4", "", unbindDeviceSmsNumber);
                                        }
                                    } catch (JSONException e) {
                                        hideProgressDialog();
                                        e.printStackTrace();
                                    }
                                }
                            }, errorFunc);
                } else {
                    hideProgressDialog();
                }
                break;
            default:
                break;
        }
    }

}
