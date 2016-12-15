package com.rdcx.tools;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.NetManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/28 0028.
 * <p/>
 * 下载服务
 */
public class Download {

    public static void downloadOperation(Context context, long startTime, long endTime) {

        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.d("test", "Download downloadOperation->:用户尚未登陆，不能同步数据。");
            return;
        }

        String downloadOperationTime = mergeTime(SP.getString(context, SP.DOWNLOAD_OPERATION_TIME + userId, null), startTime, endTime);
        if (downloadOperationTime == null) {
            Log.d("test", "Download downloadOperation 时间段从" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " - " + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime) + " 的数据已经存在。");
            return;
        }

        JSONArray jsonArray = DB.getDataInterface(context).findUploadOther(userId, String.valueOf(startTime), String.valueOf(endTime));
        if (jsonArray == null) {
            Log.e("test", "Download downloadOperation findUploadOther jsonArray=>:null");
            return;
        }
        try {
            JSONObject jo = jsonArray.getJSONObject(0);
            JSONArray modelList = jo.getJSONArray("modelList");
            ArrayList<Operation> operationArrayList = new ArrayList<>();
            JSONObject joTemp;
            Operation operation;
            Map<String, Object> packageNameMap = new HashMap<>();
            for (int i = 0, len = modelList.length(); i < len; i++) {
                joTemp = modelList.getJSONObject(i);
                operation = new Operation();
                operation.time = joTemp.getJSONObject("date").getLong("time");
                operation.packageName = joTemp.getString("name");
                operation.packageName = operation.packageName == null ? null : operation.packageName.replace("&&", "_");
                if (packageNameMap.get(operation.packageName) == null) {
                    packageNameMap.put(operation.packageName, new Object());
                    App app = new App();
                    app.packageName = operation.packageName;
                    App.insertOrUpdateApp(context, app);
                }
                operation.duration = Long.valueOf(joTemp.getString("number")) * 1000;
                operation.upload = 1;
                operationArrayList.add(operation);
            }
            Operation.insertOperationList(context, operationArrayList, 0, operationArrayList.size());
            SP.set(context, SP.DOWNLOAD_OPERATION_TIME + userId, downloadOperationTime);
            Log.d("test", "Download downloadOperation 时间段从" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " - " + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime) + " 的数据下载成功。");
        } catch (Exception e) {
            Log.e("test", "Download downloadOperation findUploadOther 向服务请求时返回值不正确->:" + e);
        }
    }

    public static void downloadLocation(Context context, long startTime, long endTime) {
        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.d("test", "Download downloadLocation->:用户尚未登陆，不能同步足迹信息。");
            return;
        }

        String downloadGPSTime = mergeTime(SP.getString(context, SP.DOWNLOAD_GPS_TIME + userId, null), startTime, endTime);
        if (downloadGPSTime == null) {
            Log.d("test", "Download downloadLocation 时间段从" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " - " + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime) + " 的足迹信息已经存在。");
            return;
        }

        JSONArray jsonArray = DB.getDataInterface(context).findUploadGPS(userId, String.valueOf(startTime), String.valueOf(endTime));
        if (jsonArray == null) {
            Log.e("test", "Download downloadLocation findUploadGPS jsonArray=>:null");
            return;
        }
        try {
            JSONObject jo = jsonArray.getJSONObject(0);
            JSONArray modelList = jo.getJSONArray("modelList");
            List<LocationInfo> locationInfoList = new ArrayList<>();
            JSONObject joTemp;
            LocationInfo locationInfo;
            for (int i = 0, len = modelList.length(); i < len; i++) {
                joTemp = modelList.getJSONObject(i);
                locationInfo = new LocationInfo();
                locationInfo.time = joTemp.getJSONObject("date").getLong("time");
                locationInfo.latitude = joTemp.getString("lat");
                locationInfo.longitude = joTemp.getString("lng");
                locationInfo.upload = 1;
                locationInfoList.add(locationInfo);
            }
            LocationInfo.insertLocationInfo(context, locationInfoList);
            SP.set(context, SP.DOWNLOAD_GPS_TIME + userId, downloadGPSTime);
            Log.d("test", "Download downloadLocation 时间段从" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " - " + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime) + " 的足迹信息下载成功。");
        } catch (Exception e) {
            Log.e("test", "Download downloadLocation findUploadGPS 向服务请求时返回值不正确->:" + e);
        }
    }

    public static void downloadPhone(Context context, long startTime, long endTime) {
        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.d("test", "Download downloadPhone->:用户尚未登陆，不能同步通话记录。");
            return;
        }

        String downloadPhoneTime = mergeTime(SP.getString(context, SP.DOWNLOAD_PHONE_TIME + userId, null), startTime, endTime);
        if (downloadPhoneTime == null) {
            Log.d("test", "Download downloadPhone 时间段从" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " - " + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime) + " 的通话记录已经存在。");
            return;
        }

        JSONArray jsonArray = DB.getDataInterface(context).findUploadPhone(userId, String.valueOf(startTime), String.valueOf(endTime));
        if (jsonArray == null) {
            Log.e("test", "Download downloadPhone findUploadPhone jsonArray=>:null");
            return;
        }
        try {
            JSONObject jo = jsonArray.getJSONObject(0);
            JSONArray modelList = jo.getJSONArray("modelList");
            List<Call> callList = new ArrayList<>();
            JSONObject joTemp;
            Call call;
            for (int i = 0, len = modelList.length(); i < len; i++) {
                joTemp = modelList.getJSONObject(i);
                call = new Call();
                call.time = joTemp.getJSONObject("date").getLong("time");
                call.number = joTemp.getString("number");
                call.duration = joTemp.getLong("duration");
                call.name = joTemp.getString("name");
                call.type = joTemp.getInt("type");
                call.upload = 1;
                callList.add(call);
            }
            Call.insertCall(context, callList, 0, callList.size());
            SP.set(context, SP.DOWNLOAD_PHONE_TIME + userId, downloadPhoneTime);
            Log.d("test", "Download downloadPhone 时间段从" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " - " + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime) + " 的通话记录下载成功。");
        } catch (Exception e) {
            Log.e("test", "Download downloadPhone findUploadPhone 向服务请求时返回值不正确->:" + e);
        }
    }

    public static void downloadDiary(final Context context) {
        final String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.e("my_log", "downloadDiary->:用户尚未登陆，不能同步数据。");
            return;
        }
        //第一次登录下载日记本数据
        boolean DownloadDiary = SP.getBoolean(context, "DownloadDiary", true);
        if (!DownloadDiary) {
            Log.e("my_log", "downloadDiary->:已经下载过日记本数据");
            return;
        }
        DB.getDataInterface(context).getDiaryData(userId, new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                Log.e("my_log", "downloadDiary ------->:" + jsonArray);
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if (jo.getString("resp").equals("000000")) {
                        JSONArray modelList = jo.getJSONArray("modelList");
                        JSONObject joTemp;
                        for (int i = 0; i < modelList.length(); i++) {
                            joTemp = modelList.getJSONObject(i);
                            Diary diary = new Diary();
                            diary.time = joTemp.getJSONObject("phoneNow").getLong("time");
                            diary.text = joTemp.getString("value");
                            diary.upload = 1;
                            JSONArray arrayPath = joTemp.getJSONArray("diaryPhotoList");
                            com.alibaba.fastjson.JSONArray array = new com.alibaba.fastjson.JSONArray();
                            com.alibaba.fastjson.JSONObject jsonObject;
                            for (int j = 0; j < arrayPath.length(); j++) {
                                jsonObject = new com.alibaba.fastjson.JSONObject();
                                jsonObject.put("locaPath", "");
                                jsonObject.put("servicePath", arrayPath.getJSONObject(j).getString("photoUrl"));
                                jsonObject.put("isUpload", "1");
                                array.add(j, jsonObject);
                            }
                            diary.path = array.toString();
                            JSONArray diaryTextList = joTemp.getJSONArray("diaryTextList");
                            StringBuilder builder = new StringBuilder();
                            HashMap<Integer, String> hashMap = new HashMap<>();
                            for (int k = 0; k < diaryTextList.length(); k++) {
                                builder.append(diaryTextList.getJSONObject(k).getString("typeText")).append(";");
                                if ((k + 1) % 2 == 0 && i != diaryTextList.length()) {
                                    builder.append("\n");
                                }
                                hashMap.put(Integer.valueOf(diaryTextList.getJSONObject(k).getString("type")), diaryTextList.getJSONObject(k).getString("value"));
                            }
                            if (hashMap.size() > 0)
                                diary.data = com.alibaba.fastjson.JSONObject.toJSONString(hashMap);
                            diary.datatext = builder.toString();
                            DB.insertOrUpdateDiary(context, diary);
                            //new DiaryIcon(diary, context).execute();
                        }
                        SP.set(context, "DownloadDiary", false);
                        Log.e("my_log", "downloadDiary -->:日记下载完成");
                    }
                } catch (Exception e) {
                    Log.e("my_log", "downloadDiary 解析异常-->:" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("my_log", "downloadDiary volleyError->:" + volleyError);
            }
        });
    }

    public static String mergeTime(String json, long startTime, long endTime) {
        if (json == null) {
            json = "[]";
        }
        Log.d("test", "Download mergeTime json=>:" + json + ",startTime=>:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + ",endTime=>:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime));
        if (startTime > System.currentTimeMillis() || startTime > endTime) {
            return null;
        }
        com.alibaba.fastjson.JSONArray timeList = null;
        try {
            timeList = JSON.parseArray(json);
            int count = 0;
            for (int i = 0; i < timeList.size(); i++) {
                com.alibaba.fastjson.JSONObject jsonObject = timeList.getJSONObject(i);
                long mapStart = jsonObject.getLong("s");
                long mapEnd = jsonObject.getLong("e");
                if (mapStart <= startTime && mapEnd >= endTime) { // 包含
                    return null;
                } else if (mapStart >= startTime && mapEnd <= endTime) { // 反包含
                    jsonObject.put("s", startTime);
                    jsonObject.put("e", endTime);
                    break;
                } else if (mapStart <= startTime && mapEnd >= startTime && mapEnd <= endTime) {
                    jsonObject.put("e", endTime);
                    break;
                } else if (mapStart >= startTime && mapStart <= endTime && mapEnd >= endTime) {
                    jsonObject.put("s", startTime);
                    break;
                }
                count++;
            }
            if (count == timeList.size()) {
                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                jsonObject.put("s", startTime);
                jsonObject.put("e", endTime);
                timeList.add(jsonObject);
            }
            Collections.sort(timeList, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return (int) (((com.alibaba.fastjson.JSONObject) o1).getLong("s") - ((com.alibaba.fastjson.JSONObject) o2).getLong("s"));
                }
            });
            for (int i = 0, len = timeList.size() - 1; i < len; ) {
                if (timeList.getJSONObject(i).getLong("e") >= timeList.getJSONObject(i + 1).getLong("s")) {
                    timeList.getJSONObject(i).put("s", Math.min(timeList.getJSONObject(i).getLong("s"), timeList.getJSONObject(i + 1).getLong("s")));
                    timeList.getJSONObject(i).put("e", Math.max(timeList.getJSONObject(i).getLong("e"), timeList.getJSONObject(i + 1).getLong("e")));
                    timeList.remove(i + 1);
                    len--;
                } else {
                    i++;
                }
            }

            for (int i = 0; i < timeList.size(); i++) {
                com.alibaba.fastjson.JSONObject jsonObject = timeList.getJSONObject(i);
                Log.d("test", "Download mergeTime jsonObject s=>:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", jsonObject.getDate("s")) + ",e=>:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", jsonObject.getDate("e")));
            }
            return timeList.toJSONString();

        } catch (Exception e) {
            Log.e("test", "Download mergeTime cause an Exception=>:" + e);
            return null;
        }

    }

    public static void mergeTime(Context context, String spKey, String userId, long start, long end) {
        String str = mergeTime(SP.getString(context, spKey + userId, null), start, end);
        if (str != null) {
            SP.set(context, spKey + userId, str);
        }
    }

}
