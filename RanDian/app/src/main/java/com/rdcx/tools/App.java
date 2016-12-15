package com.rdcx.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/3/21 0021.
 * <p/>
 * 应用分类表
 */
public class App {

    /**
     * ID
     */
    public int id;

    /**
     * 包名
     */
    public String packageName;

    /**
     * 应用名
     */
    public String name;

    /**
     * 应用图标
     */
    public String icon;

    /**
     * 应用分类
     */
    public String type;

    @Override
    public String toString() {
        return "App{" +
                "id=" + id +
                ", packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    /**
     * 向数据库中插入多条App对象
     *
     * @param context Context 对象
     * @param appList App对象集合
     */
    public static void insertOrUpdateAppList(Context context, List<App> appList) {
        if (appList == null) {
            Log.w("test", "App insertOrUpdateAppList warn in parameter appList=null;");
            return;
        }
        if (appList.size() == 0) {
            Log.w("test", "App insertOrUpdateAppList warn in parameter appList.size()=0;");
            return;
        }
        for (App app : appList) {
            insertOrUpdateApp(context, app);
        }
    }

    /**
     * 向数据库中插入一条App对象
     *
     * @param context Context 对象
     * @param app     App对象
     */
    public static void insertOrUpdateApp(Context context, App app) {
        if (app == null) {
            Log.w("test", "App insertOrUpdateApp warn in parameter app=null;");
            return;
        }
        if (app.packageName == null) {
            Log.w("test", "App insertOrUpdateApp warn in parameter app.packageName=null;");
            return;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return;
            }
            try {

                int count = DB.getQueryInt(db, "select count(*) value from app where packageName=?", new String[]{app.packageName});

                db.beginTransaction();
                if (count > 0) {
                    if (app.name != null && app.icon != null && app.type != null) {
                        db.execSQL("UPDATE app set name=?,icon=?,type=? where packageName=?", new Object[]{app.name, app.icon, app.type, app.packageName});
                    }
                    Log.d("test", "App insertOrUpdateApp update an app=>:" + app);
                } else {
                    db.execSQL("INSERT INTO app values(null,?,?,?,?)", new Object[]{app.packageName, app.name, app.icon, app.type});
                    Log.d("test", "App insertOrUpdateApp insert an app=>:" + app);
                }
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.w("test", "App insertOrUpdateApp occurs an Exception=>:" + e);
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 向数据库中删除一条App对象
     *
     * @param context Context 对象
     * @param app     App对象
     */
    public static void deleteApp(Context context, App app) {
        if (app == null) {
            Log.w("test", "App deleteApp warn in parameter app=null;");
            return;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return;
            }
            try {
                db.beginTransaction();
                db.execSQL("DELETE FROM app where packageName=?", new Object[]{app.packageName});
                Log.d("test", "App deleteApp delete an app packageName=>:" + app.packageName + ",name=>:" + app.name + ",icon=>:" + app.icon + ",type=>:" + app.type);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.w("test", "App deleteApp occurs an Exception=>:" + e);
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 根据类型查找数据库的应用及分类
     *
     * @param context Context 对象
     * @param type    所属类别，为null时查询type为空的App，为“~”时查询表中所有数据，多个类别用“,”号隔开
     * @return App数据的集合
     */
    public static List<App> selectApp(Context context, String type) {

        List<App> appList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("SELECT * FROM app where");
        if (type == null) {
            sb.append(" type is null");
        } else if ("~".equals(type)) {
            sb.append(" 1=1");
        } else {
            sb.append(" type in (");
            String[] types = type.split(",");
            for (String str : types) {
                if (str.length() > 0) {
                    sb.append("'").append(str).append("',");
                }
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append(")");
        }

        synchronized (DB.class) {
            SQLiteDatabase db = DB.getDb(context);
            if (db == null) {
                return appList;
            }
            Cursor c = null;
            try {
                c = db.rawQuery(sb.toString(), null);
                while (c.moveToNext()) {
                    App app = new App();
                    app.id = c.getInt(c.getColumnIndex("_id"));
                    app.packageName = c.getString(c.getColumnIndex("packageName"));
                    app.name = c.getString(c.getColumnIndex("name"));
                    app.icon = c.getString(c.getColumnIndex("icon"));
                    app.type = c.getString(c.getColumnIndex("type"));
                    appList.add(app);
                    Log.d("test", "App selectApp select an app packageName=>:" + app.packageName + ",name=>:" + app.name + ",icon=>:" + app.icon + ",type=>:" + app.type);
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
        return appList;
    }

    /**
     * 从服务器同步请求应用的分类，对未分类的应用进行分类
     *
     * @param context Context 对象
     * @param all     为 true ：对所有应用进行重新分类；为 false ：只对 type 为 null（即刚新增）的应用进行分类
     */
    public static void syncApp(Context context, boolean all) {
        if (all) {
            Log.d("test", "App syncApp 对所有应用进行重新分类。");
        } else {
            Log.d("test", "App syncApp 对 type 为 null（即刚新增）的应用进行分类。");
        }
        final List<App> appList = selectApp(context, all ? "~" : null);
        if (appList.size() == 0) {
            Log.d("test", "App syncApp 当前新增未分类的应用数为 0 ，不需要进行分类操作。");
            return;
        }
        HashMap<String, App> packageNameAppMap = new HashMap<>();
        StringBuilder datas = new StringBuilder();
        for (App app : appList) {
            packageNameAppMap.put(app.packageName, app);
            datas.append(app.packageName).append(",");
        }
        if (datas.lastIndexOf(",") > -1) {
            datas.deleteCharAt(datas.lastIndexOf(","));
        }

        JSONArray ja = DB.getDataInterface(context).getActivityByName(SP.getString(context, SP.USER_ID, ""), datas.toString());
        if (ja == null) {
            Log.w("test", "App syncApp 向网络请求应用分类时返回为空。");
            return;
        }
        try {
            JSONObject jo = ja.getJSONObject(0);
            if ("000000".equals(jo.getString("resp"))) {
                JSONArray activityList = jo.getJSONArray("activityList");
                for (int i = 0; i < activityList.length(); i++) {
                    JSONObject activity = activityList.getJSONObject(i);
                    String packageName = activity.getString("activityName");
                    App tempApp = packageNameAppMap.get(packageName);
                    tempApp.name = activity.getString("name").replaceAll("(^\\s*)|(\\s*$)|(\\(.*\\))", "");
                    tempApp.icon = activity.getString("icon");
                    tempApp.type = activity.getJSONObject("model").getString("name");
                }
            }
        } catch (JSONException e) {
            Log.w("test", "App syncApp 向服务器请求的数据格式不正确：" + e);
            return;
        }
        for (App app : appList) {
            if (app.type == null) {
                app.type = "未分类";
            }
        }
        for (App app : appList) {
            Log.d("test", "App syncApp app=>:" + app);
        }
        insertOrUpdateAppList(context, appList);
    }

}
