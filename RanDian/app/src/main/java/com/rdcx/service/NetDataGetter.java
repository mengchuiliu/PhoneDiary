package com.rdcx.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.rdcx.randian.DiaryShow;
import com.rdcx.utils.Constants;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/11 0029.
 *
 * @author mengchuiliu
 */
public class NetDataGetter implements DataInterface {

    private static final int RE_LOGIN_TIME = 2;

    private NetManager netManager;

    public NetDataGetter(Context context) {
        if (netManager == null) {
            netManager = new NetManager(context);
        }
    }

    @Override
    public void commonRequest(String url, NetManager.DataArray listener, ErrorListener errorListener, Map<String, String> map) {
        netManager.postNetMsg(url, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public JSONArray commonRequest(String url, Map<String, String> map) {
        return netManager.postNetMsg(url, map, RE_LOGIN_TIME);
    }

    //登录
    @Override
    public void login(String phoneStr, String phoneNumber, String passWord, String type, String token,
                      NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("phoneStr", phoneStr);
        map.put("phoneNumber", phoneNumber);
        map.put("password", passWord);
        map.put("type", type);
        map.put("token", token);
        Log.d("test", "NetDataGetter login map=>:" + map);
        netManager.postNetMsg(Constants.LOGIN_URL, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public JSONArray login(String phoneStr, String phoneNumber, String passWord, String type, String token) {
        Map<String, String> map = new HashMap<>();
        map.put("phoneStr", phoneStr);
        map.put("phoneNumber", phoneNumber);
        map.put("password", passWord);
        map.put("type", type);
        map.put("token", token);
        Log.d("test", "NetDataGetter login map=>:" + map);
        return netManager.postNetMsg(Constants.LOGIN_URL, map, RE_LOGIN_TIME);
    }

    @Override
    public void binding(String userId, String logType, String type, String phoneNumber, String token, String phoneStr, String smsNumber, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("logType", logType); // logType 表示绑定什么东西：0代表绑定QQ，1代表绑定微信，2代表绑定微博，3代表绑定手机，4代表设备
        map.put("type", type); // type 表示以什么账号去查找用户，0代表QQ，1代表微信，2代表微博，3代表手机号码，只有在绑定设备时才需要这个参数
        map.put("phoneNumber", phoneNumber);
        map.put("token", token);
        map.put("phoneStr", phoneStr);
        map.put("smsNumber", smsNumber);
        Log.d("test", "NetDataGetter binding map=>:" + map);
        netManager.postNetMsg(Constants.BINDING_USER, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void unbinding(String userId, String logType, String phoneNumber, String token, String smsNumber, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("type", logType); // logType 表示绑定什么东西：0代表绑定QQ，1代表绑定微信，2代表绑定微博，3代表绑定手机，4代表设备
        map.put("phoneNumber", phoneNumber);
        map.put("token", token);
        map.put("smsNumber", smsNumber);
        netManager.postNetMsg(Constants.UNBINDING_USER, listener, errorListener, map, RE_LOGIN_TIME);
    }

    //验证手机是否存在
    @Override
    public void phoneExist(String phoneNumber, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", phoneNumber);
        netManager.postNetMsg(Constants.User_Exist, listener, errorListener, map, RE_LOGIN_TIME);
    }

    //发送短信验证码
    @Override
    public void sendSMS(String phoneNumber, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", phoneNumber);
        netManager.postNetMsg(Constants.YANZHENG_URL, listener, errorListener, map, RE_LOGIN_TIME);
    }

    //注册
    @Override
    public void registered(String phoneStr, String phoneNumber, String passWord, String sms,
                           NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("phoneStr", phoneStr);
        map.put("phoneNumber", phoneNumber);
        map.put("password", passWord);
        map.put("sms", sms);
        netManager.postNetMsg(Constants.REGIST_URL, listener, errorListener, map, RE_LOGIN_TIME);
    }

    //忘记密码
    @Override
    public void forgetPsw(String phoneNumber, String passWord, String sms, String type,
                          NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", phoneNumber);
        map.put("password", passWord);
        map.put("sms", sms);
        map.put("type", type);
        netManager.postNetMsg(Constants.FORGOT_PSW, listener, errorListener, map, RE_LOGIN_TIME);
    }

    //上传图片
    @Override
    public void uploadPhotos(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("data", data);
        netManager.postNetMsg(Constants.PHOTO_INFO, listener, errorListener, map, RE_LOGIN_TIME);
    }

    //上传通话记录
    @Override
    public void uploadPhones(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("data", data);
        netManager.postNetMsg(Constants.PHONE_INFO, listener, errorListener, map, RE_LOGIN_TIME);
    }

    // 上传应用使用信息
    @Override
    public void uploadOther(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("data", data);
        netManager.postNetMsg(Constants.APPLY_INFO, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void uploadGPSRecords(String userId, String data, NetManager.DataArray listener, ErrorListener
            errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("data", data);
        netManager.postNetMsg(Constants.UP_LOC, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void uploadIcon(String userId, String iconPath, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("photoPath", iconPath);
        netManager.postNetMsg(Constants.ICON_INFO, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void updateName(String userId, String phoneNumber, String phoneName, NetManager.DataArray listener,
                           ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("phoneNumber", phoneNumber);
        map.put("phoneName", phoneName);
        netManager.postNetMsg(Constants.UPDATE_NAME, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void feedBack(String userId, String phoneNumber, String content, NetManager.DataArray listener,
                         ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("phoneNumber", phoneNumber);
        map.put("content", content);
        netManager.postNetMsg(Constants.FEEDBACK, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void timeStamp(String type, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        netManager.postNetMsg(Constants.TIME_STAMP, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getRuleText(NetManager.DataArray listener, ErrorListener errorListener) {
        netManager.postNetMsg(Constants.GET_RULE_TEXT, listener, errorListener, null, RE_LOGIN_TIME);
    }

    @Override
    public void loginOut(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.LOGINOUT, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void dynamic(String userId, String dynamicText, String dynamicUrl, String address,
                        String type, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("dynamicText", dynamicText);
        map.put("dynamicUrl", dynamicUrl);
        map.put("address", address);
        map.put("type", type);
        netManager.postNetMsg(Constants.Loc_Dynamic, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public JSONArray findUploadOther(String userId, String startDate, String endDate) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return netManager.postNetMsg(Constants.FIND_UPLOAD_OTHER, map, RE_LOGIN_TIME);
    }

    @Override
    public JSONArray findUploadGPS(String userId, String startDate, String endDate) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return netManager.postNetMsg(Constants.FIND_UPLOAD_GPS, map, RE_LOGIN_TIME);
    }

    @Override
    public JSONArray findUploadPhone(String userId, String startDate, String endDate) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return netManager.postNetMsg(Constants.FIND_UPLOAD_PHONE, map, RE_LOGIN_TIME);
    }

    @Override
    public void messageFound(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.MESSAGEBOX, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getCurTime(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.NowTime, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void flashPage(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.FLASH_PAGE, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getMyListData(String userId, String dimension, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("dimension", dimension);
        netManager.postNetMsg(Constants.RankList, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getRankList(String userId, String dimension, String page, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("dimension", dimension);
        map.put("page", page);
        map.put("pageSize", "10");
        netManager.postNetMsg(Constants.AppRankList, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void pushRule(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.PUSH_RULE, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public JSONArray getActivityByName(String userId, String datas) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("datas", datas);
        return netManager.postNetMsg(Constants.GET_ACTIVITY_BY_NAME, map, RE_LOGIN_TIME);
    }

    @Override
    public void uploadDiary(String userId, String value, List<String> diaryPhotoList, HashMap<Integer, String> hashMap,
                            String nowDate, String phoneDate, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("value", value);
        map.put("nowDate", nowDate);
        map.put("phoneDate", phoneDate);
        if (diaryPhotoList != null && diaryPhotoList.size() > 0) {
            for (int i = 0; i < diaryPhotoList.size(); i++) {
                map.put("diaryPhotoList[" + i + "].photoUrl", diaryPhotoList.get(i));
            }
        }

        if (hashMap != null && hashMap.size() > 0) {
            int j = 0;
            for (Integer key : hashMap.keySet()) {
                map.put("diaryTextList[" + j + "].type", key + "");
                map.put("diaryTextList[" + j + "].value", hashMap.get(key));
                map.put("diaryTextList[" + j + "].typeText", DiaryShow.getDimenData(key, hashMap.get(key)));
                j++;
            }
        }
        netManager.postNetMsg(Constants.UploadDiary, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getDiaryData(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.GetDiaryData, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getRankLable(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.GetRankLable, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getPersonalRank(String userId, String dimension, String date, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("dimension", dimension);
        map.put("type", date);
        netManager.postNetMsg(Constants.GetPersonalRank, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getRankIcon(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.GetRankIcon, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getTaskByAll(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.GET_TASK_BY_ALL, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void taskAnswerByUserId(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.TASK_ANSWER_BY_USER_ID, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void addTaskPhoto(String userId, String taskId, String photoPath, String customDate, String figure, String event, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("taskId", taskId);
        map.put("photoPath", photoPath);
        map.put("customDate", customDate);
        map.put("figure", figure);
        map.put("event", event);
        netManager.postNetMsg(Constants.ADD_TASK_PHOTO, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void addTaskAnswer(String userId, String taskId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("modelList[0].userId", userId);
        map.put("modelList[0].taskId", taskId);
        netManager.postNetMsg(Constants.ADD_TASK_ANSWER, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void findTaskPhotoByUserId(String userId, String taskId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("taskId", taskId);
        netManager.postNetMsg(Constants.FIND_TASK_PHOTO_BY_USER_ID, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getNationalRank(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.TASK_NATIONAL_RANK, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getFriendRank(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.TASK_FRIEND_RANK, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void getSplashScreen(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.SPLASHSCREEN, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void findTaskGuideByUserId(String userId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        netManager.postNetMsg(Constants.FIND_TASK_GUIDE_BY_USER_ID, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void addTaskGuide(String userId, String taskId, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("taskId", taskId);
        netManager.postNetMsg(Constants.ADD_TASK_GUIDE, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void updateStr(String str, String channel, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("phoneStr", str);
        map.put("channel", channel);
        netManager.postNetMsg(Constants.UpdateStr, listener, errorListener, map, RE_LOGIN_TIME);
    }

    @Override
    public void uploadChannel(String userId, String channel, NetManager.DataArray listener, ErrorListener errorListener) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("channel", channel);
        netManager.postNetMsg(Constants.uploadChannel, listener, errorListener, map, RE_LOGIN_TIME);
    }
}
