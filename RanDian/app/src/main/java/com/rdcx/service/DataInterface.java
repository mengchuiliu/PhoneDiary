package com.rdcx.service;

import com.android.volley.Response.ErrorListener;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/28 0028.
 * 网络接口
 *
 * @author mengchuiliu
 */
public interface DataInterface {

    // 通用请求方法
    void commonRequest(String url, NetManager.DataArray listener, ErrorListener errorListener, Map<String, String> map);

    JSONArray commonRequest(String url, Map<String, String> map);

    // 异步登录
    void login(String phoneStr, String phoneNumber, String passWord, String type, String token,
               NetManager.DataArray listener, ErrorListener errorListener);

    // 同步登陆
    JSONArray login(String phoneStr, String phoneNumber, String passWord, String type, String token);

    // 绑定
    void binding(String userId, String logType, String type, String phoneNumber, String token, String phoneStr, String smsNumber, NetManager.DataArray listener, ErrorListener errorListener);

    // 解绑
    void unbinding(String userId, String logType, String phoneNumber, String token, String smsNumber, NetManager.DataArray listener, ErrorListener errorListener);

    //验证手机是否存在
    void phoneExist(String phoneNumber, NetManager.DataArray listener, ErrorListener errorListener);

    //发送手机验证码
    void sendSMS(String phoneNumber, NetManager.DataArray listener, ErrorListener errorListener);

    //注册
    void registered(String phoneStr, String phoneNumber, String passWord, String sms,
                    NetManager.DataArray listener, ErrorListener errorListener);

    //忘记密码
    void forgetPsw(String phoneNumber, String passWord, String sms, String type,
                   NetManager.DataArray listener, ErrorListener errorListener);

    //上传照片
    void uploadPhotos(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener);

    //上传通话记录
    void uploadPhones(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener);

    // 上传应用使用信息
    void uploadOther(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener);

    // 上传 GPS 足迹信息
    void uploadGPSRecords(String userId, String data, NetManager.DataArray listener, ErrorListener errorListener);

    //上传头像
    void uploadIcon(String userId, String iconPath, NetManager.DataArray listener, ErrorListener errorListener);

    //修改昵称
    void updateName(String userId, String phoneNumber, String phoneName, NetManager.DataArray listener,
                    ErrorListener errorListener);

    //意见反馈
    void feedBack(String userId, String phoneNumber, String content, NetManager.DataArray listener,
                  ErrorListener errorListener);

    //查询时间戳
    void timeStamp(String type, NetManager.DataArray listener, ErrorListener errorListener);

    // 查询所有文案
    void getRuleText(NetManager.DataArray listener, ErrorListener errorListener);

    //登出
    void loginOut(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    //本地动态
    void dynamic(String userId, String dynamicText, String dynamicUrl, String address,
                 String type, NetManager.DataArray listener, ErrorListener errorListener);

    // 下载应用使用信息
    JSONArray findUploadOther(String userId, String startDate, String endDate);

    // 下载足迹信息
    JSONArray findUploadGPS(String userId, String startDate, String endDate);

    // 下载足迹信息
    JSONArray findUploadPhone(String userId, String startDate, String endDate);

    void messageFound(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void getCurTime(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void flashPage(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    //获取个人排行榜数据
    void getMyListData(String userId, String dimension, NetManager.DataArray listener, ErrorListener errorListener);

    //应用排行榜
    void getRankList(String userId, String dimension, String page, NetManager.DataArray listener, ErrorListener errorListener);

    // 推送规则
    void pushRule(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    // 通过包名获取应用名和应用分类
    JSONArray getActivityByName(String userId, String datas);

    //上传日记本数据
    void uploadDiary(String userId, String value, List<String> diaryPhotoList, HashMap<Integer, String> hashMap,
                     String nowDate, String phoneDate, NetManager.DataArray listener, ErrorListener errorListener);

    //获取日记本数据
    void getDiaryData(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void getTaskByAll(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void taskAnswerByUserId(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void addTaskPhoto(String userId, String taskId, String photoPath, String customDate, String figure, String event, NetManager.DataArray listener, ErrorListener errorListener);

    void addTaskAnswer(String userId, String taskId, NetManager.DataArray listener, ErrorListener errorListener);

    //贴标签
    void getRankLable(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    //个人排行榜
    void getPersonalRank(String userId, String dimension, String date, NetManager.DataArray listener, ErrorListener errorListener);

    void getRankIcon(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void findTaskPhotoByUserId(String userId, String taskId, NetManager.DataArray listener, ErrorListener errorListener);

    void getNationalRank(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void getFriendRank(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void getSplashScreen(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void findTaskGuideByUserId(String userId, NetManager.DataArray listener, ErrorListener errorListener);

    void addTaskGuide(String userId, String taskId, NetManager.DataArray listener, ErrorListener errorListener);

    void updateStr(String str, String channel, NetManager.DataArray listener, ErrorListener errorListener);

    void uploadChannel(String userId, String channel, NetManager.DataArray listener, ErrorListener errorListener);
}
