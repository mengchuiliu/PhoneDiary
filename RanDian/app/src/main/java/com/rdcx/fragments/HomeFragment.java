package com.rdcx.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.bean.AppDate;
import com.rdcx.randian.R;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.SP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/11/25 0025.
 *
 * @author mengchuiliu
 */
public class HomeFragment extends Fragment {

    protected View view;
    protected boolean newInit = false;

    protected int position = 0;
    protected int type;
    protected long currentTime;
    protected int ruleType; // 文案类型
    protected int dimensionType; // 文案的维度类型：1:日,2:周,3:月
    protected String championType; // 个人排行的维度类型
    //    protected String championStr = null; // 冠军数据和我的排名数据
    protected HashMap<String, Long> dbTime;
    protected String ruleText = null;
    protected ArrayList<AppDate> appDateList = null;

    protected boolean newSet = false;
    protected boolean loaded = false;
    protected Thread thread = null;
//    protected Thread championThread = null;

    /**
     * 通过日、周、月的维度不同生成不同的起始时间和结束时间，以及不同的文案类型
     *
     * @param position    位置
     * @param type        日、周、月的类型
     * @param currentTime 时间点
     */
    public void set(int position, int type, long currentTime) {

        this.position = position;
        this.type = type;
        this.currentTime = currentTime;

        Bundle arguments = getArguments();
        arguments.putInt("position", position);
        arguments.putInt("type", type);
        arguments.putLong("currentTime", currentTime);

        int dbTimeType;
        switch (type) {
            case 1: // 月维度
                dbTimeType = DB.TYPE_THIS_MONTH;
                dimensionType = 3;
                break;
            case 2: // 周维度
                dbTimeType = DB.TYPE_THIS_WEEK;
                dimensionType = 2;
                break;
            case 0: // 日维度
            default:
                dbTimeType = DB.TYPE_TODAY;
                dimensionType = 1;
                break;
        }
        dbTime = DB.getDBTime(dbTimeType, currentTime);

        // 新设置了参数
        newSet = true;
        loaded = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        if (newInit) {
            newInit = false;
            view = initView(inflater, savedInstanceState, arguments);
            return view;
        }

        if (view == null) {
            ruleType = arguments.getInt("ruleType");
            championType = arguments.getString("championType");
            set(arguments.getInt("position"), arguments.getInt("type"), arguments.getLong("currentTime"));
            view = initView(inflater, savedInstanceState, arguments);
        }

        return view;
    }

