package com.rdcx.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;
import android.util.Log;

import com.rdcx.randian.MyApplication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/16 0016.
 * <p/>
 * 一条操作记录
 */
public class Operation {

    /**
     * 用户屏幕开启状态
     */
    public final static String SCREEN_ON = "ScreenOn";

    /**
     * ID
     */
    public int id;

    /**
     * 时间
     */
    public long time;

    /**
     * 应用程序
     */
    public String packageName;

    /**
     * 持续时间
     */
    public long duration;

    /**
     * 是否已经插入到数据库
     */
    public boolean isInsert = false;

    /**
     * 数据是否上传到服务器
     */
    public int upload;

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", time=" + DateFormat.format("yyyy-MM-dd HH:mm:ss", time) +
                ", packageName='" + packageName + '\'' +
                ", duration=" + duration +
                ", isInsert=" + isInsert +
                ", upload=" + upload +
                '}';
    }

    /**
     * 向 operation 表中插入多条数据
     * 数据保存在一个队列当中，对于该队列中的每一条数据，如果被标记为已插入数据库，则将数据库中的该条数据进行更新，如果未被标记，则直接在数据库中插入该条数据
     * 队列中的最后一条数据会继续保留在队列中并标记为已插入数据库，其它数据会被从该队列中移除
     *
     * @param context       Context
     * @param operationList 需要向 operation 表中插入的数据
     * @return 数据是否插入成功
     */
    public static boolean insertOperations(Context context, LinkedList<Operation> operationList) {
        if (context == null || operationList == null) {
            return false;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return false;
            }
            db.beginTransaction();
            try {
                // 非最后一条记录，移除队列并插入数据库中
                while (operationList.size() > 1) {
                    insertOperation(db, operationList.poll());
                }
                // 将最后一条插入到数据库中，并标记已插入数据库中
                if (operationList.size() > 0) {
                    insertOperation(db, operationList.get(0));
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                return false;
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return true;
    }

    /**
     * 向 Operation 表中插入或更新多条数据
     *
     * @param context       Context
     * @param operationList operationList
     * @return 是否成功
     */
    public static boolean insertOperationList(Context context, List<Operation> operationList, int startIndex, int count) {
        if (context == null || operationList == null) {
            return false;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return false;
            }
            db.beginTransaction();
            try {
                for (int i = startIndex, j = 0, len = operationList.size(); i < len && j < count; i++, j++) {
                    Operation operation = operationList.get(i);
                    insertOperation(db, operation);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                return false;
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        return true;
    }

    /**
     * 向数据库中插入或更新一条使用记录
     * 若记录被标记为已插入数据库，则进行更新操作
     * 若记录未被标记，则进行插入操作，并标记为已插入数据库
     *
     * @param db        SQLiteDatabase
     * @param operation 将要插入的 operation 对象
     */
    private static void insertOperation(SQLiteDatabase db, Operation operation) {
        if (operation == null) {
            return;
        }
        if (operation.time <= 0 || operation.packageName == null || operation.duration <= 0) {
            Log.d("test", "Find error in a operation=>:" + operation);
            return;
        }
        operation.time = (operation.time / 1000) * 1000;
        if (operation.id > 0) {
            db.execSQL("UPDATE operation set time=?,packageName=?,duration=?,upload=? where _id=?", new Object[]{operation.time, operation.packageName, operation.duration, operation.upload, operation.id});
            Log.d("test", "update a operation=>:" + operation);
        } else {
            int queryId = DB.getQueryInt(db, "select _id value from operation where time>=" + operation.time + " and time<" + (operation.time + 1000), null);
            if (queryId > 0) {
                operation.id = queryId;
                db.execSQL("UPDATE operation set time=?,packageName=?,duration=?,upload=? where _id=?", new Object[]{operation.time, operation.packageName, operation.duration, operation.upload, operation.id});
                Log.d("test", "update a operation=>:" + operation);
            } else {
                db.execSQL("INSERT INTO operation values(null,?,?,?,?)", new Object[]{operation.time, operation.packageName, operation.duration, operation.upload});
                Log.d("test", "insert a operation=>:" + operation);
            }
        }

    }

    /**
     * 同步应用使用数据
     *
     * @param context Context 对象
     */
    public static void syncOperation(Context context) {
        insertOperations(context, ((MyApplication) context.getApplicationContext()).getOperationList());
    }

    /**
     * 查询 operation 表
     *
     * @param context     Context 对象
     * @param startTime   起始时间点
     * @param endTime     结束时间
     * @param packageName 包名
     * @return 记录的集合
     */
    public static List<Operation> selectOperation(Context context, long startTime, long endTime, String packageName) {
        return selectOperation(context, startTime, endTime, packageName, null, null);
    }

    /**
     * 查询 operation 表
     *
     * @param context     Context 对象
     * @param startTime   起始时间点
     * @param endTime     结束时间点
     * @param packageName 包名
     * @param type        类型名
     * @return 符合条件的记录
     */
    public static List<Operation> selectOperation(Context context, long startTime, long endTime, String packageName, String type) {
        return selectOperation(context, startTime, endTime, packageName, type, null);
    }

    /**
     * 查询 operation 表
     *
     * @param context     Context 对象
     * @param startTime   起始时间点
     * @param endTime     结束时间
     * @param packageName 包名
     * @param type        类型名
     * @param upload      是否上传[0,1,null]
     * @return 记录的集合
     */
    public static List<Operation> selectOperation(Context context, long startTime, long endTime, String packageName, String type, String upload) {
        ArrayList<Operation> operationArrayList = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM operation where 1=1");
        if (startTime > 0 && endTime > 0) {
            sb.append(" and time>=").append(startTime).append(" and time<").append(endTime);
        }
        if (packageName != null) {
            sb.append(" and packageName in (");
            String[] packageNames = packageName.split(",");
            for (String str : packageNames) {
                if (str.length() > 0) {
                    sb.append("'").append(str).append("',");
                }
            }
            if (sb.lastIndexOf(",") > -1) {
                sb.deleteCharAt(sb.lastIndexOf(","));
            }
            sb.append(")");
        }
        if (type != null) {
            sb.append(" and packageName in (select packageName from app where type in (");
            String[] types = type.split(",");
            for (String str : types) {
                if (str.length() > 0) {
                    sb.append("'").append(str).append("',");
                }
            }
            if (sb.lastIndexOf(",") > -1) {
                sb.deleteCharAt(sb.lastIndexOf(","));
            }
            sb.append("))");
        }
        if (upload != null) {
            sb.append(" and upload=").append(upload);
        }
        sb.append(" order by time");
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return operationArrayList;
            }
            Cursor c = null;
            try {
                c = db.rawQuery(sb.toString(), null);
                while (c.moveToNext()) {
                    Operation operation = new Operation();
                    operation.id = c.getInt(c.getColumnIndex("_id"));
                    operation.time = c.getLong(c.getColumnIndex("time"));
                    operation.packageName = c.getString(c.getColumnIndex("packageName"));
                    operation.duration = c.getLong(c.getColumnIndex("duration"));
                    operation.upload = c.getInt(c.getColumnIndex("upload"));
                    if (operation.time <= 0 || operation.packageName == null) {
                        Log.w("test", "Find error in a operation=>:" + operation);
                    } else {
                        Log.w("test", "select a operation=>:" + operation);
                        operationArrayList.add(operation);
                    }
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
        return operationArrayList;
    }

    /**
     * 分组获取 operation 表中的使用记录
     *
     * @param context   Context对象
     * @param startTime 起始时间点
     * @param endTime   结束时间点
     * @return 按 packageName 分组获取总数据
     */
    public static List<Operation> selectOperationGroup(Context context, long startTime, long endTime) {
        ArrayList<Operation> operationArrayList = new ArrayList<>();
        String sql = "SELECT count(1) time,packageName,sum(duration) durations FROM operation";
        if (startTime > 0 && endTime > 0) {
            Log.d("test", "selectOperationGroup->startTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " ->endTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime));
            sql += " where time>=" + startTime + " and time<" + endTime;
        }
        sql += " group by packageName order by durations desc";
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return operationArrayList;
            }
            Cursor c = null;
            try {
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    Operation operation = new Operation();
                    operation.time = c.getInt(c.getColumnIndex("time"));
                    operation.packageName = c.getString(c.getColumnIndex("packageName"));
                    operation.duration = c.getLong(c.getColumnIndex("durations"));
                    Log.d("test", "select a operation:packageName>:" + operation.packageName + ",duration>:" + operation.duration);
                    operationArrayList.add(operation);
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
        return operationArrayList;
    }
}
