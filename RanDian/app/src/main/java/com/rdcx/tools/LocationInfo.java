package com.rdcx.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/12/23 0023.
 * <p/>
 * 保存在数据库的 Location
 */
public class LocationInfo {

    /**
     * ID
     */
    public int id;

    /**
     * 时间点
     */
    public long time;

    /**
     * 经度
     */
    public String longitude;

    /**
     * 纬度
     */
    public String latitude;

    /**
     * 是否上传
     */
    public int upload;

    @Override
    public String toString() {
        return "LocationInfo{" +
                "id=" + id +
                ", time=" + DateFormat.format("yyyy-MM-dd HH:mm:ss", time) +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", upload=" + upload +
                '}';
    }

    /**
     * 向数据库中插入一条位置记录
     *
     * @param context  Context 对象
     * @param location 位置信息对象
     */
    public static boolean insertLocation(Context context, Location location) {
        if (location == null) {
            return false;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return false;
            }
            db.beginTransaction();
            try {
                db.execSQL("INSERT INTO location values(null,?,?,?,0)", new Object[]{location.getTime(), location.getLongitude(), location.getLatitude()});
                Log.d("test", "insertLocation a location time=>:" + location.getTime() + " location=>:" + location);
                db.setTransactionSuccessful();
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 向数据库中插入多条位置记录
     *
     * @param context          Context 对象
     * @param locationInfoList 位置信息对象集合
     */
    public static boolean insertLocationInfo(Context context, List<LocationInfo> locationInfoList) {
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return false;
            }
            try {
                db.beginTransaction();
                for (LocationInfo locationInfo : locationInfoList) {
                    if (locationInfo.id > 0) {
                        db.execSQL("UPDATE location set time=?,latitude=?,longitude=?,upload=? where _id=?", new Object[]{locationInfo.time, locationInfo.latitude, locationInfo.longitude, locationInfo.upload, locationInfo.id});
                        Log.d("test", "insertLocationInfo update a location=>:" + locationInfo);
                    } else {
                        int queryId = DB.getQueryInt(db, "select _id value from location where time>=" + locationInfo.time + " and time<" + (locationInfo.time + 1000), null);
                        if (queryId > 0) {
                            locationInfo.id = queryId;
                            db.execSQL("UPDATE location set time=?,latitude=?,longitude=?,upload=? where _id=?", new Object[]{locationInfo.time, locationInfo.latitude, locationInfo.longitude, locationInfo.upload, locationInfo.id});
                            Log.d("test", "insertLocationInfo update a location=>:" + locationInfo);
                        } else {
                            db.execSQL("INSERT INTO location values(null,?,?,?,?)", new Object[]{locationInfo.time, locationInfo.longitude, locationInfo.latitude, locationInfo.upload});
                            Log.d("test", "insertLocationInfo insert a location=>:" + locationInfo);
                        }
                    }
                }
                db.setTransactionSuccessful();
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 查询所有的位置信息
     *
     * @param context   Context 对象
     * @param startTime 起始时间点
     * @param endTime   结束时间点
     * @return Location 的 List
     */
    public static List<HashMap<String, Object>> selectLocation(Context context, long startTime, long endTime) {
        ArrayList<HashMap<String, Object>> resultList = new ArrayList<>();
        if (!PermissionTools.checkPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION, "", true)) {
            return resultList;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return resultList;
            }
            HashMap<String, Object> resultMap;
            String sql = "SELECT * FROM location";
            if (startTime > 0 && endTime > 0) {
                Log.d("test", "selectLocation->startTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " ->endTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime));
                sql += " where time>=" + startTime + " and time<" + endTime;
            }
            sql += " order by time desc";
            Cursor c = null;
            try {
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    resultMap = new HashMap<>();
                    resultMap.put("time", c.getLong(c.getColumnIndex("time")));
                    resultMap.put("longitude", c.getString(c.getColumnIndex("longitude")));
                    resultMap.put("latitude", c.getString(c.getColumnIndex("latitude")));
                    resultList.add(resultMap);
                    Log.d("test", "select a location time=>:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", (long) resultMap.get("time")) + " longitude=>:" + resultMap.get("longitude") + " latitude=>:" + resultMap.get("latitude"));
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
        return resultList;
    }
}
