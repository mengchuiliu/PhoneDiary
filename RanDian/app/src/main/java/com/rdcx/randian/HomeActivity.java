package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.igexin.sdk.PushManager;
import com.nineoldandroids.view.ViewHelper;
import com.rdcx.fragments.DiaryFragment;
import com.rdcx.fragments.TaskFragment;
import com.rdcx.myview.HomeViewPager;
import com.rdcx.myview.MyAdapter;
import com.rdcx.service.MyReceiver;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.Diary;
import com.rdcx.tools.Operation;
import com.rdcx.tools.PermissionTools;
import com.rdcx.tools.SP;
import com.rdcx.tools.ServiceUtils;
import com.rdcx.utils.Utils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengService;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class HomeActivity extends FragmentActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private DrawerLayout mDrawerLayout;
    MyAdapter myadapter;
    private HomeViewPager homeViewPager;
    ImageView rb_home, rb_diary, rb_list, rb_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ((MyApplication) getApplication()).addActivity(this);

        //推送昨日日记
        if (SP.oneDayOnceStamp(HomeActivity.this, "YesterDayDimen", System.currentTimeMillis())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<Integer, String> hash = new HashMap<>();
                    for (int i = 0; i < 8; i++) {
                        hash.put(i, null);
                    }
                    boolean flag = false;
                    StringBuilder builder = new StringBuilder();
                    builder.append("昨日账单:").append("\n");
                    DB.findDimension(HomeActivity.this, hash, DB.TYPE_YESTERDAY);
                    for (Integer key : hash.keySet()) {
                        if (hash.get(key) != null && hash.get(key).length() > 0) {
                            flag = true;
                        }
                        builder.append(DiaryShow.getDimenData(key, hash.get(key))).append("\n");
                    }
                    if (flag) {
                        Diary diaryAdd = new Diary();
                        diaryAdd.text = "";
                        diaryAdd.path = "";
                        diaryAdd.data = "";
                        diaryAdd.upload = 1;
                        diaryAdd.time = System.currentTimeMillis();
                        diaryAdd.nowDate = System.currentTimeMillis();
                        if (builder.length() > 1) {
                            builder.deleteCharAt(builder.length() - 1);
                        }
                        diaryAdd.datatext = builder.toString();
                        if (DB.insertOrUpdateDiary(HomeActivity.this, diaryAdd)) {
                            //SP.set(HomeActivity.this, "YesterDayState", true);
                            if (myadapter.getItem(1) != null)
                                ((DiaryFragment) myadapter.getItem(1)).updataAdapter();
                        }
                    }
                }
            }).start();
        }

        PushManager.getInstance().initialize(this.getApplicationContext());//个推

