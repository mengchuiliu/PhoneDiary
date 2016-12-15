package com.rdcx.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.randian.DiaryEditorActivity;
import com.rdcx.randian.FeedbackActivity;
import com.rdcx.randian.HomeActivity;
import com.rdcx.randian.UpdateUserInfo;
import com.rdcx.randian.VersionUpdate;
import com.rdcx.randian.WebHtmlActivity;
import com.rdcx.service.NetManager;
import com.rdcx.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/5/6 0006.
 * <p/>
 * 自动完成的任务在这里进行判断
 */
public class TaskTools {

    public static void checkAllTask(Context context) {
        int[] ids = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 35, 36, 37};
        for (int id : ids) {
            checkTask(context, id);
        }
    }

    public static String checkTask(Context context, int taskId) {

        HashMap<String, Long> dbTime = DB.getDBTime(DB.TYPE_TODAY);
        long startTime = dbTime.get("start");
        long endTime = dbTime.get("end");

        List<Call> callList;
        List<Operation> operationList;
        List<HashMap<String, Object>> locationInfoList;
        List<ImageInfo> imageInfoList;
        long total;
        boolean finish;
        switch (taskId) {
            case 1: // 带NOTE2装B，主页任一项目排行在全国前100位,由后台自动验证，前端不验证。
                return "";
            case 2: // 睡到自然醒，一天内0点到10点没有亮屏
                if (startTime + Constants.ONE_HOUR * 10 > System.currentTimeMillis()) {
                    return "今天还没到10点。";
                }
                operationList = Operation.selectOperation(context, startTime, startTime + Constants.ONE_HOUR * 10, null);
                if (operationList.size() > 0) {
                    return "今天0点到10点内有亮屏哦。";
                }
                locationInfoList = LocationInfo.selectLocation(context, startTime, startTime + Constants.ONE_HOUR * 10);
                if (locationInfoList.size() == 0) {
                    return "今天0点到10点内没有亮屏，但是手机日记可能被关闭了。";
                }
                break;
            case 3: // 坚持就是胜利，连续三天开启“手机日记”软件
                total = 0;
                for (long start = startTime - Constants.ONE_DAY * 2; start < endTime; start += Constants.ONE_DAY) {
                    locationInfoList = LocationInfo.selectLocation(context, start, endTime);
                    if (locationInfoList.size() > 0) {
                        total++;
                    }
                }
                if (total < 3) {
                    return "当前已连续开启“手机日记”软件" + total + "天。";
                }
                break;
            case 4: // 理财达人，一天内使用理财软件1小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null, "理财");
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR) {
                    return "今天已使用理财软件" + DB.getTimeBySecond(total / 1000) + "。";
                }
                break;
            case 5: // 阅读达人，一天内使用阅读,资讯软件3小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null, "阅读,资讯");
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR * 3) {
                    return "今天已使用阅读、资讯软件" + DB.getTimeBySecond(total / 1000) + "。";
                }
                break;
            case 6: // 话匣子，一天内通话时长1小时以上
                callList = Call.selectCall(context, startTime, endTime);
                total = 0;
                for (Call call : callList) {
                    total += call.duration;
                }
                if (total <= Constants.ONE_HOUR) {
                    return "今天已通话" + DB.getTimeBySecond(total / 1000) + "。";
                }
                break;
            case 7: // 业务员，一天内通话30个以上
                callList = Call.selectCall(context, startTime, endTime);
                if (callList.size() <= 30) {
                    return "今天已通话" + callList.size() + "个。";
                }
                break;
            case 8: // 摄影达人，一天内拍照30张
                imageInfoList = DB.selectImageInfo(context, startTime, endTime);
                if (imageInfoList.size() < 30) {
                    return "今天已拍照" + imageInfoList.size() + "张。";
                }
                break;
            case 9: // 肌肤之亲，一天内使用手机5小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null);
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR * 5) {
                    return "今天已使用手机" + DB.getTimeBySecond(total / 1000);
                }
                break;
            case 18: // NOTE2想静静，一天内通话时长为0
                long currentTime = System.currentTimeMillis();
                callList = Call.selectCall(context, currentTime - Constants.ONE_DAY, currentTime);
                if (callList.size() > 0) {
                    return "一天内通话次数为" + callList.size() + "次。";
                }
                break;
            case 19: // 放NOTE2一天假，一天不碰手机
                operationList = Operation.selectOperation(context, startTime - Constants.ONE_DAY, endTime - Constants.ONE_DAY, null);
                if (operationList.size() > 0) {
                    return "昨天碰了手机哦。";
                }
                locationInfoList = LocationInfo.selectLocation(context, startTime, endTime);
                if (locationInfoList.size() < 10) {
                    return "昨天一天没碰手机，但是“手机日记”有可能没有开启哦。";
                }
                break;
            case 20: // NOTE2不想剁手，连续3天不使用购物软件
                operationList = Operation.selectOperation(context, startTime - Constants.ONE_DAY * 3, endTime - Constants.ONE_DAY, null, "购物");
                if (operationList.size() > 0) {
                    return "这三天内使用了购物软件哦。";
                }
                total = 0;
                for (long start = startTime - Constants.ONE_DAY * 3; start < endTime - Constants.ONE_DAY; start += Constants.ONE_DAY) {
                    operationList = Operation.selectOperation(context, start, start + Constants.ONE_DAY, null);
                    if (operationList.size() > 0) {
                        total++;
                    }
                }
                if (total < 3) {
                    return "当前" + total + "天内没有使用购物软件了。";
                }
                break;
            case 21: // 闻鸡起舞，5点到6点之间有通话记录
                callList = Call.selectCall(context, startTime, endTime);
                Calendar c = Calendar.getInstance();
                total = 0;
                for (Call call : callList) {
                    c.setTimeInMillis(call.time);
                    if (c.get(Calendar.HOUR) == 5) {
                        total++;
                    }
                }
                if (total == 0) {
                    return "今天5点到6点之间没有通话记录哦。";
                }
                break;
            case 22: // 社交达人，一天内使用社交软件3小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null, "社交");
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR * 3) {
                    return "今天使用社交软件" + DB.getTimeBySecond(total / 1000) + "。";
                }
                break;
            case 23: // 剁手党，一天内使用购物软件1小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null, "购物");
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR) {
                    return "今天使用购物软件" + DB.getTimeBySecond(total / 1000L) + "。";
                }
                break;
            case 24: // 视频达人，一天内使用视频软件3小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null, "影音");
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR * 3) {
                    return "今天使用视频软件" + DB.getTimeBySecond(total / 1000) + "。";
                }
                break;
            case 25: // 游戏达人，一天内使用游戏软件3小时以上
                operationList = Operation.selectOperation(context, startTime, endTime, null, "棋牌天地,休闲娱乐,策略塔防,角色动作,飞行射击,速度激情,益智游戏,网络游戏,经营养成,体育竞技,儿童最爱");
                total = 0;
                for (Operation operation : operationList) {
                    total += operation.duration;
                }
                if (total <= Constants.ONE_HOUR * 3) {
                    return "今天使用游戏软件" + DB.getTimeBySecond(total / 1000) + "。";
                }
                break;
            case 26: // 解锁大师，一天内解锁手机屏幕20次
                operationList = Operation.selectOperationGroup(context, startTime, endTime);
                total = 0;
                for (Operation operation : operationList) {
                    if (Operation.SCREEN_ON.equals(operation.packageName)) {
                        total = operation.time;
                    }
                }
                if (total < 20) {
                    return "今天内解锁手机屏幕" + total + "次。";
                }
                break;
            case 27: // 亲密无间，一天内与同一联系人通话3次
                callList = Call.selectCallGroup(context, startTime, endTime);
                if (callList.size() == 0) {
                    return "今天没有给人打过电话。";
                }
                total = Integer.MIN_VALUE;
                for (Call call : callList) {
                    if (total < call.time) {
                        total = call.time;
                    }
                }
                if (total < 3) {
                    return "今天还没有与同一联系人通话3次哦。";
                }
                break;
            case 28: // app 达人，一天内使用手机应用20个以上
                operationList = Operation.selectOperationGroup(context, startTime, endTime);
                if (operationList.size() < 20) {
                    return "今天使用手机应用" + operationList.size() + "个";
                }
                break;
            case 35: // 带NOTE2认识新朋友，新增通讯录联系人10个
                HashMap<String, String> map = DB.getContacts(context, null, false);
                total = 0;
                for (String value : map.values()) {
                    try {
                        long longValue = Long.parseLong(value);
                        if (longValue >= startTime - Constants.ONE_DAY - 30 && longValue <= endTime) {
                            total++;
                        }
                    } catch (Exception e) {
                        Log.w("test", "TaskTools checkTask 35 cause an error=>:", e);
                    }
                }
                if (total < 10) {
                    return "本月新增通讯录联系人" + total + "个";
                }
                break;
            case 36: // NOTE2只想在这里，一天内足迹为1个坐标
                locationInfoList = LocationInfo.selectLocation(context, startTime, endTime);
                HashMap<String, Object> locationMap = new HashMap<>();
                for (HashMap<String, Object> locationInfo : locationInfoList) {
                    if (locationInfo.get("longitude") != null && locationInfo.get("latitude") != null) {
                        locationMap.put(locationInfo.get("longitude").toString() + locationInfo.get("latitude").toString(), "");
                    }
                }
                if (locationMap.size() != 1) {
                    return "今天足迹坐标一共有" + locationMap.size() + "个。";
                }
                break;
            case 37: // 口若悬河，一通电话达到30分钟以上
                callList = Call.selectCall(context, startTime, endTime);
                finish = false;
                for (Call call : callList) {
                    if (call.duration > 1000 * 60 * 30) {
                        finish = true;
                    }
                }
                if (!finish) {
                    return "今天没有一通电话达到30分钟以上。";
                }
                break;
            default:
                return "";
        }
        boolean stopAllTaskComplete = SP.getBoolean(context, SP.getUserIdKey(context, SP.STOP_ALL_TASK_COMPLETE), false);
        if (stopAllTaskComplete) {
            return "";
        }
        DB.getDataInterface(context).addTaskAnswer(SP.getUserId(context), String.valueOf(taskId), new NetManager.DataArray() {

            @Override
            public void getServiceData(JSONArray jsonArray) {
                Log.d("test", "TaskTools checkTask jsonArray=>:" + jsonArray);
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("test", "TaskTools checkTask volleyError=>:" + volleyError);
            }

        });
        return null;
    }

    public static void addStageTask(final Context context, final int id) {
        boolean stopAllTaskComplete = SP.getBoolean(context, SP.getUserIdKey(context, SP.STOP_ALL_TASK_COMPLETE), false);
        if (stopAllTaskComplete) {
            int guideId = SP.getInt(context, SP.getUserIdKey(context, SP.TASK_GUIDE_PARSE_ID), 0);
            if (guideId == id) {
                DB.getDataInterface(context).addTaskGuide(SP.getUserId(context), String.valueOf(id), new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        try {
                            JSONObject result = jsonArray.getJSONObject(0);
                            if ("000000".equals(result.getString("resp"))) {
                                SP.set(context, SP.getUserIdKey(context, SP.STOP_ALL_TASK_COMPLETE), false);
                                SP.set(context, SP.getUserIdKey(context, SP.TASK_GUIDE_PARSE), id - 1000);
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
        }
    }

    public static void showStagetTask(final Activity activity) {
        try {

            SP.set(activity, SP.getUserIdKey(activity, SP.STOP_ALL_TASK_COMPLETE), true); // 任务清单不再允许新任务的完成

            final int id = SP.getInt(activity, SP.getUserIdKey(activity, SP.TASK_GUIDE_PARSE_ID), 0);
            String content = SP.getString(activity, SP.getUserIdKey(activity, SP.TASK_GUIDE_PARSE_CONTENT), "");
            MessageTools.showTaskAlert(activity, "小主已完成本轮任务，进行\n\n“" + content + "”\n\n即可解锁下一关。", "确定", new MessageTools.OnClick() {
                @Override
                public void onClick() {
                    Intent intent;
                    switch (id) {
                        case 1001: // 更换头像一次
                            intent = new Intent(activity, UpdateUserInfo.class);
                            activity.startActivity(intent);
                            break;
                        case 1002: // 修改昵称一次
                            intent = new Intent(activity, UpdateUserInfo.class);
                            activity.startActivity(intent);
                            break;
                        case 1003: // 分享应用给好友一次
                            intent = new Intent(activity, HomeActivity.class);
                            activity.startActivity(intent);
                            break;
                        case 1004: // 写 1 篇手机日记
                            intent = new Intent(activity, DiaryEditorActivity.class);
                            activity.startActivity(intent);
                            break;
                        case 1005: // 分享内容 1 次
                            intent = new Intent(activity, WebHtmlActivity.class);
                            intent.putExtra("url", "share1.html");
                            activity.startActivity(intent);
                            break;
                        case 1006: // 新增关注 1 个
                            intent = new Intent(activity, WebHtmlActivity.class);
                            intent.putExtra("url", "discovery.html");
                            activity.startActivity(intent);
                            break;
                        case 1007: // 新增粉丝 1 个
                            intent = new Intent(activity, WebHtmlActivity.class);
                            intent.putExtra("url", "fans.html");
                            activity.startActivity(intent);
                            break;
                        case 1008: // 给好友点赞 1 次
                            intent = new Intent(activity, WebHtmlActivity.class);
                            intent.putExtra("url", "discovery.html");
                            activity.startActivity(intent);
                            break;
                        case 1009: // 评论好友心情 1 次
                            intent = new Intent(activity, WebHtmlActivity.class);
                            intent.putExtra("url", "discovery.html");
                            activity.startActivity(intent);
                            break;
                        case 1010: // 升级手机日记 1 次
                            intent = new Intent(activity, VersionUpdate.class);
                            activity.startActivity(intent);
                            break;
                        case 1011: // 吐槽“手机日记”
                            intent = new Intent(activity, FeedbackActivity.class);
                            activity.startActivity(intent);
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (Exception e) {
            Log.e("test", "TaskFragment showStagetTask cause an Exception=>:", e);
        }
    }

}
