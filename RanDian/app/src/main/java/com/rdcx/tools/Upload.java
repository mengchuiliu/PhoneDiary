package com.rdcx.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.NetManager;
import com.rdcx.service.UploadDiaryAT;
import com.sina.weibo.sdk.utils.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10 0010.
 * <p/>
 * 上传服务
 */
public class Upload {

    /**
     * 上传用户使用手机的数据
     *
     * @param context Context
     */
    public static void uploadOperation(final Context context) {

        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.w("test", "uploadOperation->:用户尚未登陆，不能同步数据。");
            return;
        }

        Log.w("test", "uploadOperation->:开始上传应用的使用数据。");

        long startTime = 0;

        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return;
            }
            try {
                startTime = DB.getQueryLong(db, "SELECT min(time) value FROM operation where upload = 0", null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }

        if (startTime <= 0) {
            Log.w("test", "uploadOperation->:暂时没有需要更新的应用使用数据，startTime=>:" + startTime);
            return;
        }

        final List<Operation> operationList = Operation.selectOperation(context, startTime, System.currentTimeMillis(), null, null, "0");

        Download.mergeTime(context, SP.DOWNLOAD_OPERATION_TIME, userId, DB.getDBTime(DB.TYPE_TODAY, startTime).get("start"), DB.getDBTime(DB.TYPE_TODAY).get("end"));

        if (operationList.size() <= 0) {
            Log.w("test", "uploadOperation->:暂时没有需要更新的应用使用数据，operationList.size()=>:" + operationList.size());
            return;
        }

        realUploadOperation(context, operationList, 0, 100);

    }

    private static void realUploadOperation(final Context context, final List<Operation> operationList, final int index, final int count) {
        Log.d("test", "uploadOperation index=>:" + index + " ,count=>:" + count);
        StringBuilder sb = new StringBuilder();
        for (int i = index, j = 0, len = operationList.size(); i < len && j < count; i++, j++) {
            Operation operation = operationList.get(i);
            if (Operation.SCREEN_ON.equals(operation.packageName)) {
                continue;
            }
            sb.append(operation.packageName).append(":");
            sb.append(operation.time).append(",");
            sb.append(operation.duration / 1000).append(";");
            sb.append("_");
        }
        if (sb.length() > 0) {
            Log.d("test", "uploadOperation data=>" + sb.toString());
            DB.getDataInterface(context).uploadOther(SP.getString(context, SP.USER_ID, ""), sb.toString(), new NetManager.DataArray() {

                @Override
                public void getServiceData(JSONArray jsonArray) {
                    Log.d("test", "uploadOperation jsonArray->:" + jsonArray);
                    try {
                        JSONObject jo = jsonArray.getJSONObject(0);
                        if ("000000".equals(jo.getString("resp"))) {
                            for (int i = index, j = 0, len = operationList.size(); i < len && j < count; i++, j++) {
                                Operation operation = operationList.get(i);
                                operation.isInsert = true;
                                operation.upload = 1;
                            }
                            Operation.insertOperationList(context, operationList, index, count);
                            realUploadOperation(context, operationList, index + count, count);
                        }
                    } catch (Exception e) {
                        Log.e("test", "uploadOperation 向服务请求时返回值不正确->:" + e);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("test", "uploadOperation volleyError->:" + volleyError);
                }
            });
        }
    }

    /**
     * 上传足迹信息
     *
     * @param context Context 对象
     */
    public static void uploadLocation(final Context context) {

        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.d("test", "uploadLocation->:用户尚未登陆，不能同步数据。");
            return;
        }

        Log.d("test", "uploadLocation->:开始上传足迹信息：");

        final ArrayList<LocationInfo> locationList = new ArrayList<>();
        StringBuilder sb;

        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return;
            }
            LocationInfo locationInfo;
            String sql = "SELECT * FROM location where upload = 0 order by time asc";
            Cursor c = null;
            sb = new StringBuilder();
            try {
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    locationInfo = new LocationInfo();
                    locationInfo.id = c.getInt(c.getColumnIndex("_id"));
                    locationInfo.time = c.getLong(c.getColumnIndex("time"));
                    locationInfo.longitude = c.getString(c.getColumnIndex("longitude"));
                    locationInfo.latitude = c.getString(c.getColumnIndex("latitude"));
                    locationInfo.upload = c.getInt(c.getColumnIndex("upload"));
                    locationList.add(locationInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
                db.close();
            }
        }

        if (locationList.size() > 0) {

            for (LocationInfo locationInfoTemp : locationList) {
                sb.append(locationInfoTemp.latitude).append(",");
                sb.append(locationInfoTemp.longitude).append(",");
                sb.append(locationInfoTemp.time).append(";");
            }
            Log.d("test", "sb.toString()->:" + sb.toString());
            DB.getDataInterface(context).uploadGPSRecords(SP.getString(context, SP.USER_ID, ""), sb.toString(), new NetManager.DataArray() {

                @Override
                public void getServiceData(JSONArray jsonArray) {
                    Log.d("test", "uploadLocation uploadGPSRecords jsonArray->:" + jsonArray);
                    try {
                        JSONObject jo = jsonArray.getJSONObject(0);
                        if ("000000".equals(jo.getString("resp"))) {
                            for (LocationInfo locationInfoTemp : locationList) {
                                locationInfoTemp.upload = 1;
                            }
                            LocationInfo.insertLocationInfo(context, locationList);
                            Log.d("test", "uploadLocation->:上传足迹信息成功。");
                        }
                    } catch (Exception e) {
                        Log.e("test", "uploadLocation uploadGPSRecords 向服务请求时返回值不正确->:" + e);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("test", "uploadLocation uploadGPSRecords volleyError->:" + volleyError);
                }
            });

        } else {

            Log.d("test", "uploadLocation->:暂时没有需要上传的图片信息。");

        }
    }


    /**
     * 上传通话记录至服务器
     *
     * @param context Context
     */
    public static void uploadCall(final Context context) {
        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.d("test", "uploadCall->:用户尚未登陆，不能同步数据。");
            return;
        }

        Log.d("test", "uploadCall->:开始上传通话记录。");
        final ArrayList<Call> callList = new ArrayList<>();
        synchronized (DB.class) {
            // 向数据库中查询需要上传的通话记录
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return;
            }
            Call call;
            String sql = "SELECT * FROM call where upload = 0 order by time asc";
            Cursor c = null;
            try {
                HashMap<String, String> contactMap = null;
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    call = new Call();
                    call.id = c.getInt(c.getColumnIndex("_id"));
                    call.time = c.getLong(c.getColumnIndex("time"));
                    call.number = c.getString(c.getColumnIndex("number"));
                    call.duration = c.getLong(c.getColumnIndex("duration"));
                    call.type = c.getInt(c.getColumnIndex("type"));
                    if (contactMap == null) {
                        contactMap = DB.getContacts(context, null, true);
                    }
                    call.name = contactMap.get(call.number);
                    callList.add(call);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
                db.close();
            }
        }

        if (callList.size() > 0) {

            realUploadCall(context, callList, 0, 100);

        } else {

            Log.d("test", "uploadCall->:没有需要上传的通话记录。");

        }
    }

    private static void realUploadCall(final Context context, final ArrayList<Call> callList, final int index, final int count) {
        Log.d("test", "uploadPhones index=>:" + index + " ,count=>:" + count);
        StringBuilder sb = new StringBuilder();
        for (int i = index, j = 0, len = callList.size(); i < len && j < count; i++, j++) {
            Call call = callList.get(i);
            sb.append(call.id).append(",");
            sb.append(call.number).append(",");
            sb.append(call.time).append(",");
            sb.append(call.duration).append(",");
            sb.append(call.type);
            if (call.name != null) {
                sb.append(",").append(call.name);
            }
            sb.append(";");
        }
        int lastIndex = sb.lastIndexOf(";");
        if (lastIndex > 0) {
            sb.deleteCharAt(lastIndex);
        }
        if (sb.length() > 0) {
            Log.d("test", "uploadPhones data=>" + sb.toString());
            DB.getDataInterface(context).uploadPhones(SP.getString(context, SP.USER_ID, ""), sb.toString(), new NetManager.DataArray() {

                @Override
                public void getServiceData(JSONArray jsonArray) {
                    Log.d("test", "uploadPhones jsonArray->:" + jsonArray);
                    try {
                        JSONObject jo = jsonArray.getJSONObject(0);
                        if ("000000".equals(jo.getString("resp"))) {
                            for (int i = index, j = 0, len = callList.size(); i < len && j < count; i++, j++) {
                                Call call = callList.get(i);
                                call.upload = 1;
                            }
                            Call.insertCall(context, callList, index, count);
                            realUploadCall(context, callList, index + count, count);
                        }
                    } catch (Exception e) {
                        Log.e("test", "uploadPhones 向服务请求时返回值不正确->:" + e);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("test", "uploadPhones volleyError->:" + volleyError);
                }
            });
        }
    }

    /**
     * 上传图片信息
     *
     * @param context Context 对象
     */
    public static void uploadImageInfo(final Context context) {

        String userId = SP.getString(context, SP.USER_ID, null);
        if (userId == null || userId.length() < 0) {
            Log.d("test", "uploadImageInfo->:用户尚未登陆，不能同步数据。");
            return;
        }

        Log.d("test", "uploadImageInfo->:开始上传图片信息：");

        final ArrayList<ImageInfo> imageInfoList = new ArrayList<>();
        StringBuilder sb;
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return;
            }
            ImageInfo imageInfo;
            String sql = "SELECT * FROM image where upload = 0 order by time asc";
            Cursor c = null;
            sb = new StringBuilder();
            try {
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    imageInfo = new ImageInfo();
                    imageInfo.id = c.getInt(c.getColumnIndex("_id"));
                    imageInfo.time = c.getLong(c.getColumnIndex("time"));
                    imageInfo.path = c.getString(c.getColumnIndex("path"));
                    imageInfo.longitude = Double.parseDouble(c.getString(c.getColumnIndex("longitude")));
                    imageInfo.latitude = Double.parseDouble(c.getString(c.getColumnIndex("latitude")));
                    imageInfo.address = c.getString(c.getColumnIndex("address"));
                    imageInfo.upload = c.getInt(c.getColumnIndex("upload"));
                    imageInfoList.add(imageInfo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
                db.close();
            }
        }

        if (imageInfoList.size() > 0) {

            for (ImageInfo imageInfoTemp : imageInfoList) {
                sb.append(MD5.hexdigest(imageInfoTemp.path).toUpperCase()).append(",");
                sb.append(imageInfoTemp.time).append(";");
            }
            Log.d("test", "DB uploadImageInfo uploadPhotos sb.toString()=>:" + sb.toString());
            DB.getDataInterface(context).uploadPhotos(SP.getString(context, SP.USER_ID, ""), sb.toString(), new NetManager.DataArray() {

                @Override
                public void getServiceData(JSONArray jsonArray) {
                    Log.d("test", "uploadImageInfo uploadPhotos jsonArray->:" + jsonArray);
                    try {
                        JSONObject jo = jsonArray.getJSONObject(0);
                        if ("000000".equals(jo.getString("resp"))) {
                            for (ImageInfo imageInfoTemp : imageInfoList) {
                                imageInfoTemp.upload = 1;
                            }
                            DB.insertImageInfos(context, imageInfoList);
                        }
                    } catch (Exception e) {
                        Log.e("test", "uploadImageInfo uploadPhotos 向服务请求时返回值不正确->:" + e);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("test", "uploadImageInfo uploadPhotos volleyError->:" + volleyError);
                }
            });

        } else {

            Log.d("test", "uploadImageInfo->:暂时没有需要上传的图片信息。");

        }
    }

    /**
     * 上传日记本信息
     *
     * @param diaryList 本地日记本数据集合
     */
    @SuppressLint({"HandlerLeak"})
    public static void uploadDiary(Context context, List<Diary> diaryList) {
        for (Diary diary : diaryList) {
            //Log.e("my_log", "1111111 -->:" + diary.toString());
            if (diary.upload == 0) {
                Log.e("my_log", "uploadDiary --> 日记已经上传中");
                try {
                    boolean isUpload = false;
                    if (!TextUtils.isEmpty(diary.path)) {
                        JSONArray jsonArray = new JSONArray(diary.path);
                        if (jsonArray.length() > 0) {
                            List<Integer> list = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (jsonObject.getString("isUpload").equals("0")) {
                                    isUpload = true;
                                    list.add(i);
                                }
                            }
                            if (list.size() > 0) {
                                new UploadDiaryAT(context, diary, list).execute();
                            }
                            if (!isUpload) {
                                uploadDiary(context, diary);
                            }
                        } else {
                            Log.e("my_log", "uploadDiary --> 日记无图片上传");
                            uploadDiary(context, diary);
                        }
                    } else {
                        Log.e("my_log", "uploadDiary --> 日记无图片上传");
                        uploadDiary(context, diary);
                    }
                } catch (Exception e) {
                    Log.e("my_log", "uploadDiary --> 日记已经上传中报错" + e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.e("my_log", "uploadDiary --> 日记已经上传过");
            }
        }
    }

    public static void uploadDiary(final Context context, final Diary diary) {
        List<String> paths = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(diary.path);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("isUpload").equals("0")) {
                        return;
                    } else {
                        paths.add(jsonObject.getString("servicePath"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<Integer, String> hashMap = new HashMap<>();
        try {
            com.alibaba.fastjson.JSONObject jo = JSON.parseObject(diary.data);
            for (String str : jo.keySet()) {
                hashMap.put(Integer.valueOf(str), jo.getString(str));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DB.getDataInterface(context).uploadDiary(SP.getString(context, SP.USER_ID, ""), diary.text,
                paths, hashMap, Long.toString(diary.nowDate), Long.toString(diary.time),
                new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        try {
                            JSONObject jo = jsonArray.getJSONObject(0);
                            Log.e("my_log", "uploadDiary -->:" + jo.toString());
                            if ("000000".equals(jo.getString("resp"))) {
                                diary.upload = 1;
                                DB.insertOrUpdateDiary(context, diary);
                                Log.e("liu_test", "uploadDiary -->:" + "日记上传成功!");
                            } else {
                                Log.e("liu_test", "uploadDiary -->:" + "日记上传失败!");
                            }
                        } catch (Exception e) {
                            Log.e("test", "uploadDiary 向服务请求时返回值不正确->:" + e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("liu_test", "uploadDiary volleyError->:" + volleyError);
                    }
                });
    }
}
