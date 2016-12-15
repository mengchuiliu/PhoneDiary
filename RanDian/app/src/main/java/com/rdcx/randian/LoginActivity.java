package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.Download;
import com.rdcx.tools.DownloadThread;
import com.rdcx.tools.MessageTools;
import com.rdcx.tools.SP;
import com.rdcx.utils.AccessTokenKeeper;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText phoneEditText, passwordEditText;
    private ImageView psw_image;
    boolean flag = false;//密码显示
    private DataInterface dataInterface;//数据接口
    private Toast toast;
    ProgressDialog progressDialog = null;
    LinearLayout ll_root;

    String login_type = "";// 0表示qq，1表示微信，2表示微博，3表示正常登陆
    String phoneNumber = "";
    String password = "";
    String token = "";// 第三方登录返回标识码
    String userId;
    boolean isLogin = false;

    //微博
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((MyApplication) getApplication()).addActivity(this);
        mSsoHandler = new SsoHandler(LoginActivity.this, MyApplication.mAuthInfo);

        dataInterface = new NetDataGetter(getApplicationContext());
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        initView();

        isLogin = SP.getBoolean(LoginActivity.this, "isLogin", false);
    }

    private void initView() {
        ll_root = (LinearLayout) findViewById(R.id.root);
        phoneEditText = (EditText) findViewById(R.id.phone);
        passwordEditText = (EditText) findViewById(R.id.password);
        psw_image = (ImageView) findViewById(R.id.psw_image);

        findViewById(R.id.psw_show).setOnClickListener(this);
        findViewById(R.id.registered).setOnClickListener(this);
        findViewById(R.id.forget).setOnClickListener(this);
        findViewById(R.id.weibo_icon).setOnClickListener(this);
        findViewById(R.id.qq_icon).setOnClickListener(this);
        findViewById(R.id.weixin_icon).setOnClickListener(this);
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        if (Build.VERSION.SDK_INT < 22) {
            Utils.controlKeyboardLayout(ll_root, login);
        }
    }

    long lastClick = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login://登录
                if (System.currentTimeMillis() - lastClick < 2000) {
                    return;
                }
                lastClick = System.currentTimeMillis();

                phoneNumber = phoneEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();
                if (phoneNumber.length() != 11) {
                    toast.setText("请输入正确的手机号码");
                    toast.show();
                } else if (phoneNumber.length() < 6) {
                    toast.setText("请输入六位以上密码");
                    toast.show();
                } else if (Utils.StringFilter(password).length() != password.length()) {
                    toast.setText("密码只能输入字母、数字和下划线");
                    toast.show();
                } else {
                    showProgressDialog(null);
                    login_type = "3";
                    login();
                }
                break;
            case R.id.registered://注册
                startActivity(new Intent(LoginActivity.this, RegisteredActivity.class));
                break;
            case R.id.forget://忘记密码
                startActivity(new Intent(LoginActivity.this, ForgetPswActivity.class));
                break;
            case R.id.qq_icon://qq登录
                login_type = "0";
                if (!MyApplication.mTencent.isSessionValid()) {
                    showProgressDialog(null);
                    MyApplication.mTencent.login(LoginActivity.this, "all", new BaseUiListener());
                } else {
                    // 注销登陆
                    MyApplication.mTencent.logout(LoginActivity.this);
                }
                break;
            case R.id.weixin_icon://微信登录
                showProgressDialog(null);
                Log.e("my_log", "======" + MyApplication.APP_ID);
                MyApplication.flag = true;
                login_type = "1";
                SP.set(this, "weixinToken", "");
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "wechat_sdk_demo_test";
                MyApplication.api.sendReq(req);
