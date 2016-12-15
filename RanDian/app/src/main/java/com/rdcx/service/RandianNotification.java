package com.rdcx.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rdcx.randian.HomeActivity;
import com.rdcx.randian.R;
import com.rdcx.tools.DB;
import com.rdcx.tools.Operation;
import com.rdcx.tools.SP;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/3/16 0016.
 * <p/>
 * 在满足一定的规则下给用户推送提示信息
 */
public class RandianNotification {

    private static final int NOTIFICATION_FLAG = 1;

    // 推送规则发送消息时的 group 前缀
    private static final String PUSH_RULE_PREFIX = "PUSH_RULE_";

    /**
     * 发送默认通知
     *
     * @param context Context
     * @param ticker  在状态栏显示的内容
     * @param title   通知的标题
     * @param content 通知的内容
     */
    public static void sendNotification(Context context, String ticker, String title, String content, Integer group) {

        ticker = ticker == null ? "" : ticker;
        title = title == null ? "" : title;
        content = content == null ? "" : content;
        group = group == null ? 1 : group;

        NotificationManager nm = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(context, HomeActivity.class), 0);
        if (Build.VERSION.SDK_INT >= 16) {
            Notification n = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.randian)
                    .setTicker(ticker)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(pendingIntent)
                    .setNumber(group)
                    .build();
            n.flags |= Notification.FLAG_AUTO_CANCEL;
            nm.notify(NOTIFICATION_FLAG, n);
        } else if (Build.VERSION.SDK_INT >= 11) {
            Notification n = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.randian)
                    .setTicker(ticker)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(pendingIntent)
                    .setNumber(group)
                    .getNotification();
            n.flags |= Notification.FLAG_AUTO_CANCEL;
            nm.notify(NOTIFICATION_FLAG, n);
        } else {
            Notification n = new Notification(R.mipmap.randian, ticker, System.currentTimeMillis());
            try {
                Method method = n.getClass().getDeclaredMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                method.setAccessible(true);
                method.invoke(n, context, title, content, pendingIntent);
                n.number = group;
                n.flags |= Notification.FLAG_AUTO_CANCEL;
                nm.notify(NOTIFICATION_FLAG, n);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 循环检查当前是否符合发送通知的规则，如果符合发送通知的规则，则向用户发送通知
     *
     * @param context Context
     * @param curTime 当前时间
     */
    public static void checkNotificationRules(Context context, long curTime) {

        HashMap<String, Long> dbMap = null;

        String pushRule = SP.getString(context, SP.PUSH_RULE, "[]");
        JSONArray ja = JSON.parseArray(pushRule);

        for (int i = 0; i < ja.size(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            Integer group = jo.getInteger("type");
            String content = jo.getString("content");
            content = content == null ? "" : content;
            switch (jo.getInteger("type")) {
                case 1:
                    if (dbMap == null) {
                        dbMap = calculateTotalAndMax(context, curTime);
                    }
                    if (dbMap.get("total") >= jo.getLong("number1") * 1000) {
                    if (SP.oneDayOnceStamp(context, PUSH_RULE_PREFIX + (group == null ? "" : String.valueOf(group)), curTime)) {
                        content = content.replaceAll("\\[count\\]", DB.getTimeBySecond(dbMap.get("total") / 1000));
                        sendNotification(context, "", jo.getString("title"), content, group);
                    }
                }
                    break;
                case 2:
                    if (dbMap == null) {
                        dbMap = calculateTotalAndMax(context, curTime);
                    }
                    if (dbMap.get("max") >= jo.getLong("number1") * 1000) {
                        if (SP.oneDayOnceStamp(context, PUSH_RULE_PREFIX + (group == null ? "" : String.valueOf(group)), curTime)) {
                            content = content.replaceAll("\\[count\\]", DB.getTimeBySecond(dbMap.get("max") / 1000));
                            sendNotification(context, "", jo.getString("title"), content, group);
                        }
                    }
                    break;
                case 3:
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(curTime);
                    if (c.get(Calendar.HOUR_OF_DAY) == jo.getInteger("number1")) {
                        if (SP.oneDayOnceStamp(context, PUSH_RULE_PREFIX + (group == null ? "" : String.valueOf(group)), curTime)) {
                            content = content.replaceAll("\\[count\\]", String.valueOf(c.get(Calendar.HOUR_OF_DAY)));
                            sendNotification(context, "", jo.getString("title"), content, group);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 查找数据库中所有的屏幕点亮的数据，即 packageName = "ScreenOn" 的所有当天的 Operation 表中的数据
     *
     * @param context Context
     * @param curTime 当前时间
     * @return total:今天使用的总时长，max:今天连续使用的最大时长
     */
    private static HashMap<String, Long> calculateTotalAndMax(Context context, long curTime) {
        HashMap<String, Long> dbTime = DB.getDBTime(DB.TYPE_TODAY, curTime);
        List<Operation> screenOnList = Operation.selectOperation(context, dbTime.get("start"), dbTime.get("end"), Operation.SCREEN_ON);
        long total = 0;
        long max = 0;
        long temp = 0;
        Operation lastOperation = null;
        for (Operation operation : screenOnList) {
            total += operation.duration;

            if (lastOperation == null) {
                temp = operation.duration;
            } else if (lastOperation.time + lastOperation.duration >= operation.time) {
                temp += operation.duration;
            } else {
                temp = operation.duration;
            }
            if (max < temp) {
                max = temp;
            }
            lastOperation = operation;
        }
        dbTime.put("total", total);
        dbTime.put("max", max);
        return dbTime;
    }

}