    /**
     * 初始化 View
     *
     * @param inflater LayoutInflater
     * @return view
     */
    protected View initView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        return inflater.inflate(R.layout.home_3, null);
    }

    /**
     * 在线程中刷新数据
     *
     * @param handler Handler
     */
    public void refreshData(final Context context, final Handler handler) {
        synchronized (this) {
            if (context == null || handler == null || view == null) {
                Log.e("test", "HomeFragment refreshData context=>:" + context + ",handler=>:" + handler + ",view=>:" + view);
                return;
            }
            // 加载 Loading 动画
            startLoading();

            if (newSet) {
                newSet = false;
                loaded = false;
                // 获取冠军的进程
//                championStr = SP.oneDayOnceString(context, "champion_" + championType, currentTime);
//                if (championStr == null || championStr.length() < 1) {
//                    championThread = new Thread() {
//
//                        @Override
//                        public void run() {
//                            new NetDataGetter(context).getMyListData(SP.getString(context, SP.USER_ID, ""), championType, new NetManager.DataArray() {
//                                @Override
//                                public void getServiceData(JSONArray jsonArray) {
//                                    Log.w("test", "HomeFragment refreshData getMyListData jsonArray=>:" + jsonArray);
//                                    try {
//                                        JSONObject championJson = jsonArray.getJSONObject(0);
//                                        if ("000000".equals(championJson.getString("resp")) && championJson.getString("champion") != null && championJson.getString("champion").length() > 1) {
//                                            championStr = championJson.toString();
//                                            SP.setOneDayOnceString(context, "champion_" + championType, championStr);
//
//                                            Message message = new Message();
//                                            message.what = 11;
//                                            message.arg1 = position;
//                                            handler.sendMessage(message);
//                                        } else {
//                                            Log.e("test", "HomeFragment refreshData getMyListData 从服务器获取的数据有误。");
//                                        }
//                                    } catch (JSONException e) {
//                                        Log.e("test", "HomeFragment refreshData getMyListData occurs an Exception=>:" + e);
//                                    }
//                                }
//                            }, new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError volleyError) {
//                                    Log.e("test", "HomeFragment refreshData getMyListData volleyError=>:" + volleyError);
//                                }
//                            });
//                            super.run();
//                        }
//
//                    };
//                    championThread.start();
//                } else {
//                    Message message = new Message();
//                    message.what = 11;
//                    message.arg1 = position;
//                    handler.sendMessage(message);
//                }

                // 获取数据的进程
                if (thread == null || !thread.isAlive()) {
                    thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Log.d("test", "HomeFragment->:" + HomeFragment.this + " refreshDataInThread");
                                refreshDataInThread(context, handler);

                                loaded = true;

                                Message message = new Message();
                                message.what = 10;
                                message.arg1 = position;
                                handler.sendMessage(message);

                            } catch (Exception e) {

                                Log.e("test", "HomeFragment.this->:" + HomeFragment.this + " Exception->" + e, e);

                                handler.sendEmptyMessage(12);

                            }

                        }
                    };
                    // 在主线程中增加清理缓存的操作，分别由其子类去实现
                    clearCache();
                    // 运行获取数据的进程
                    thread.start();
                }
            } else if (loaded) {
                Message message = new Message();
                message.what = 10;
                message.arg1 = position;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 清理缓存，在其子类予以实现
     */
    protected void clearCache() {

    }

    /**
     * 刷新数据的实际方法，在其子类予以实现
     *
     * @param handler Handler
     */
    protected void refreshDataInThread(Context context, Handler handler) {

    }

    /**
     * 加载数据的线程运行的时候，启动 startLoading 动画
     */
    public void startLoading() {

    }

    /**
     * 刷新 UI，在其子类中予以实现
     */
    public void invalidate(MyHomeFragment myHomeFragment) {

    }

//    protected void showChampionStr(TextView champion, TextView oneSelf, String championStr) {
//
//    }

    // 设置标题文字
    protected void setTitleText(String text) {
        if (view != null) {
            View textView = view.findViewById(R.id.home_web_name);
            if (textView != null && textView instanceof TextView) {
                ((TextView) textView).setText(text);
            }
        }
    }

//    public void showChampion() {
//        if (type == 1 || type == 2) { // 月、周维度不需要显示冠军
//            return;
//        }
//        if (championStr == null || championStr.length() < 1) { // 未获取到数据时不需要显示冠军
//            return;
//        }
//        View my_list = view.findViewById(R.id.my_list);
//        if (my_list != null) {
//            my_list.setVisibility(View.VISIBLE);
//            my_list.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_list_show, null);
//                    TextView champion = (TextView) view.findViewById(R.id.champion);
//                    TextView oneself = (TextView) view.findViewById(R.id.oneself);
//
//                    if (championStr == null) {
//                        champion.setText("昨日全国冠军：无");
//                        oneself.setText("昨日全国排名：无");
//                    } else {
//                        showChampionStr(champion, oneself, championStr);
//                    }
//
//                    if (dialog == null) {
//                        if (Build.VERSION.SDK_INT >= 11) {
//                            dialog = new AlertDialog.Builder(getActivity(), R.style.championStyle).create();
//                        } else {
//                            dialog = new AlertDialog.Builder(getActivity()).create();
//                        }
//                    }
//                    dialog.show();
//                    Window window = dialog.getWindow();
//                    DisplayMetrics dm = new DisplayMetrics();
//                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//                    WindowManager.LayoutParams lp = window.getAttributes();
//                    lp.height = dm.heightPixels * 2 / 3;
//                    window.setGravity(Gravity.CENTER);
//                    window.setContentView(view);
//                    dialog.setCancelable(true);
//
//                    view.findViewById(R.id.rl_list).setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            if (dialog != null && dialog.isShowing()) {
//                                dialog.dismiss();
//                            }
//                            return false;
//                        }
//                    });
//                }
//            });
//        }
//    }

    //设置标签
    public void setLable(MyHomeFragment myHomeFragment) {
    }
}