//        String str = Settings.System.getString(getApplication().getContentResolver(),
//                Settings.System.NEXT_ALARM_FORMATTED);
//        Log.e("my_log", "==当前渠道名===>" + Utils.getChannelName(getApplicationContext()));
//        Log.e("my_log", "==闹钟===>" + str);

        PushAgent mPushAgent = PushAgent.getInstance(this);//友盟推送
        mPushAgent.enable();
        mPushAgent.onAppStart();

        boolean isPage = SP.getBoolean(this, "GuidePage", false);
        if (isPage) {
//            if (!SP.getBoolean(this, "Tip2016", false)) {
//                newTip(); // 显示指引页时就不显示 新内容提示
//            }
        } else {
            guidePage(); // 显示指引页
        }

        initEvents();//侧拉控件和监听
        setLeftWidth();//设置侧滑菜单宽度
        initView();//加载布局

        if (savedInstanceState != null) {
            boolean b = savedInstanceState.getBoolean("left");
            if (b) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        }
        //友盟更新
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);

        DB.getDataInterface(this).login(MyApplication.getPhoneStr(this),
                SP.getString(this, "phoneNumber", ""),
                SP.getString(this, "password", ""),
                SP.getString(this, "login_type", ""),
                SP.getString(this, "token", ""), new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        try {
                            org.json.JSONObject object = jsonArray.getJSONObject(0);
                            String resp = object.getString("resp");
                            String msg = object.getString("msg");
                            if (resp.equals("000230") || resp.equals("000240")) { // 设备被别人绑定，或者账号已绑定其它设备
                                SP.set(getApplicationContext(), "isLogin", false);
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("left", mDrawerLayout.isDrawerOpen(Gravity.LEFT));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean b = savedInstanceState.getBoolean("left");
        if (b) {
            initEvents();//侧拉控件和监听
            initView();//加载布局
//            mDrawerLayout.openDrawer(Gravity.LEFT);
        } else {
            initView();
        }
    }

    int state = 0;

    //新手引导页，第一次进入显示
    private void guidePage() {
        final ImageView page = (ImageView) findViewById(R.id.page_view);
        page.setVisibility(View.VISIBLE);
        page.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
//                Bitmap bitmap;
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        state++;
                        if (state == 1) {
//                            bitmap = Utils.readBitmap(HomeActivity.this, R.mipmap.page_3);
//                            page.setImageBitmap(bitmap);
                            page.setImageResource(R.mipmap.page_3);
                        } else if (state == 2) {
//                            bitmap = Utils.readBitmap(HomeActivity.this, R.mipmap.page_2);
//                            page.setImageBitmap(bitmap);
                            page.setImageResource(R.mipmap.page_2);
                        } else {
                            page.setVisibility(View.GONE);
                            SP.set(HomeActivity.this, "GuidePage", true);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void newTip() {
        final ImageView page = (ImageView) findViewById(R.id.page_tip);
        page.setVisibility(View.VISIBLE);
        page.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        page.setVisibility(View.GONE);
                        SP.set(HomeActivity.this, "Tip2016", true);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        ServiceUtils.keepMainService(this); // 确保 MainService 在运行

        usageNoPermission();

//        homeSetProtect();

    }

    /**
     * 没有权限
     */
    private void usageNoPermission() {
        if (Build.VERSION.SDK_INT >= 21 && !SP.getBoolean(this, SP.USAGE_GET, false)) { // 如果抓取不到数据

            boolean usagePermission = PermissionTools.usageStats(this);

            TextView homeNoPermissionTextView = (TextView) findViewById(R.id.home_no_permission);
            if (usagePermission) {
                sendBroadcast(new Intent(MyReceiver.REFRESH_PACKAGE_GETTER));
                homeNoPermissionTextView.setVisibility(View.GONE);
            } else {
                homeNoPermissionTextView.setVisibility(View.VISIBLE);
                if (homeNoPermissionTextView.getAnimation() == null) {

                    homeNoPermissionTextView.setOnClickListener(new View.OnClickListener() {

                        @TargetApi(21)
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(HomeActivity.this, "跳转到“有权查看使用情况的应用”设置时失败！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Animation animation = new TranslateAnimation(0F, 0F, homeNoPermissionTextView.getMeasuredHeight()
                            , 0F);
                    animation.setDuration(1200);
                    animation.setInterpolator(new LinearInterpolator());
                    homeNoPermissionTextView.setAnimation(animation);
                }
                homeNoPermissionTextView.startAnimation(homeNoPermissionTextView.getAnimation());
            }
        }
    }

    /**
     * 试试开机自启吧
     */
    private void homeSetProtect() {
        boolean setProtect = SP.getBoolean(this, "SET_PROTECT", false);
        if (!setProtect) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("试试设置开机自启，数据更准确哦！");
            builder.setPositiveButton("马上设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Intent intent = new Intent(HomeActivity.this, WebHtmlActivity.class);
                        intent.putExtra("program", true);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "跳转时失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("我知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("点击了我知道了");
                    dialog.dismiss();
                }
            });
            builder.create().show();
            SP.set(this, "SET_PROTECT", true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initView() {
        homeViewPager = (HomeViewPager) findViewById(R.id.homePage);
        myadapter = new MyAdapter(getSupportFragmentManager());
        myadapter.setHomeFrgs(4);
        homeViewPager.setAdapter(myadapter);
        homeViewPager.setOffscreenPageLimit(4);//缓存页面数
        homeViewPager.setScanScroll(false);
        homeViewPager.setCurrentItem(0);

        rb_home = (ImageView) findViewById(R.id.rb_home);
        rb_diary = (ImageView) findViewById(R.id.rb_diary);
        rb_list = (ImageView) findViewById(R.id.rb_list);
        rb_task = (ImageView) findViewById(R.id.rb_task);
        rb_home.setOnClickListener(this);
        rb_diary.setOnClickListener(this);
        rb_list.setOnClickListener(this);
        rb_task.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        HashMap<String, String> map;
        switch (v.getId()) {
            case R.id.rb_home:
                homeViewPager.setCurrentItem(0);
                rb_home.setImageResource(R.mipmap.rb_home);
                rb_diary.setImageResource(R.mipmap.rb_diary_1);
                rb_list.setImageResource(R.mipmap.rb_list_1);
                rb_task.setImageResource(R.mipmap.rb_task_1);
                break;
            case R.id.rb_diary:
                homeViewPager.setCurrentItem(1);
                rb_home.setImageResource(R.mipmap.rb_home_1);
                rb_diary.setImageResource(R.mipmap.rb_diary);
                rb_list.setImageResource(R.mipmap.rb_list_1);
                rb_task.setImageResource(R.mipmap.rb_task_1);

                map = new HashMap<>();
                map.put("page", "日记本");
                MobclickAgent.onEventValue(this, "zhu_ye_dian_ji", map, 1);
                break;
            case R.id.rb_list:
                homeViewPager.setCurrentItem(2);
                rb_home.setImageResource(R.mipmap.rb_home_1);
                rb_diary.setImageResource(R.mipmap.rb_diary_1);
                rb_list.setImageResource(R.mipmap.rb_list);
                rb_task.setImageResource(R.mipmap.rb_task_1);

                map = new HashMap<>();
                map.put("page", "排行榜");
                MobclickAgent.onEventValue(this, "zhu_ye_dian_ji", map, 1);
                break;
            case R.id.rb_task:
                homeViewPager.setCurrentItem(3);
                rb_home.setImageResource(R.mipmap.rb_home_1);
                rb_diary.setImageResource(R.mipmap.rb_diary_1);
                rb_list.setImageResource(R.mipmap.rb_list_1);
                rb_task.setImageResource(R.mipmap.rb_task);

                ((TaskFragment) myadapter.getItem(3)).init();
//                ((TaskFragment) myadapter.getItem(3)).checkStageTask();

                map = new HashMap<>();
                map.put("page", "任务清单");
                MobclickAgent.onEventValue(this, "zhu_ye_dian_ji", map, 1);
                break;
        }
    }

    public void getLeft() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    // 设置左滑菜单的宽度
    private void setLeftWidth() {
        ViewGroup left_user_info = (ViewGroup) findViewById(R.id.left_user_info);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = dm.widthPixels * 4 / 5;
        params.gravity = Gravity.LEFT;
        left_user_info.setLayoutParams(params);
    }

    // 侧滑监听
    private void initEvents() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawerLayout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = mDrawerLayout.getChildAt(0);
                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;
                if (drawerView.getTag().equals("LEFT")) {
                    float leftScale = 1 - 0.3f * scale;
                    ViewHelper.setScaleX(drawerView, leftScale);// x轴缩放比例
                    ViewHelper.setScaleY(drawerView, leftScale);// y轴缩放比例
                    // ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
                    ViewHelper.setTranslationX(mContent,
                            drawerView.getMeasuredWidth() * (1 - scale));
                    ViewHelper.setPivotX(mContent, 0);
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                    mDrawerLayout.setScrimColor(Color.TRANSPARENT);
                } else {
                    ViewHelper.setTranslationX(mContent, -drawerView.getMeasuredWidth() * slideOffset);
                    ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawers();
            } else {
                if (homeViewPager.getCurrentItem() == 1) {
                    boolean b = ((DiaryFragment) myadapter.getItem(1)).onKeyDown();
                    if (b) {
                        return true;
                    } else {
                        ((MyApplication) getApplication()).actFinisih();
                    }
                } else if (homeViewPager.getCurrentItem() == 3) {
                    boolean t = ((TaskFragment) myadapter.getItem(3)).onKeyDown();
                    if (t) {
                        return true;
                    } else {
                        ((MyApplication) getApplication()).actFinisih();
                    }
                } else {
                    // activity中调用 moveTaskToBack (boolean nonRoot)方法即可将activity
                    // 退到后台，而不用finish()退出。
                    // 参数为false代表只有当前activity是task根，指应用启动的第一个activity时才有效;
                    // 如果为true则忽略这个限制，任何activity都可以有效。
                    // moveTaskToBack(true);
                    ((MyApplication) getApplication()).actFinisih();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("test", "HomeActivity onRequestPermissionsResult requestCode=>:" + requestCode + ",permissions=>:" + permissions + ",grantResults=>:" + grantResults);
    }
}
