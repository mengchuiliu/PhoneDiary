package com.rdcx.fragments.home;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.bean.AppDate;
import com.rdcx.fragments.HomeFragment;
import com.rdcx.fragments.MyHomeFragment;
import com.rdcx.myview.ChatView;
import com.rdcx.myview.LoadingView;
import com.rdcx.myview.RotateTextView;
import com.rdcx.randian.R;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.Call;
import com.rdcx.tools.DB;
import com.rdcx.tools.Download;
import com.rdcx.tools.Operation;
import com.rdcx.tools.SP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20 0020.
 * <p/>
 * 应用排行榜
 */
public class AppRankFragment extends HomeFragment {

    private View view;

    private RotateTextView rankLable;

    private boolean timeAxis;

    private int curMinutes = 0;

    private ArrayList<ChatView.ChatData> chatDatas;

    private HashMap<String, Drawable> iconMap;

    private List<ChatView.ChatList> appTimeBreakList;

    private String title, lableText;

    protected Thread tagThread = null;
    ImageView imageView;

    @Override
    protected View initView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        view = inflater.inflate(R.layout.home_3, null);
        title = arguments.getString("title");
        timeAxis = arguments.getBoolean("timeAixs");
        rankLable = (RotateTextView) view.findViewById(R.id.rank_lable);
        imageView = (ImageView) view.findViewById(R.id.rank_iv);
        rankLable.setDegrees(345);
        return view;
    }

    //围绕图片中心点旋转
    private void testRotate(MyHomeFragment myHomeFragment, ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) myHomeFragment.getResources().getDrawable(R.mipmap.app_tag)).getBitmap();
        Matrix matrix = new Matrix();
        // 设置旋转角度
        matrix.setRotate(345);
        // 重新绘制Bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void refreshDataInThread(final Context context, final Handler handler) {

        Operation.syncOperation(context); // 同步应用使用数据
        Download.downloadOperation(context, dbTime.get("start"), dbTime.get("end"));

        if (timeAxis && tagThread == null) {
            tagThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new NetDataGetter(context).getRankLable(SP.getString(context, SP.USER_ID, ""), new NetManager.DataArray() {
                        @Override
                        public void getServiceData(JSONArray jsonArray) {
                            Log.e("my_log", "===lable==>:" + jsonArray);
                            try {
                                JSONObject jo = jsonArray.getJSONObject(0);
                                if ("000000".equals(jo.getString("resp"))) {
                                    if (jo.optJSONObject("model") != null)
                                        lableText = jo.getJSONObject("model").getString("lableText");
                                    Message message = new Message();
                                    message.what = 18;
                                    message.arg1 = position;
                                    handler.sendMessage(message);
                                }
                            } catch (JSONException e) {
                                Log.e("my_log", "lable Exception=>:" + e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e("my_log", "lable volleyError=>:" + volleyError);
                        }
                    });
                }
            });
            tagThread.start();
        }


        chatDatas = new ArrayList<>();
        iconMap = new HashMap<>();
        List<Operation> operationList = Operation.selectOperationGroup(context, dbTime.get("start"), dbTime.get("end"));
        ArrayList<AppDate> appDates = new ArrayList<>();
        PackageManager pm = context.getApplicationContext().getPackageManager();
        if (operationList.size() > 0) {
            long max = operationList.get(0).duration;
            for (Operation operation : operationList) {
                try {
                    AppDate appDate = new AppDate();
                    ApplicationInfo info = pm.getApplicationInfo((operation.packageName), 0);
                    appDate.setIcon(info.loadIcon(pm));
                    appDate.setTime((int) Math.ceil(operation.duration / 1000F));
                    appDate.setPercent((int) Math.ceil(operation.duration * 100F / max));
                    appDate.setCount((int) operation.time);
                    appDates.add(appDate);

                    iconMap.put(operation.packageName, appDate.getIcon());

                    ChatView.ChatData chatData = new ChatView.ChatData(pm.getApplicationLabel(info).toString(), (int)
                            operation.duration, true);
                    chatDatas.add(chatData);

                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("test", "包名" + operation.packageName + "的应用不存在。");
                }
            }
        }
        this.appDateList = appDates;

        if (chatDatas.size() > 5) {
            int total = 0;
            for (int i = chatDatas.size() - 1; i >= 4; i--) {
                total += chatDatas.get(i).value;
                chatDatas.remove(i);
            }
            chatDatas.add(new ChatView.ChatData("其它", total, true));
        } else if (chatDatas.size() == 0) {
            chatDatas.add(new ChatView.ChatData("无", 100, true));
        }

        if (timeAxis) {

            Call.syncPhone(context, System.currentTimeMillis()); // 同步通话数据
            Download.downloadPhone(context, dbTime.get("start"), dbTime.get("end"));

            if (Build.VERSION.SDK_INT >= 21) {
                iconMap.put("call", context.getApplicationContext().getResources().getDrawable(R.mipmap.tel, null));
            } else {
                iconMap.put("call", context.getApplicationContext().getResources().getDrawable(R.mipmap.tel));
            }
            timeAxisData(context);
        } else {
            appTimeBreakList = new ArrayList<>();
            appTimeBreakList.add(new ChatView.ChatList(chatDatas));
        }

    }

    private void timeAxisData(Context context) {
        List<Operation> operations = Operation.selectOperation(context, dbTime.get("start"), dbTime.get("end"), null);
        List<Call> callList = Call.selectCall(context, dbTime.get("start"), dbTime.get("end"));
        for (Call call : callList) {
            Operation operation = new Operation();
            operation.packageName = "call";
            operation.time = call.time;
            operation.duration = call.duration * 1000;
            operations.add(operation);
        }
        Collections.sort(chatDatas, new Comparator<ChatView.ChatData>() {
            @Override
            public int compare(ChatView.ChatData lhs, ChatView.ChatData rhs) {
                return (int) (rhs.time - lhs.time);
            }
        });

        long total = 0;
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        // 当天

        chatDatas.clear();
        appTimeBreakList = new ArrayList<>();
        List<ChatView.ChatData> list;

        if (type == 0) { // 日维度
            if (c.getTimeInMillis() >= dbTime.get("start") && c.getTimeInMillis() < dbTime.get("end")) {
                curMinutes = hour * 60 + minute;
            } else {
                curMinutes = 24 * 60;
            }
            for (int i = 0; i < 4; i++) {
                appTimeBreakList.add(new ChatView.ChatList(new ArrayList<ChatView.ChatData>()));
            }
            for (Operation operation : operations) {
                c.setTimeInMillis(operation.time);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                chatDatas.add(new ChatView.ChatData(operation.packageName, (int) ((hour * 60 + minute) * 360F / (24 * 60)), (int)
                        operation.duration, false));

                if (!Operation.SCREEN_ON.equals(operation.packageName)) {
                    total += operation.duration / 1000;
                    list = appTimeBreakList.get(hour / 6).chatDatas;
                    boolean find = false;
                    for (ChatView.ChatData chatData : list) {
                        if (chatData.text != null && chatData.text.equals(operation.packageName)) {
                            chatData.value += operation.duration;
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        list.add(new ChatView.ChatData(operation.packageName, (int) operation.duration, false));
                    }
                }
            }
        } else if (type == 2) { // 周维度
            if (c.getTimeInMillis() >= dbTime.get("start") && c.getTimeInMillis() < dbTime.get("end")) {
                curMinutes = (dayOfWeek == 1 ? 6 : dayOfWeek - 2) * 24 * 60 + hour * 60 + minute;
            } else {
                curMinutes = 7 * 24 * 60;
            }
            for (int i = 0; i < 7; i++) {
                appTimeBreakList.add(new ChatView.ChatList(new ArrayList<ChatView.ChatData>()));
            }
            for (Operation operation : operations) {
                c.setTimeInMillis(operation.time);
                dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                dayOfWeek = (dayOfWeek == 1 ? 6 : dayOfWeek - 2);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                chatDatas.add(new ChatView.ChatData(operation.packageName, (int) ((dayOfWeek * 24 * 60 + hour * 60 + minute) * 360F / (7 * 24 * 60)), (int)
                        operation.duration, false));

                if (!Operation.SCREEN_ON.equals(operation.packageName)) {
                    total += operation.duration / 1000;
                    list = appTimeBreakList.get(dayOfWeek).chatDatas;
                    boolean find = false;
                    for (ChatView.ChatData chatData : list) {
                        if (chatData.text != null && chatData.text.equals(operation.packageName)) {
                            chatData.value += operation.duration;
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        list.add(new ChatView.ChatData(operation.packageName, (int) operation.duration, false));
                    }
                }
            }
        }

        for (int i = 0; i < appTimeBreakList.size(); i++) {
            list = appTimeBreakList.get(i).chatDatas;
            Collections.sort(list, new Comparator<ChatView.ChatData>() {
                @Override
                public int compare(ChatView.ChatData lhs, ChatView.ChatData rhs) {
                    return rhs.value - lhs.value;
                }
            });
        }

        ruleText = DB.selectRuleText(context, ruleType, dimensionType, total / 60, "报告报告，[nick]已伺候小主[count]分钟。");
        ruleText = ruleText.replaceAll("\\d+分钟", DB.getTimeBySecond(total));
    }

    @Override
    public void startLoading() {
        ChatView chatView = (ChatView) view.findViewById(R.id.home_chat_view);
        chatView.setVisibility(View.GONE);

        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.startAnimation();
    }

    public void stopLoading() {
        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.GONE);
        loadingView.stopAnimation();

        ChatView chatView = (ChatView) view.findViewById(R.id.home_chat_view);
        chatView.setVisibility(View.VISIBLE);
    }

    @Override
    public void invalidate(MyHomeFragment myHomeFragment) {

        stopLoading();

        setTitleText(title);

        ChatView chatView = (ChatView) view.findViewById(R.id.home_chat_view);
        if (timeAxis) {
            chatView.setCurMinutes(curMinutes);
            chatView.drawTimeAxis(chatDatas, iconMap, appTimeBreakList);
            myHomeFragment.showText(ruleText, null);
        } else {
            chatView.drawPieGraph(appTimeBreakList);
            myHomeFragment.showText(ruleText, appDateList);
        }

        newSet = true;
    }

//    @Override
//    protected void showChampionStr(TextView champion, TextView oneself, String championStr) {
//        com.alibaba.fastjson.JSONObject championJson = com.alibaba.fastjson.JSONObject.parseObject(championStr);
//        if (timeAxis) {
//            champion.setText(String.format("昨日全国第一ID：%s\n%s", championJson.getString("champion"), DB.getTimeBySecond(championJson.getLong("championCount"))));
//            oneself.setText(String.format("昨日全国排名：%s\n%s", championJson.getString("count"), DB.getTimeBySecond(championJson.getLong("number"))));
//        } else {
//            champion.setText(String.format("昨日全国第一ID：%s\n使用应用 %s 个", championJson.getString("champion"), championJson.getString("championCount")));
//            oneself.setText(String.format("昨日全国排名：%s\n使用应用 %s 个", championJson.getString("count"), championJson.getString("number")));
//        }
//    }

    @Override
    public void setLable(MyHomeFragment myHomeFragment) {
        super.setLable(myHomeFragment);
        try {
            if (timeAxis) {
                rankLable.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                testRotate(myHomeFragment, imageView);
                if (lableText == null || lableText.equals("")) {
                    rankLable.setText("空空如也");
                } else {
                    rankLable.setText(lableText);
                }
            }
        } catch (Exception e) {
            Log.d("test", "AppRankFragment setLabel cause an Exception=>:", e);
        }
    }
}
