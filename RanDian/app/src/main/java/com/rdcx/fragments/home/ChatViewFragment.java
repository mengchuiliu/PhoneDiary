package com.rdcx.fragments.home;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rdcx.fragments.HomeFragment;
import com.rdcx.fragments.MyHomeFragment;
import com.rdcx.myview.ChatView;
import com.rdcx.myview.LoadingView;
import com.rdcx.randian.R;
import com.rdcx.tools.App;
import com.rdcx.tools.Call;
import com.rdcx.tools.DB;
import com.rdcx.tools.Download;
import com.rdcx.tools.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20 0020.
 * <p>
 * 绘图的 Fragment
 */
public class ChatViewFragment extends HomeFragment {

    private String xUnit;
    List<ChatView.ChatList> appTimeBreakList;

    private String typeNames;
    private String title;
    private String defaultText;

    @Override
    protected View initView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        typeNames = arguments.getString("typeNames");
        title = arguments.getString("title");
        defaultText = arguments.getString("defaultText");
        return inflater.inflate(R.layout.home_3, null);
    }

    @Override
    protected void refreshDataInThread(Context context, Handler handler) {

        appTimeBreakList = new ArrayList<>();

        List<Operation> operationList;
        long total = 0;
        if (typeNames != null) {

            Operation.syncOperation(context); // 同步应用使用数据
            Download.downloadOperation(context, dbTime.get("start"), dbTime.get("end"));
            App.syncApp(context, false);

            operationList = Operation.selectOperation(context, dbTime.get("start"), dbTime.get("end"), null, typeNames);

            PackageManager pm = context.getPackageManager();
            HashMap<String, ArrayList<Operation>> map = new HashMap<>();
            ArrayList<Operation> tempList;
            for (Operation operation : operationList) {
                tempList = map.get(operation.packageName);
                if (tempList == null) {
                    tempList = new ArrayList<>();
                    map.put(operation.packageName, tempList);
                }
                tempList.add(operation);
                total += operation.duration / 1000L;
            }
            Iterator<String> keyIt = map.keySet().iterator();
            while (keyIt.hasNext()) {
                String key = keyIt.next();
                String applicationLabel = null;
                try {
                    ApplicationInfo info = pm.getApplicationInfo((key), 0);
                    applicationLabel = pm.getApplicationLabel(info).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("test", "包名" + key + "的应用不存在。");
                }
                int value = 0;
                List<ChatView.ChatData> chatDatas = getChatDatas(map.get(key));
                for (ChatView.ChatData chatData : chatDatas) {
                    value += chatData.value;
                }
                appTimeBreakList.add(new ChatView.ChatList(applicationLabel, null, value, chatDatas));
            }
            if (appTimeBreakList.size() < 1) {
                appTimeBreakList.add(new ChatView.ChatList(null, null, 0, getChatDatas(new ArrayList<Operation>())));
            }
            Collections.sort(appTimeBreakList, new Comparator<ChatView.ChatList>() {
                @Override
                public int compare(ChatView.ChatList lhs, ChatView.ChatList rhs) {
                    return rhs.value - lhs.value;
                }
            });
        } else {

            Call.syncPhone(context, System.currentTimeMillis()); // 同步通话数据
            Download.downloadPhone(context, dbTime.get("start"), dbTime.get("end"));

            List<Call> callList = Call.selectCall(context, dbTime.get("start"), dbTime.get("end"));
            operationList = new ArrayList<>();
            for (Call call : callList) {
                Operation operation = new Operation();
                operation.time = call.time;
                operation.duration = call.duration * 1000L;
                operationList.add(operation);
                total += call.duration;
            }
            appTimeBreakList.add(new ChatView.ChatList(getChatDatas(operationList)));
        }

        ruleText = DB.selectRuleText(context, ruleType, dimensionType, (int) Math.ceil(total / 60.0), defaultText);
        ruleText = ruleText.replaceAll("\\d+分钟", DB.getTimeBySecond(total));

    }

    private List<ChatView.ChatData> getChatDatas(List<Operation> operationList) {
        List<ChatView.ChatData> dataArrayList = new ArrayList<>();
        long startTime = dbTime.get("start");
        long endTime = 0;
        long duration = 0;
        switch (type) {
            case 0: // 日维度
                xUnit = "时";
                endTime = startTime + 1000 * 60 * 60;
                for (int i = 0, j = 0; i < 24; ) {
                    Operation operation;
                    if (j < operationList.size() && (operation = operationList.get(j)) != null && operation.time >= startTime && operation.time < endTime) {
                        duration += operation.duration;
                        j++;
                    } else {
                        dataArrayList.add(new ChatView.ChatData(i % 8 == 0 ? String.valueOf(i) : null, (int) Math.ceil(duration / 1000F), i % 4 == 0));
                        startTime = endTime;
                        endTime = startTime + 1000 * 60 * 60;
                        duration = 0;
                        i++;
                    }
                }
                break;
            case 1: // 月维度
                xUnit = "天";
                endTime = startTime + 1000 * 60 * 60 * 24;
                int days = (int) ((dbTime.get("end") - startTime) / (1000 * 60 * 60 * 24));
                for (int i = 0, j = 0; i < days; ) {
                    Operation operation;
                    if (j < operationList.size() && (operation = operationList.get(j)) != null && operation.time >= startTime && operation.time < endTime) {
                        duration += operation.duration;
                        j++;
                    } else {
                        dataArrayList.add(new ChatView.ChatData(i % 8 == 0 ? String.valueOf(i + 1) : null, (int) Math.ceil(duration / 1000F), i % 4 == 0));
                        startTime = endTime;
                        endTime = startTime + 1000 * 60 * 60 * 24;
                        duration = 0;
                        i++;
                    }
                }
                break;
            case 2: // 周维度
                xUnit = "天";
                endTime = startTime + 1000 * 60 * 60 * 24;
                for (int i = 0, j = 0; i < 7; ) {
                    Operation operation;
                    if (j < operationList.size() && (operation = operationList.get(j)) != null && operation.time >= startTime && operation.time < endTime) {
                        duration += operation.duration;
                        j++;
                    } else {
                        dataArrayList.add(new ChatView.ChatData(String.valueOf(i + 1), (int) Math.ceil(duration / 1000F), true));
                        startTime = endTime;
                        endTime = startTime + 1000 * 60 * 60 * 24;
                        duration = 0;
                        i++;
                    }
                }
                break;
            default:
                break;
        }
        return dataArrayList;
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

        myHomeFragment.showText(ruleText, appDateList);

        ChatView chatView = (ChatView) view.findViewById(R.id.home_chat_view);
        chatView.setxUnit(xUnit);
        chatView.setyUnit("秒");
        chatView.drawAreaGraph(appTimeBreakList);

    }

//    @Override
//    protected void showChampionStr(TextView champion, TextView oneself, String championStr) {
//        com.alibaba.fastjson.JSONObject championJson = com.alibaba.fastjson.JSONObject.parseObject(championStr);
//        champion.setText(String.format("昨日全国第一ID：%s\n%s", championJson.getString("champion"), DB.getTimeBySecond(championJson.getLong("championCount"))));
//        oneself.setText(String.format("昨日全国排名：%s\n%s", championJson.getString("count"), DB.getTimeBySecond(championJson.getLong("number"))));
//    }

}
