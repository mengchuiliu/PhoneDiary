package com.rdcx.fragments.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdcx.fragments.HomeFragment;
import com.rdcx.fragments.MyHomeFragment;
import com.rdcx.myview.LoadingView;
import com.rdcx.randian.R;
import com.rdcx.tools.Call;
import com.rdcx.tools.DB;
import com.rdcx.tools.Download;

import java.util.List;

/**
 * Created by Administrator on 2015/12/20 0020.
 * <p/>
 * 联系人排行
 */
public class ContactRankFragment extends HomeFragment {

    TextView name1, phone_count1, time1, name2, phone_count2, time2, name3, phone_count3, time3;

    private List<Call> callList;

    private Call emptyCall;

    @Override
    protected View initView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        view = inflater.inflate(R.layout.home_1, null);
        return view;
    }

    @Override
    protected void refreshDataInThread(Context context, Handler handler) {

        Call.syncPhone(context, System.currentTimeMillis()); // 同步通话记录
        Download.downloadPhone(context, dbTime.get("start"), dbTime.get("end"));

        callList = Call.selectCallGroup(context, dbTime.get("start"), dbTime.get("end"));
        long count = 0;
        long total = 0;
        for (Call call : callList) {
            count += call.time;
            total += call.duration;
        }
        ruleText = DB.selectRuleText(context, ruleType, dimensionType, count, "[nick]通话[count]次，共联系了[number]个人。 ");
        ruleText = ruleText.replaceAll("\\[number\\]", String.valueOf(callList.size())).replaceAll("\\[total\\]分钟", DB.getTimeBySecond(total));

        emptyCall = new Call();
        emptyCall.name = "无";
        emptyCall.time = 0;
        emptyCall.duration = 0;

    }

    private String getName(String name, String number) {
        return name != null && name.length() > 0 ? name : number;
    }

    @Override
    public void startLoading() {
        LinearLayout contactLL = (LinearLayout) view.findViewById(R.id.ll_contacts);
        if (contactLL != null) {
            contactLL.setVisibility(View.GONE);
        }

        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.startAnimation();
    }

    public void stopLoading() {
        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.GONE);
        loadingView.stopAnimation();

        LinearLayout contactLL = (LinearLayout) view.findViewById(R.id.ll_contacts);
        if (contactLL != null) {
            contactLL.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void invalidate(MyHomeFragment myHomeFragment) {

        stopLoading();

        name1 = (TextView) view.findViewById(R.id.name_1);
        name2 = (TextView) view.findViewById(R.id.name_2);
        name3 = (TextView) view.findViewById(R.id.name_3);
        phone_count1 = (TextView) view.findViewById(R.id.phone_count1);
        phone_count2 = (TextView) view.findViewById(R.id.phone_count2);
        phone_count3 = (TextView) view.findViewById(R.id.phone_count3);
        time1 = (TextView) view.findViewById(R.id.time1);
        time2 = (TextView) view.findViewById(R.id.time2);
        time3 = (TextView) view.findViewById(R.id.time3);

        myHomeFragment.showText(ruleText, appDateList);

        for (int i = 0; i < 3; i++) {
            Call call = i < callList.size() ? callList.get(i) : emptyCall;
            String name = getName(call.name, call.number);
            switch (i) {
                case 0:
                    name1.setText(name);
                    phone_count1.setText(String.valueOf(call.time + "次"));
                    time1.setText(String.valueOf(call.duration + "秒"));
                    break;
                case 1:
                    name2.setText(name);
                    phone_count2.setText(String.valueOf(call.time + "次"));
                    time2.setText(String.valueOf(call.duration + "秒"));
                    break;
                case 2:
                    name3.setText(name);
                    phone_count3.setText(String.valueOf(call.time + "次"));
                    time3.setText(String.valueOf(call.duration + "秒"));
                    break;
            }
        }

    }

//    protected void showChampionStr(TextView champion, TextView oneself, String championStr) {
//        com.alibaba.fastjson.JSONObject championJson = com.alibaba.fastjson.JSONObject.parseObject(championStr);
//        champion.setText(String.format("昨日全国第一ID：%s\n通话 %s 次", championJson.getString("champion"), championJson.getString("championCount")));
//        oneself.setText(String.format("昨日全国排名：%s\n通话 %s 次", championJson.getString("count"), championJson.getString("number")));
//    }


}
