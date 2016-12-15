package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.collect.Lists;
import com.rdcx.fragments.GuideFragment;
import com.rdcx.myview.WaterWave;
import com.rdcx.service.NetManager;
import com.rdcx.service.UploadDiaryAT;
import com.rdcx.tools.DB;
import com.rdcx.tools.SP;
import com.rdcx.tools.ServiceUtils;
import com.rdcx.utils.Utils;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;

import org.json.JSONArray;

import java.io.File;
import java.util.List;

import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerModelManager;
import springindicator.SpringIndicator;
import springindicator.viewpager.ScrollerViewPager;

@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        ((MyApplication) getApplication()).addActivity(this);

        userId = SP.getString(WelcomeActivity.this, SP.USER_ID, "-1");
        //友盟推送
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
        mPushAgent.onAppStart();
        String device_token = UmengRegistrar.getRegistrationId(this);
        Log.e("my_log", "=====推送=====>" + device_token);

        upOnlyStr();
        isNewVersion();//新版本重新显示引导页
        boolean isWelcome = SP.getBoolean(WelcomeActivity.this, "isWelcome", false); // 欢迎界面显示
        long standTime = SP.oneDayOnceStamp(this, "WELCOME_PAGE_STAND", System.currentTimeMillis()) ? 1500 : 600; // 一天中的第一次显示久一点，不是第一次显示时间短一点
        if (isWelcome) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean isLogin = SP.getBoolean(WelcomeActivity.this, "isLogin", false);
                    if (isLogin) {
                        startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                        WelcomeActivity.this.finish();
//                        new NetDataGetter(getApplicationContext()).login(MyApplication.phoneStr,
//                                SP.getString(WelcomeActivity.this, "phoneNumber", ""),
//                                SP.getString(WelcomeActivity.this, "password", ""),
//                                SP.getString(WelcomeActivity.this, "login_type", ""),
//                                SP.getString(WelcomeActivity.this, "token", ""), new NetManager.DataArray() {
//                                    @Override
//                                    public void getServiceData(JSONArray jsonArray) {
//                                        try {
//                                            JSONObject object = jsonArray.getJSONObject(0);
//                                            String resp = object.getString("resp");
//                                            String photoPath;
//                                            if (resp.equals("000000")) {
//                                                JSONObject jsonObject = object.getJSONObject("userInfo");
//                                                photoPath = jsonObject.getString("photoPath");
//                                                if (!TextUtils.isEmpty(photoPath)) {
//                                                    String locpath = Environment.getExternalStorageDirectory() +
//                                                            "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
//                                                    File file = new File(locpath);
//                                                    if (!file.exists()) {
//                                                        Utils.getImage(Constants.head_url + photoPath.replace("\\",
//                                                                "/"), handler, 0);
//                                                    } else {
//                                                        startActivity(new Intent(WelcomeActivity.this, HomeActivity
//                                                                .class));
//                                                        WelcomeActivity.this.finish();
//                                                    }
//                                                }
//                                            } else {
//                                                Toast.makeText(getApplicationContext(), "系统繁忙,请重新登录！",
//                                                        Toast.LENGTH_SHORT).show();
//                                                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
//                                                WelcomeActivity.this.finish();
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }, new Response.ErrorListener() {
//                                    @Override
//                                    public void onErrorResponse(VolleyError volleyError) {
//                                        if (Utils.isNetworkAvailable(getApplicationContext())) {
//                                            Toast.makeText(getApplicationContext(), "登录失败,请稍后重试!",
//                                                    Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            Toast.makeText(getApplicationContext(),
//                                                    "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
//                                        }
//                                        SP.set(WelcomeActivity.this, "isLogin", false);
//                                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
//                                        WelcomeActivity.this.finish();
//                                    }
//                                });
                    } else {
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                        WelcomeActivity.this.finish();
                    }
                }
            }, standTime);
        } else {
            initView();
        }
    }

    private void isNewVersion() {
        int lastCode = SP.getInt(WelcomeActivity.this, "versionCode", 0);
        try {
            // 当前版本的版本号
            PackageInfo info = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), 0);
            int code = info.versionCode;
            if (lastCode == 0 || code > lastCode) {
                SP.set(WelcomeActivity.this, "isWelcome", false);
                SP.set(WelcomeActivity.this, "versionCode", code);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    //第一次登录上传渠道号
    private void upOnlyStr() {
        boolean isUp = SP.getBoolean(WelcomeActivity.this, "OnlyStr", true);
        if (isUp) {
            DB.getDataInterface(WelcomeActivity.this).updateStr(MyApplication.getPhoneStr(WelcomeActivity.this), Utils.getChannelName(WelcomeActivity.this), new NetManager.DataArray() {
                @Override
                public void getServiceData(JSONArray jsonArray) {
                    Log.e("my_log", "----OnlyStr----->" + MyApplication.getPhoneStr(WelcomeActivity.this));
                    File cacheFile = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/DiaryCacheImage");
                    if (cacheFile.exists()) {
                        UploadDiaryAT.deleteDir(cacheFile);
                    }
                    SP.set(WelcomeActivity.this, "OnlyStr", false);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
        }
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
                        Log.e("my_log", "welcome===>" + "头像保存失败！");
                    }
                    startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
                    WelcomeActivity.this.finish();
                    break;
            }
        }
    };

    private ScrollerViewPager viewPager;
    private PagerModelManager manager;
    private ModelPagerAdapter adapter;

    private void initView() {
        findViewById(R.id.re_wel).setVisibility(View.VISIBLE);
        viewPager = (ScrollerViewPager) findViewById(R.id.view_pager);
        WaterWave btn_wel = (WaterWave) findViewById(R.id.btn_wel);
        btn_wel.setOnClickListener(this);
        SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id.indicator);

        manager = new PagerModelManager();
        manager.addCommonFragment(GuideFragment.class, getBgRes(), getTitles());
        adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
        viewPager.setAdapter(adapter);
        viewPager.fixScrollSpeed();

        // just set viewPager
        springIndicator.setViewPager(viewPager, btn_wel, springIndicator);
    }

    protected List<String> getTitles() {
        return Lists.newArrayList("⊙", "⊙", "⊙", "⊙", "⊙");
    }

    //欢迎页显示页面集合
    protected List<Integer> getBgRes() {
        return Lists.newArrayList(R.mipmap.wel_1, R.mipmap.wel_2, R.mipmap.wel_3, R.mipmap.wel_5, R.mipmap.wel_4);
    }

    @Override
    public void onClick(View v) {
        SP.set(WelcomeActivity.this, "isWelcome", true);
        SP.set(WelcomeActivity.this, "isFirstLogin", true);
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        this.finish();
    }
}
