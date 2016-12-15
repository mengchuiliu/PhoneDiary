package com.rdcx.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/12/11 0011.
 * <p/>
 * 一条通话记录
 */
public class Call {

    /**
     * ID 号
     */
    public int id;

    /**
     * 时间
     */
    public long time;

    /**
     * 应用程序
     */
    public String number;

    /**
     * 姓名
     */
    public String name;

    /**
     * 持续时间
     */
    public long duration;

    /**
     * 类型
     */
    public int type;

    /**
     * 是否已经上传
     */
    public int upload;

    @Override
    public String toString() {
        return "Call{" +
                "id=" + id +
                ", time=" + time +
                ", time=" + DateFormat.format("yyyy-MM-dd HH:mm:ss", time) +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", duration=" + duration +
                ", type=" + type +
                ", upload=" + upload +
                '}';
    }

    /**
     * 向 call 表中插入多条数据
     *
     * @param context  Context
     * @param callList 需要向 call 表中插入的数据
     * @return 数据是否插入成功
     */
    public static boolean insertCall(Context context, List<Call> callList, int startIndex, int count) {
        synchronized (DB.class) {
            if (context == null || callList == null) {
                return false;
            }
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return false;
            }
            db.beginTransaction();
            try {
                for (int i = startIndex, j = 0, len = callList.size(); i < len && j < count; i++, j++) {
                    Call call = callList.get(i);
                    call.time = (call.time / 1000) * 1000;
                    if (call.id > 0) {
                        db.execSQL("UPDATE call set time=?,number=?,duration=?,type=?,upload=? where _id=?", new Object[]{call.time, call.number, call.duration, call.type, call.upload, call.id});
                        Log.w("test", "DB insertCall update a call=>:" + call);
                    } else {
                        int queryId = DB.getQueryInt(db, "select _id value from call where time>=" + call.time + " and time<" + (call.time + 1000), null);
                        Log.d("test", "Call insertCall queryId=>:" + queryId);
                        if (queryId > 0) {
                            call.id = queryId;
                            db.execSQL("UPDATE call set time=?,number=?,duration=?,type=?,upload=? where _id=?", new Object[]{call.time, call.number, call.duration, call.type, call.upload, call.id});
                            Log.w("test", "DB insertCall update a call=>:" + call);
                        } else {
                            db.execSQL("INSERT INTO call values(null,?,?,?,?,?)", new Object[]{call.time, call.number, call.duration, call.type, call.upload});
                            Log.w("test", "DB insertCall insert a call=>:" + call);
                        }
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                db.endTransaction();
                db.close();
            }
            return true;
        }
    }

    /**
     * 同步系统的通话记录到本地数据库
     *
     * @param context context 对象
     * @param time    需要同步到的时间点
     * @return 是否同步成功
     */
    public static boolean syncPhone(Context context, long time) {
        synchronized (Call.class) {
            if (!PermissionTools.checkPermission(context, android.Manifest.permission.READ_CALL_LOG, "", true)) {
                return false;
            }
            Log.d("test", "syncPhone->:开始同步通话记录：");
            long lastSyncTime = SP.getLong(context, SP.LAST_PHONE_SYNC_TIME, 0);
            if (time > lastSyncTime) {
                Cursor phoneCursor = null;
                try {
                    phoneCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, "date>?", new String[]{String.valueOf(lastSyncTime)}, "date asc");
                    ArrayList<Call> callList = new ArrayList<>();
                    HashMap<String, String> contactMap = null;
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            Call call = new Call();
                            call.time = phoneCursor.getLong(phoneCursor.getColumnIndex("date"));
                            call.number = phoneCursor.getString(phoneCursor.getColumnIndex("number"));
                            if ("#".equals(call.number)) {
                                continue;
                            }
                            call.duration = phoneCursor.getLong(phoneCursor.getColumnIndex("duration"));
                            call.duration = call.duration < 0 ? 0 : call.duration;
                            call.type = phoneCursor.getInt(phoneCursor.getColumnIndex("type"));
                            if (contactMap == null) {
                                contactMap = DB.getContacts(context, null, true);
                            }
                            call.name = contactMap.get(call.number);
                            callList.add(call);
                        }
                    }
                    if (callList.size() > 0) {
                        insertCall(context, callList, 0, callList.size());
                        Log.d("test", "syncPhone->:同步了[" + callList.size() + "]条通话记录。");
                        SP.set(context, SP.LAST_PHONE_SYNC_TIME, callList.get(callList.size() - 1).time);
                        Log.d("test", "syncPhone->:update SP " + SP.LAST_PHONE_SYNC_TIME + " value->:" + (callList.get(callList.size() - 1).time));
                    }
                    Log.d("test", "syncPhone->:通话记录同步成功。");
                    return true;
                } catch (SecurityException e) {
                    Log.d("test", "syncPhone->:通话记录同步失败。");
                    return false;
                } finally {
                    if (phoneCursor != null) {
                        phoneCursor.close();
                    }
                }
            } else {
                Log.d("test", "syncPhone->:该时间点之前的通话记录已同步，无需再次同步。");
                return true;
            }
        }
    }

    /**
     * 根据时间查询某个时间段的通话记录
     *
     * @param context   context 对象
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 通话记录的集合
     */
    public static List<Call> selectCall(Context context, long startTime, long endTime) {
        // 同步系统通话记录
        syncPhone(context, endTime);

        // 向数据库中查询
        ArrayList<Call> callList = new ArrayList<>();

        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return callList;
            }
            Call call;
            String sql = "SELECT * FROM call";
            if (startTime > 0 && endTime > 0) {
                Log.e("test", "selectCall->startTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " ->endTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime));
                sql += " where time>=" + startTime + " and time<" + endTime;
            }
            sql += " order by time asc";
            Cursor c = null;
            try {
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    call = new Call();
                    call.id = c.getInt(c.getColumnIndex("_id"));
                    call.time = c.getLong(c.getColumnIndex("time"));
                    call.number = c.getString(c.getColumnIndex("number"));
                    call.duration = c.getLong(c.getColumnIndex("duration"));
                    call.type = c.getInt(c.getColumnIndex("type"));
                    callList.add(call);
                    Log.w("test", "DB selectCall select a call=>:" + call);
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
        return callList;
    }

    /**
     * 分组查询某个时间段之内的通话记录
     *
     * @param context   context 记录
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 按电话号码分组得到一个集合
     */
    public static List<Call> selectCallGroup(Context context, long startTime, long endTime) {
        // 向数据库中查询
        ArrayList<Call> callList = new ArrayList<>();

        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return callList;
            }
            Call call;
            String sql = "SELECT count(1) time,number,sum(duration) duration FROM call";
            if (startTime > 0 && endTime > 0) {
                Log.d("test", "selectCallGroup->startTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " ->endTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime));
                sql += " where time>=" + startTime + " and time<" + endTime;
            }
            sql += " group by number order by duration desc";
            Cursor c = null;
            try {
                HashMap<String, String> contactMap = null;
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    call = new Call();
                    call.time = c.getLong(c.getColumnIndex("time"));
                    call.number = c.getString(c.getColumnIndex("number"));
                    call.duration = c.getLong(c.getColumnIndex("duration"));
                    if (contactMap == null) {
                        contactMap = DB.getContacts(context, null, true);
                    }
                    call.name = contactMap.get(DB.handlePhoneNumber(call.number));
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
        return callList;
    }

}