//                Log.e("my_log", "===请求===" + MyApplication.api.sendReq(req));
                if (MyApplication.api.sendReq(req)) {
                    hideProgressDialog();
//                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), "微信授权失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.weibo_icon://微博登录
                login_type = "2";
                showProgressDialog(null);
                mSsoHandler.authorize(new WeiboAuthListener() {
                    @Override
                    public void onWeiboException(WeiboException arg0) {

                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(Bundle values) {
                        // 3562401714
                        Log.e("my_log", "===weibo  uid===>" + values.getString("uid"));
                        mAccessToken = Oauth2AccessToken.parseAccessToken(values);
                        if (mAccessToken.isSessionValid()) {
                            AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
                            String uid = mAccessToken.getUid();
                            token = uid;
                            token = mAccessToken.getToken();
                            // 保存Token
                            Log.e("my_log", "微博授权成功===" + uid);
                            login();
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
                break;
            case R.id.psw_show:
                if (!flag) {
                    // 显示密码
                    psw_image.setImageResource(R.mipmap.psw_show);
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    //隐藏密码
                    psw_image.setImageResource(R.mipmap.psw_gone);
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                // 使光标始终在最后位置
                Editable etable = passwordEditText.getText();
                Selection.setSelection(etable, etable.length());
                flag = !flag;
                break;
        }
    }

    //服务器数据返回监听
    NetManager.DataArray loginListener = new NetManager.DataArray() {
        @Override
        public void getServiceData(JSONArray jsonArray) {
            hideProgressDialog();
            Log.e("my_log", "===登录返回===>" + jsonArray.toString() + jsonArray.length());
            String nickName, photoPath, phoneNumber, psw;
            final long createTime;
            try {
                JSONObject object = jsonArray.getJSONObject(0);
                String resp = object.getString("resp");
                if (resp.equals("000000")) {
                    Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                    JSONObject jsonObject = object.getJSONObject("userInfo");
                    userId = jsonObject.getInt("id") + "";
                    nickName = jsonObject.getString("phoneName");
                    photoPath = jsonObject.getString("photoPath");
                    phoneNumber = jsonObject.getString("phoneNumber");
                    psw = jsonObject.getString("password");
                    JSONObject jsonObject2 = jsonObject.getJSONObject("createDate");
                    createTime = jsonObject2.getLong("time");
                    JSONArray modelList = jsonObject.getJSONArray("modelList");
                    String bindState = modelList == null ? "[]" : modelList.toString();

                    SP.set(LoginActivity.this, "phoneNumber", phoneNumber);
                    SP.set(LoginActivity.this, "password", psw);
                    SP.set(LoginActivity.this, SP.USER_ID, userId);
                    SP.set(LoginActivity.this, "nickName", nickName);
                    SP.set(LoginActivity.this, "photoPath", photoPath);
                    SP.set(LoginActivity.this, "login_type", login_type);
                    SP.set(LoginActivity.this, "token", token);
                    SP.set(LoginActivity.this, "createTime", createTime);
                    SP.set(LoginActivity.this, "isLogin", true);
                    SP.set(LoginActivity.this, SP.BIND_STATE, bindState);

                    if (!TextUtils.isEmpty(photoPath)) {
                        String locpath = Environment.getExternalStorageDirectory() +
                                "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
                        File file = new File(locpath);
                        if (!file.exists()) {
                            Utils.getImage(Constants.head_url + photoPath.replace("\\", "/"), handler, 0);
                        }
                    }
                    // 登陆成功之后下载数据
                    DownloadThread.start(LoginActivity.this);

                    //第一次登录上传渠道好
                    boolean isChannel = SP.getBoolean(LoginActivity.this, "Channel", true);
                    if (isChannel) {
                        DB.getDataInterface(LoginActivity.this).uploadChannel(userId, Utils.getChannelName(LoginActivity.this), new NetManager.DataArray() {
                            @Override
                            public void getServiceData(JSONArray jsonArray) {
                                Log.e("my_log", "----channel----->" + Utils.getChannelName(LoginActivity.this));
                                SP.set(LoginActivity.this, "Channel", false);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                            }
                        });
                    }

                    //第一次登录下载日记本数据
                    boolean DownloadDiary = SP.getBoolean(LoginActivity.this, "DownloadDiary", true);
                    if (DownloadDiary) {
                        //下载日记本
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Download.downloadDiary(LoginActivity.this);
                            }
                        }).start();
                    }

                    boolean isFirstLogin = SP.getBoolean(LoginActivity.this, "isFirstLogin", false);
                    if (isFirstLogin && TextUtils.isEmpty(nickName.trim())) {
                        startActivity(new Intent(LoginActivity.this, NickNameActivity.class));
                    } else {
                        // 登陆成功跳转页面
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    }
                    LoginActivity.this.finish();
                } else if (resp.equals("000220")) {
                    // 绑定设备
                    String msg = object.getString("msg");
                    MessageTools.showMessageOKCancel(LoginActivity.this, msg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bindingDevice();
                        }
                    });
                } else {
                    String msg = object.getString("msg");
                    msg = (msg != null && msg.length() > 0) ? msg : "手机号或密码输入错误";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //服务器返回错误监听
    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.e("test", "LoginActivity volleyError=>:" + volleyError);
            Log.e("my_log", "服务器连接失败==>" + volleyError.getMessage());
            hideProgressDialog();
            if (Utils.isNetworkAvailable(getApplicationContext())) {
                toast.setText("登录失败,请稍后重试!");
                toast.show();
            } else {
                Toast.makeText(getApplicationContext(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void login() {
        dataInterface.login(MyApplication.getPhoneStr(this), phoneNumber, Utils.getMD5Str(password), login_type, token, loginListener, errorListener);
    }

    public void bindingDevice() {

        final Context context = this;

        DB.getDataInterface(context).binding("", "4", login_type, phoneNumber == null ? "" : phoneNumber, token == null ? "" : token, MyApplication.getPhoneStr(context), "",
                new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        try {
                            Log.d("test", "LoginActivity binding jsonArray=>:" + jsonArray);
                            JSONObject object = jsonArray.getJSONObject(0);
                            String resp = object.getString("resp");
                            String msg = object.getString("msg");
                            if (resp.equals("000000")) {
                                Toast.makeText(context, "绑定该设备成功，正在重新登陆。。。", Toast.LENGTH_SHORT).show();
                                login();
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (Utils.isNetworkAvailable(context)) {
                            Toast.makeText(context, "服务器返回失败，请稍后重试。", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "网络连接失败，请检查您的网络是否正常。", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) {
                            File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/cache");
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            String locpath = Environment.getExternalStorageDirectory() +
                                    "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
                            Utils.savePhotoToSDCard(bitmap, locpath);
                        }
                    } else {
                        Log.e("my_log", "login===>" + "头像保存失败！");
                    }
                    break;
            }
        }
    };

    private void showProgressDialog(String msg) {
        progressDialog = ProgressDialog.show(this, "", msg == null ? "登陆中..." : msg);
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
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        String weixinToken = SP.getString(this, "weixinToken", "");
        Log.d("test", "LoginActivity onResume weixinToken=>:" + weixinToken);
        if (weixinToken.length() > 0) {
            SP.set(this, "weixinToken", "");
            showProgressDialog(null);
            token = weixinToken;
            login();
        }
//        progressDialog = ProgressDialog.show(this, "", "登录中...");
//        progressDialog.dismiss();
//
//        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    if (progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                }
//                return false;
//            }
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    // qq信息接口
    class BaseUiListener implements IUiListener {
        // 取消授权
        @Override
        public void onCancel() {
            hideProgressDialog();
            Toast.makeText(getApplicationContext(), "qq取消授权",
                    Toast.LENGTH_SHORT).show();
        }

        // 登陆成功
        @Override
        public void onComplete(Object response) {
            try {
                JSONObject jsonObject = (JSONObject) response;
//                token = jsonObject.getString("openid");
                token = jsonObject.getString("access_token");
                Log.e("my_log", "==openid==" + token + "==access_token==" + token);
                login();
            } catch (JSONException e) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "qq授权登录失败",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError error) {
            hideProgressDialog();
            Toast.makeText(getApplicationContext(), "qq授权登录失败", Toast.LENGTH_SHORT).show();
            Toast.makeText(LoginActivity.this, "onError: " + error.errorDetail,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
        MyApplication.mTencent.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((MyApplication) getApplication()).exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}

