package com.rdcx.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2015/11/16 0016.
 * <p/>
 * 用于系统内数据库操作的一个类
 */
public class DB extends SQLiteOpenHelper {

    /**
     * 数据库所存文件的文件名
     */
    private static final String DATABASE_NAME = "test.db";

    /**
     * 数据库的版本号
     */
    private static final int DATABASE_VERSION = 10;

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    /**
     * 初始化数据库
     *
     * @param context Context 对象
     */
    public DB(Context context, String fileName) {
        super(context.getApplicationContext(), fileName, null, DATABASE_VERSION);
    }

    /**
     * 第一次新建数据库时会调用该方法，用来创建各表
     * operation表：用来保存抓取到的用户的数据，每条记录是一次使用记录
     *
     * @param db SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS operation (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,packageName VARCHAR,duration BIGINT,upload INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS location (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,longitude VARCHAR,latitude VARCHAR,upload INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS call (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,number VARCHAR,duration BIGINT,type INTEGER,upload INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS image (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,path VARCHAR,longitude VARCHAR,latitude VARCHAR,address VARCHAR,upload INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS ruletext (_id INTEGER PRIMARY KEY AUTOINCREMENT,type INTEGER,ruletype INTEGER,priority INTEGER,startdate BIGINT,enddate BIGINT,rulestartvalue BIGINT,ruleendvalue BIGINT,rulephotourl VARCHAR,keywords VARCHAR,rulename VARCHAR,ruletext VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS diary (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,text VARCHAR,path VARCHAR,data VARCHAR,upload INTEGER,datatext VARCHAR,nowDate BIGINT)");
        db.execSQL("CREATE INDEX index_operation_time_packageName ON operation(time,packageName);");
        db.execSQL("CREATE INDEX index_location_time ON location(time);");
        db.execSQL("CREATE INDEX index_call_time ON call(time);");
        db.execSQL("CREATE INDEX index_image_time ON image(time);");
        db.execSQL("CREATE INDEX index_ruletext_type_ruletype ON ruletext(type,ruletype);");
        db.execSQL("CREATE INDEX index_diary_time ON diary(time);");
        db.execSQL("CREATE TABLE IF NOT EXISTS app(_id INTEGER PRIMARY KEY AUTOINCREMENT,packageName VARCHAR UNIQUE,name VARCHAR,icon VARCHAR,type VARCHAR);");
        Log.d("test", "在数据库中新建各表");
    }

    /**
     * 数据库版本号发生变化时，会调用该方法
     *
     * @param db         SQLiteDatabase
     * @param oldVersion 老版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("test", "数据库更新了，从版本[" + oldVersion + "]更新至[" + newVersion + "]。");
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            if (i == 2) {
                db.execSQL("ALTER TABLE operation ADD COLUMN upload INTEGER");
                db.execSQL("CREATE TABLE IF NOT EXISTS location (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,longitude VARCHAR,latitude VARCHAR,upload INTEGER)");
            } else if (i == 3) {
                db.execSQL("CREATE TABLE IF NOT EXISTS call (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,number VARCHAR,duration BIGINT,type INTEGER,upload INTEGER)");
            } else if (i == 4) {
                db.execSQL("CREATE TABLE IF NOT EXISTS image (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,path VARCHAR,longitude VARCHAR,latitude VARCHAR,address VARCHAR,upload INTEGER)");
            } else if (i == 5) {
                db.execSQL("CREATE TABLE IF NOT EXISTS ruletext (_id INTEGER PRIMARY KEY AUTOINCREMENT,type INTEGER,ruletype INTEGER,priority INTEGER,startdate BIGINT,enddate BIGINT,rulestartvalue BIGINT,ruleendvalue BIGINT,rulephotourl VARCHAR,keywords VARCHAR,rulename VARCHAR,ruletext VARCHAR)");
            } else if (i == 6) {
                db.execSQL("CREATE TABLE IF NOT EXISTS diary (_id INTEGER PRIMARY KEY AUTOINCREMENT,time BIGINT,text VARCHAR,path VARCHAR,data VARCHAR,upload INTEGER)");
            } else if (i == 7) {
                db.execSQL("ALTER TABLE diary ADD COLUMN datatext VARCHAR");
            } else if (i == 8) {
                db.execSQL("CREATE INDEX index_operation_time_packageName ON operation(time,packageName);");
                db.execSQL("CREATE INDEX index_location_time ON location(time);");
                db.execSQL("CREATE INDEX index_call_time ON call(time);");
                db.execSQL("CREATE INDEX index_image_time ON image(time);");
                db.execSQL("CREATE INDEX index_ruletext_type_ruletype ON ruletext(type,ruletype);");
                db.execSQL("CREATE INDEX index_diary_time ON diary(time);");
            } else if (i == 9) {
                db.execSQL("CREATE TABLE IF NOT EXISTS app(_id INTEGER PRIMARY KEY AUTOINCREMENT,packageName VARCHAR UNIQUE,name VARCHAR,icon VARCHAR,type VARCHAR);");
            } else if (i == 10) {
                db.execSQL("ALTER TABLE diary ADD COLUMN nowDate BIGINT");
            }
        }
    }

    /**
     * 单例模式，DB 实例
     */
    private static DB instance = null;


    /**
     * 获取 DB 的实例
     *
     * @param context Context 对象
     * @return DB 的单例模式
     */
    public static DB instance(Context context, String userId) {
//        if (instance == null || (userId != null && !userId.equals(instance.getUserId()))) {
//            synchronized (DB.class) {
        if (instance == null || (userId != null && !userId.equals(instance.getUserId()))) {
//                    if (mdb != null) {
//                        mdb.close();
//                    }
            if (instance != null) {
                instance.close();
            }
            File testDataBase = context.getDatabasePath(DATABASE_NAME);
            File userDataBase = context.getDatabasePath(userId + DATABASE_NAME);
            boolean testDataBaseExists = context.getDatabasePath(DATABASE_NAME).exists();
            boolean userDataBaseExists = context.getDatabasePath(userId + DATABASE_NAME).exists();
            if (testDataBaseExists && !userDataBaseExists) {
                if (testDataBase.renameTo(userDataBase)) {
                    Log.w("test", "DB instance 数据库从【" + testDataBase.getPath() + "】迁移到【" + userDataBase.getPath() + "】时成功。");
                    instance = new DB(context, userDataBase.getName());
                    instance.setUserId(userId);
                } else {
                    Log.w("test", "DB instance 数据库从【" + testDataBase.getPath() + "】迁移到【" + userDataBase.getPath() + "】时失败。");
                    instance = new DB(context, testDataBase.getName());
                    instance.setUserId(userId);
                }
            } else {
                Log.w("test", "DB instance 数据库无需迁移。");
                instance = new DB(context, userDataBase.getName());
                instance.setUserId(userId);
            }
//                    mdb = instance.getWritableDatabase();
        }
//            }
//        }
        return instance;
    }

    private static SQLiteDatabase mdb = null;

    /**
     * 获取 SQLiteDatabase 的实例
     *
     * @param context Context 对象
     * @return 数据库实例对象
     */
    public static SQLiteDatabase getDb(Context context) {
        String userId = SP.getUserId(context);
        if (userId == null || userId.length() == 0) {
            return null;
        }
        DB dbHelper = instance(context, userId);
        if (mdb != null && mdb.isOpen()) {
            mdb.close();
        }
        mdb = dbHelper.getWritableDatabase();
        return mdb;
    }

    /**
     * 向网络请求数据接口的实例
     */
    private static DataInterface dataInterface;

    /**
     * 向网络请求数据接口的实例的单例模式
     *
     * @param context Context 对象
     * @return DataInterface 对象
     */
    public static DataInterface getDataInterface(Context context) {
        if (dataInterface == null) {
            dataInterface = new NetDataGetter(context.getApplicationContext());
        }
        return dataInterface;
    }

    /**
     * 从数据库中查询获取某个单一的值
     *
     * @param db            SQLiteDatabase
     * @param sql           SQL
     * @param selectionArgs 查询条件
     * @return 该值
     */
    public static long getQueryLong(SQLiteDatabase db, String sql, String[] selectionArgs) {
        long result = 0;
        if (db == null) {
            return result;
        }
        Cursor c = null;
        try {
            c = db.rawQuery(sql, selectionArgs);
            if (c.moveToFirst()) {
                result = c.getLong(c.getColumnIndex("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    public static boolean execSql(Context context, String sql, Object[] bindArgs) {
        SQLiteDatabase db = getDb(context);
        if (db == null) {
            return false;
        }
        try {
            db.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 从数据库中查询获取某个单一的值
     *
     * @param db            SQLiteDatabase
     * @param sql           SQL
     * @param selectionArgs 查询条件
     * @return 该值
     */
    public static int getQueryInt(SQLiteDatabase db, String sql, String[] selectionArgs) {
        int result = 0;
        if (db == null) {
            return result;
        }
        Cursor c = null;
        try {
            c = db.rawQuery(sql, selectionArgs);
            if (c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex("value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /**
     * 获取手机里所有的联系人信息：
     * key 为 手机号码
     * value 为 联系人姓名
     *
     * @param context Context
     * @return 联系人信息
     */
    public static HashMap<String, String> getContacts(Context context, String number, boolean numberOrTime) {
        HashMap<String, String> contactsMap = new HashMap<>();
        Cursor contactCursor = null;
        try {
            if (PermissionTools.checkPermission(context, android.Manifest.permission.READ_CONTACTS, "", true)) {
                if (number != null && number.length() > 0) {
                    contactCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.NUMBER + "=? ", new String[]{number}, null);
                } else {
                    contactCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                }
                if (contactCursor != null) {
                    while (contactCursor.moveToNext()) {
                        String phoneNumber = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (phoneNumber != null) {
                            phoneNumber = handlePhoneNumber(phoneNumber);

                            if (numberOrTime) {
                                String name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                contactsMap.put(phoneNumber, name);
                            } else {
                                long time = contactCursor.getLong(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP));
                                contactsMap.put(phoneNumber, String.valueOf(time));
                            }
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            Log.e("test", "获取联系人信息时失败。");
        } finally {
            if (contactCursor != null) {
                contactCursor.close();
            }
        }
        return contactsMap;
    }

    /**
     * 对手机号码进行简单的处理，以更好的匹配联系人姓名。
     *
     * @param phoneNumber 手机号码
     * @return 处理之后的手机号码
     */
    public static String handlePhoneNumber(String phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.replaceAll("[^\\d]", "").replaceAll("^86", ""); // 去掉所有非数字的字符，并且去掉前面的86
    }

    /**
     * @param context Context
     * @return 大账单数据
     */
    public static HashMap<String, String> getContactCount(Context context) {
        // 返回大账单数据
        HashMap<String, String> resultMap = new HashMap<>();
        // 系统中的联系人信息
        HashMap<String, String> contactMap = getContacts(context, null, true);
        // 2016年1月的起始时间点
        HashMap<String, Long> dbTime = getDBTime(TYPE_THIS_MONTH, 1452355200000L);
        long start = dbTime.get("start");
        long end = dbTime.get("end");
        // 各联系人通话记录总时间
        HashMap<String, Long> numberTimeMap = new HashMap<>();
        // 各时间段的通话时长
        HashMap<String, Long> hourMap = new HashMap<>();
        hourMap.put("上午", 0L);
        hourMap.put("中午", 0L);
        hourMap.put("下午", 0L);
        hourMap.put("晚上", 0L);
        hourMap.put("深夜", 0L);
        hourMap.put("凌晨", 0L);
        Calendar c = Calendar.getInstance();
        // 每天的通话时长
        HashMap<Integer, Long> dayMap = new HashMap<>();
        // 从数据库中查询出所有通话记录
        List<Call> callList = Call.selectCall(context, start, end);
        Call firstCall = null;
        long totalTime = 0;
        int strangeCall = 0;
        String maxStrangeNumber = "无";
        long maxStrangeCall = 0;
        for (Call call : callList) {
            totalTime += call.duration;
            if (firstCall == null && call.duration > 0) {
                firstCall = call;
            }
            if (numberTimeMap.get(call.number) == null) {
                numberTimeMap.put(call.number, call.duration);
            } else {
                numberTimeMap.put(call.number, numberTimeMap.get(call.number) + call.duration);
            }
            c.setTimeInMillis(call.time);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 6 && hour < 12) {
                hourMap.put("上午", hourMap.get("上午") + call.duration);
            } else if (hour >= 12 && hour < 14) {
                hourMap.put("中午", hourMap.get("中午") + call.duration);
            } else if (hour >= 14 && hour < 18) {
                hourMap.put("下午", hourMap.get("下午") + call.duration);
            } else if (hour >= 18 && hour < 24) {
                hourMap.put("晚上", hourMap.get("晚上") + call.duration);
            } else if (hour >= 0 && hour < 3) {
                hourMap.put("深夜", hourMap.get("深夜") + call.duration);
            } else if (hour >= 3 && hour < 6) {
                hourMap.put("凌晨", hourMap.get("凌晨") + call.duration);
            }
            int day = c.get(Calendar.DAY_OF_MONTH);
            if (dayMap.get(day) == null) {
                dayMap.put(day, call.duration);
            } else {
                dayMap.put(day, dayMap.get(day) + call.duration);
            }
            if (contactMap.get(handlePhoneNumber(call.number)) == null) {
                strangeCall++;
                if (maxStrangeCall < call.duration) {
                    maxStrangeCall = call.duration;
                    maxStrangeNumber = call.number;
                }
            }
        }
        // 1、	上个月，共通话xxx小时xxx分钟
        resultMap.put("totalTime", String.valueOf(totalTime));
        // 2、	上个月你与xxx缠绵最久，xxx分钟
        String numberTimeMaxNumber = null;
        long numberTimeMax = 0;
        for (String number : numberTimeMap.keySet()) {
            if (numberTimeMax < numberTimeMap.get(number)) {
                numberTimeMax = numberTimeMap.get(number);
                numberTimeMaxNumber = number;
            }
        }
        String nameTemp = contactMap.get(handlePhoneNumber(numberTimeMaxNumber));
        resultMap.put("numberTimeMax", String.valueOf(numberTimeMax));
        resultMap.put("numberTimeMaxNumber", nameTemp == null ? handlePhoneNumber(numberTimeMaxNumber) : nameTemp);
        // 3、	上个月大部分的通话发生在（早上、上午、下午、晚上、凌晨），（以时长统计），平均日通话xxx分钟。
        String maxHour = null;
        long maxHourTime = 0;
        for (String key : hourMap.keySet()) {
            if (maxHourTime < hourMap.get(key)) {
                maxHourTime = hourMap.get(key);
                maxHour = key;
            }
        }
        resultMap.put("maxHour", maxHour);
        resultMap.put("maxHourTime", String.valueOf(maxHourTime));
        resultMap.put("averageDayTime", String.valueOf(totalTime / ((dbTime.get("end") - dbTime.get("start")) / Constants.ONE_DAY)));
        // 4、	去年你的最后一次给了xxx，通话xxx分钟
        List<Call> lastCallList = Call.selectCall(context, start - Constants.ONE_DAY * 30, start);
        if (lastCallList.size() > 0) {
            Call lastCall = lastCallList.get(lastCallList.size() - 1);
            nameTemp = contactMap.get(handlePhoneNumber(lastCall.number));
            resultMap.put("lastCallName", nameTemp == null ? lastCall.number : nameTemp);
            resultMap.put("lastCallTime", String.valueOf(lastCall.duration));
        } else {
            resultMap.put("lastCallName", "无");
            resultMap.put("lastCallTime", "0");
        }
        // 5、	今年你的第一次给了xxx,通话xx分钟
        if (firstCall == null) {
            resultMap.put("firstCallName", "无");
            resultMap.put("firstCallTime", "0");
        } else {
            nameTemp = contactMap.get(handlePhoneNumber(firstCall.number));
            resultMap.put("firstCallName", nameTemp == null ? firstCall.number : nameTemp);
            resultMap.put("firstCallTime", String.valueOf(firstCall.duration));
        }
        // 6、	xxx号这一天，电话粥煲最久xxx分钟
        int maxDay = 1;
        long maxDayTime = 0;
        for (Integer day : dayMap.keySet()) {
            if (maxDayTime < dayMap.get(day)) {
                maxDayTime = dayMap.get(day);
                maxDay = day;
            }
        }
        resultMap.put("maxDayTime", String.valueOf(maxDayTime));
        resultMap.put("maxDay", String.valueOf(maxDay));
        // 7、	上个月我的通话打败全国xxx%的人
        resultMap.put("beatPercent", String.valueOf(totalTime * 1.0F / (60 * 60 * 20)));
        // 8、	上个月陌生电话占比xxx%，最长一通来自xxxxxx，猜猜Ta是谁？
        resultMap.put("strangeCall", strangeCall == 0 ? "0" : String.valueOf(strangeCall * 1.0F / callList.size()));
        resultMap.put("maxStrangeNumber", maxStrangeNumber);
        // 9、	常联系前三名：xxx；xxx；xxx。（以时长统计）
        String oftenContact = "";
        for (int i = 0; i < 3; i++) {
            String maxNumber = null;
            long max = 0;
            for (String number : numberTimeMap.keySet()) {
                if (max < numberTimeMap.get(number)) {
                    max = numberTimeMap.get(number);
                    maxNumber = number;
                }
            }
            numberTimeMap.remove(maxNumber);
            String maxTemp = contactMap.get(handlePhoneNumber(maxNumber));
            oftenContact += "," + (maxTemp == null ? handlePhoneNumber(maxNumber) : maxTemp);
        }
        resultMap.put("oftenContact", oftenContact);

        System.out.println(resultMap);

        return resultMap;
    }

    /**
     * 向 image 表中插入多条数据
     *
     * @param context       Context
     * @param imageInfoList 需要向 image 表中插入的数据
     * @return 数据是否插入成功
     */
    public static boolean insertImageInfos(Context context, ArrayList<ImageInfo> imageInfoList) {
        if (context == null || imageInfoList == null) {
            return false;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = getDb(context);
            if (db == null) {
                return false;
            }
            try {
                db.beginTransaction();
                for (ImageInfo imageInfo : imageInfoList) {
                    if (imageInfo.id > 0) {
                        db.execSQL("UPDATE image set upload=? where _id=?", new Object[]{imageInfo.upload, imageInfo.id});
                        Log.d("test", "update image upload->:" + imageInfo.upload + " id->:" + imageInfo.id);
                    } else {
                        db.execSQL("INSERT INTO image values(null,?,?,?,?,?,0)", new Object[]{imageInfo.time, imageInfo.path, imageInfo.longitude, imageInfo.latitude, imageInfo.address});
                        Log.d("test", "insertImageInfos into image time->:" + imageInfo.time + " path->:" + imageInfo.path + " longitude->:" + imageInfo.longitude + " latitude->:" + imageInfo.latitude + " address->:" + imageInfo.address);
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
        }
        return true;
    }

    /**
     * 同步图库的图片记录到本地数据库
     *
     * @param context context 对象
     * @param time    需要同步到的时间点
     * @return 是否同步成功
     */
    public static boolean syncImage(Context context, long time) {
        synchronized (ImageInfo.class) {

            Log.d("test", "syncImage->:开始同步图片信息：");

            long lastSyncTime = SP.getLong(context, SP.LAST_IMAGE_SYNC_TIME, 0);
            if (time > lastSyncTime * 1000) {
                Cursor imageCursor = null;
                try {
                    imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns.DATE_MODIFIED + ">?", new String[]{lastSyncTime + ""}, MediaStore.MediaColumns.DATE_MODIFIED + " asc");
                    ArrayList<ImageInfo> imageInfoList = new ArrayList<>();
                    if (imageCursor != null) {
                        while (imageCursor.moveToNext()) {

                            long date = imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
                            String path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.MediaColumns.DATA));

                            ExifInterface ef = new ExifInterface(path);
                            // 照片时间
//                            String datetime = ef.getAttribute(ExifInterface.TAG_DATETIME);

                            Location location;
                            double longitude = 0, latitude = 0;
                            String address = "";
                            if ((location = exifLoc(ef)) != null) {
                                // 经度
                                longitude = location.getLongitude();
                                // 纬度
                                latitude = location.getLatitude();
                                address = getAddress(context, latitude, longitude);
                            }

                            if (longitude != 0 && latitude != 0) {
                                ImageInfo imageInfo = new ImageInfo();
                                imageInfo.time = date * 1000;
                                imageInfo.path = path;
                                imageInfo.longitude = longitude;
                                imageInfo.latitude = latitude;
                                imageInfo.address = address;
                                imageInfoList.add(imageInfo);

                                Log.d("test", "syncImage->:查找图片的位置信息 path=>:" + path + " longitude=>:" + longitude + " latitude=>:" + latitude);
                            }

                        }
                    }
                    if (imageInfoList.size() > 0) {
                        insertImageInfos(context, imageInfoList);
                        Log.d("test", "syncImage->:同步了[" + imageInfoList.size() + "]条图片信息。");
                        SP.set(context, SP.LAST_IMAGE_SYNC_TIME, imageInfoList.get(imageInfoList.size() - 1).time / 1000);
                        Log.d("test", "syncImage->:update SP " + SP.LAST_IMAGE_SYNC_TIME + " value->:" + (imageInfoList.get(imageInfoList.size() - 1).time) / 1000);
                    }
                    Log.d("test", "syncImage->:照片记录同步成功。");
                    return true;
                } catch (Exception e) {
                    Log.d("test", "syncImage->:照片记录同步失败。", e);
                    return false;
                } finally {
                    if (imageCursor != null) {
                        imageCursor.close();
                    }
                }
            } else {
                Log.d("test", "syncImage->:该时间点之前的通话记录已同步，无需再次同步。");
                return true;
            }
        }
    }

    /**
     * 根据时间查询某个时间段的照片记录
     *
     * @param context   context 对象
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 通话记录的集合
     */
    public static List<ImageInfo> selectImageInfo(Context context, long startTime, long endTime) {
        // 向数据库中查询
        ArrayList<ImageInfo> imageInfoList = new ArrayList<>();
        ImageInfo imageInfo;
        String sql = "SELECT * FROM image";
        if (startTime > 0 && endTime > 0) {
            Log.d("test", "selectImageInfo->startTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", startTime) + " ->endTime:" + DateFormat.format("yyyy-MM-dd HH:mm:ss", endTime));
            sql += " where time>=" + startTime + " and time<" + endTime;
        }
        sql += " order by time asc";

        synchronized (DB.class) {
            SQLiteDatabase db = getDb(context);
            if (db == null) {
                return imageInfoList;
            }
            Cursor c = null;
            try {
                c = db.rawQuery(sql, null);
                while (c.moveToNext()) {
                    imageInfo = new ImageInfo();
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
        return imageInfoList;
    }

    /**
     * 同步文案
     *
     * @param context Context 对象
     */
    public static void syncText(final Context context) {
        Log.d("test", "DB syncText 开始查询数据的时间戳。");
        getDataInterface(context).timeStamp("1", new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if ("000000".equals(jo.getString("resp"))) {
                        JSONArray modelList = jo.getJSONArray("modelList");
                        for (int i = 0; i < modelList.length(); i++) {
                            JSONObject model = modelList.getJSONObject(i);
                            int type = model.getInt("type");
                            long netTimeStamp = model.getJSONObject("updateDate").getLong("time");
                            if (type == 1) {
                                if (netTimeStamp != SP.getLong(context, SP.LOCAL_TEXT_TIME_STAMP, 0)) {
                                    Log.d("test", "DB syncText 本地时间戳不是最新，开始同步文案：");
                                    realSyncRuleText(context, netTimeStamp);
                                }
                            } else if (type == 2) {
                                if (netTimeStamp != SP.getLong(context, SP.FLASH_PAGE_TIME_STAMP, 0)) {
                                    Log.d("test", "DB syncText 本地时间戳不是最新，开始同步闪屏页菜单：");
                                    realSyncFlashPage(context, netTimeStamp);
                                }
                            } else if (type == 3) {
                                if (netTimeStamp != SP.getLong(context, SP.PUSH_RULE_TIME_STAMP, 0)) {
                                    Log.d("test", "DB syncText 本地时间戳不是最新，开始同步推送规则：");
                                    realSyncPushRule(context, netTimeStamp);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("test", "DB syncText 向服务请求时返回值不正确->:" + e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("test", "DB syncText 向服务器请求时出错->:" + volleyError.getCause());
                volleyError.printStackTrace();
            }
        });

    }

    /**
     * 向服务器请求文案，并进行同步操作
     *
     * @param context      Context
     * @param netTimeStamp 服务器时间戳
     */
    private static void realSyncRuleText(final Context context, final long netTimeStamp) {
        getDataInterface(context).getRuleText(new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                Log.d("test", "jsonArray->:" + jsonArray);
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if ("000000".equals(jo.getString("resp"))) {
                        JSONArray modelList = jo.getJSONArray("modelList");
                        ArrayList<RuleText> ruleTextArrayList = new ArrayList<>();
                        for (int i = 0, len = modelList.length(); i < len; i++) {
                            JSONObject model = modelList.getJSONObject(i);
                            RuleText ruleText = new RuleText();
                            ruleText._id = model.getInt("id");
                            ruleText.type = model.getInt("type");
                            ruleText.ruleType = model.getInt("ruleType");
                            ruleText.priority = model.getInt("priority");
                            ruleText.startDate = JSONGetTime(model, "startDate");
                            ruleText.endDate = JSONGetTime(model, "endDate");
                            ruleText.ruleStartValue = model.getLong("ruleStartValue");
                            ruleText.ruleEndValue = model.getLong("ruleEndValue");
                            ruleText.rulePhotoUrl = model.getString("rulePhotoUrl");
                            ruleText.keywords = model.getString("keywords");
                            ruleText.ruleName = model.getString("ruleName");
                            ruleText.ruleText = model.getString("ruleText");
                            ruleTextArrayList.add(ruleText);
                        }
                        insertRuleText(context, ruleTextArrayList, netTimeStamp, true);
                    }
                } catch (Exception e) {
                    Log.e("test", "向服务请求时返回值不正确->:" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("test", "向服务器请求时出错->:" + volleyError.getCause());
            }
        });
    }

    /**
     * 同步菜单
     *
     * @param context      Context
     * @param netTimeStamp 服务器时间戳
     */
    private static void realSyncFlashPage(final Context context, final long netTimeStamp) {
        getDataInterface(context).flashPage(SP.getString(context, SP.USER_ID, ""), new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if ("000000".equals(jo.getString("resp"))) {
                        JSONArray models = jo.getJSONArray("modelList");
                        SP.set(context, SP.FLASH_PAGE, models.toString());
                        SP.set(context, SP.FLASH_PAGE_TIME_STAMP, netTimeStamp);
                    }
                } catch (Exception e) {
                    Log.e("test", "向服务请求时返回值不正确->:" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("test", "向服务器请求时出错->:" + volleyError.getCause());
            }
        });
    }

    /**
     * 向服务器同步推送规则
     *
     * @param context      Context
     * @param netTimeStamp 服务器时间戳
     */
    private static void realSyncPushRule(final Context context, final long netTimeStamp) {
        getDataInterface(context).pushRule(SP.getString(context, SP.USER_ID, ""), new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if ("000000".equals(jo.getString("resp"))) {
                        JSONArray models = jo.getJSONArray("modelList");
                        SP.set(context, SP.PUSH_RULE, models.toString());
                        SP.set(context, SP.PUSH_RULE_TIME_STAMP, netTimeStamp);
                    }
                } catch (Exception e) {
                    Log.e("test", "向服务请求时返回值不正确->:" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("test", "向服务器请求时出错->:" + volleyError.getCause());
            }
        });
    }

    /**
     * 向 call 表中插入多条数据
     *
     * @param context      Context
     * @param ruleTextList 需要向 call 表中插入的数据
     * @return 数据是否插入成功
     */
    public static boolean insertRuleText(Context context, List<RuleText> ruleTextList, long netTimeStamp, boolean deleteAll) {
        if (context == null || ruleTextList == null) {
            return false;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = getDb(context);
            if (db == null) {
                return false;
            }
            try {
                db.beginTransaction();
                if (deleteAll) {
                    db.execSQL("DELETE from ruleText;");
                }
                for (RuleText ruleText : ruleTextList) {
                    db.execSQL("INSERT INTO ruleText values(?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{ruleText._id, ruleText.type, ruleText.ruleType, ruleText.priority, ruleText.startDate, ruleText.endDate, ruleText.ruleStartValue, ruleText.ruleEndValue, ruleText.rulePhotoUrl, ruleText.keywords, ruleText.ruleName, ruleText.ruleText});
                    Log.d("test", "insertRuleText into ruletext ->:" + ruleText.toString());
                }
                db.setTransactionSuccessful();
                SP.set(context, SP.LOCAL_TEXT_TIME_STAMP, netTimeStamp);
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
     * 查询文案
     *
     * @param context context 对象
     * @return 文案
     */
    public static String selectRuleText(Context context, int ruleType, int dimensionType, long value, String defaultText) {
        String ruleText = defaultText == null ? "文案加载失败了，小主请稍后再试。" : defaultText;
        synchronized (DB.class) {
            SQLiteDatabase db = getDb(context);
            if (db == null) {
                return ruleText;
            }
            String sql = "SELECT * FROM ruletext where ruletype=" + ruleType + " and type=" + dimensionType + " and rulestartvalue<=" + value + " and ruleendvalue>=" + value + " order by priority asc";
            Cursor c = null;
            try {
                c = db.rawQuery(sql, null);
                ArrayList<String> ruleTextList = new ArrayList<>();
                while (c.moveToNext()) {
                    ruleTextList.add(c.getString(c.getColumnIndex("ruletext")));
                }
                if (ruleTextList.size() > 0) {
                    ruleText = ruleTextList.get((int) Math.floor(Math.random() * ruleTextList.size()));
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
        ruleText = ruleText.replaceAll("\\[nick\\]", SP.getString(context, "nickName", "手机"));
        ruleText = ruleText.replaceAll("\\[count\\]", value + "");
        return ruleText;
    }

    /**
     * 插入日记
     *
     * @param context Context
     * @param diary   日记
     * @return 插入是否成功
     */
    public static boolean insertOrUpdateDiary(Context context, Diary diary) {
        if (diary == null) {
            return false;
        }
        synchronized (DB.class) {
            SQLiteDatabase db = getDb(context);
            if (db == null) {
                return false;
            }
            try {
                db.beginTransaction();
                if (diary.id > 0) {
                    db.execSQL("UPDATE diary set time=?,text=?,path=?,data=?,upload=?,datatext=?,nowDate=? where _id=?", new Object[]{diary.time, diary.text, diary.path, diary.data, diary.upload, diary.datatext, diary.nowDate, diary.id});
                    Log.d("test", "DB insertOrUpdateDiary update a " + diary);
                } else {
                    db.execSQL("INSERT INTO diary values(null,?,?,?,?,?,?,?)", new Object[]{diary.time, diary.text, diary.path, diary.data, diary.upload, diary.datatext, diary.nowDate});
                    diary.id = getQueryInt(db, "select _id value from diary where time=?", new String[]{String.valueOf(diary.time)});
                    Log.d("test", "DB insertOrUpdateDiary insert a " + diary);
                }
                db.setTransactionSuccessful();
                return diary.id > 0;
            } catch (Exception e) {
                return false;
            } finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * 查询日记
     *
     * @param context   Context
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 日记集合
     */
    public static List<Diary> selectDiary(Context context, long startTime, long endTime) {
        List<Diary> diaryList = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM diary where 1=1");
        if (startTime > 0 && endTime > 0) {
            sb.append(" and time>=").append(startTime).append(" and time<").append(endTime);
        }
        sb.append(" order by time desc");
        synchronized (DB.class) {
            SQLiteDatabase db = getDb(context);
            if (db == null) {
                return diaryList;
            }
            Cursor c = null;
            try {
                c = db.rawQuery(sb.toString(), null);
                while (c.moveToNext()) {
                    Diary diary = new Diary();
                    diary.id = c.getInt(c.getColumnIndex("_id"));
                    diary.time = c.getLong(c.getColumnIndex("time"));
                    diary.text = c.getString(c.getColumnIndex("text"));
                    diary.path = c.getString(c.getColumnIndex("path"));
                    diary.data = c.getString(c.getColumnIndex("data"));
                    diary.upload = c.getInt(c.getColumnIndex("upload"));
                    diary.datatext = c.getString(c.getColumnIndex("datatext"));
                    diary.nowDate = c.getLong(c.getColumnIndex("nowDate"));
                    diaryList.add(diary);
                    Log.d("test", "DB selectDiary select a " + diary);
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
        return diaryList;
    }

    /**
     * 获取各数据维度的数据
     *
     * @param context Context
     * @param map     维度
     */
    public static void findDimension(Context context, HashMap<Integer, String> map, int timeType) {

        long curTime = System.currentTimeMillis();
        HashMap<String, Long> dbTime = getDBTime(timeType, curTime);
        long start = dbTime.get("start");
        long end = dbTime.get("end");

        int sum;

        if (map.containsKey(0) || map.containsKey(1) || map.containsKey(7)) {
            Call.syncPhone(context, curTime);
        }
        if (map.containsKey(2)) {
            DB.syncImage(context, curTime);
        }
        if (map.containsKey(3) || map.containsKey(4) || map.containsKey(6) || map.containsKey(7)) {
            Operation.syncOperation(context);
        }

        for (Integer key : map.keySet()) {
            switch (key) {
                case 0:
                    List<Call> callList = Call.selectCall(context, start, end);
                    sum = 0;
                    for (Call call : callList) {
                        sum += call.duration;
                    }
                    map.put(key, String.valueOf(sum));
                    break;
                case 1:
                    List<Call> callGroupList = Call.selectCallGroup(context, start, end);
                    sum = 0;
                    for (Call call : callGroupList) {
                        sum += call.time;
                    }
                    map.put(key, String.valueOf(sum));
                    break;
                case 2:
                    List<ImageInfo> imageInfoList = DB.selectImageInfo(context, start, end);
                    map.put(key, String.valueOf(imageInfoList.size()));
                    break;
                case 3:
                    List<Operation> socialAppsList = Operation.selectOperation(context, start, end, null, "社交");
                    sum = 0;
                    for (Operation operation : socialAppsList) {
                        sum += operation.duration / 1000;
                    }
                    map.put(key, String.valueOf(sum));
                    break;
                case 4:
                    List<Operation> shoppingAppsList = Operation.selectOperation(context, start, end, null, "购物");
                    sum = 0;
                    for (Operation operation : shoppingAppsList) {
                        sum += operation.duration / 1000;
                    }
                    map.put(key, String.valueOf(sum));
                    break;
                case 5:
                    List<HashMap<String, Object>> locationMapList = LocationInfo.selectLocation(context, start, end);
                    if (locationMapList.size() > 0) {
                        List<com.rdcx.loction.Location> locationList = new ArrayList<>();
                        for (HashMap<String, Object> locationMap : locationMapList) {
                            locationList.add(new com.rdcx.loction.Location(Double.parseDouble((String) locationMap.get("longitude")), Double.parseDouble((String) locationMap.get("latitude"))));
                        }
                        com.rdcx.loction.Location.getAll(context);
                        map.put(key, String.valueOf(com.rdcx.loction.Location.getCitySize(locationList)));
                    } else {
                        map.put(key, "0");
                    }
                    break;
                case 6:
                    List<Operation> operationGroupList = Operation.selectOperationGroup(context, start, end);
                    sum = 0;
                    for (Operation operation : operationGroupList) {
                        if (!Operation.SCREEN_ON.equals(operation.packageName)) {
                            sum++;
                        }
                    }
                    map.put(key, String.valueOf(sum));
                    break;
                case 7:
                    List<Operation> timeAxisOperationList = Operation.selectOperation(context, start, end, null);
                    sum = 0;
                    for (Operation operation : timeAxisOperationList) {
                        if (!Operation.SCREEN_ON.equals(operation.packageName)) {
                            sum += operation.duration / 1000;
                        }
                    }
                    List<Call> timeAxisCallList = Call.selectCall(context, start, end);
                    for (Call call : timeAxisCallList) {
                        sum += call.duration;
                    }
                    map.put(key, String.valueOf(sum));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 从 JSONObject的 时间戳对象 中取出时间戳字段
     *
     * @param jo  包含时间对象的一个JOSN对象
     * @param key 该时间对象的键名
     * @return 时间戳
     */

    private static long JSONGetTime(JSONObject jo, String key) {
        try {
            JSONObject dateJSON = jo.getJSONObject(key);
            return dateJSON.getLong("time");
        } catch (Exception e) {
            return 0L;
        }
    }

    // 当天的 0:00 至 明天的 0:00
    public static final int TYPE_TODAY = 1;

    // 这周的  周一0:00-周日24:00
    public static final int TYPE_THIS_WEEK = 2;

    // 上周的  周一0:00-周日24:00
    public static final int TYPE_LAST_WEEK = 3;

    // 本月1号的 0:00 至 下月1号的 0:00
    public static final int TYPE_THIS_MONTH = 4;

    // 本年1月1号的 0:00 至 下年1月1号的 0:00
    public static final int TYPE_THIS_YEAR = 5;

    // 本小时的 00:00:000 - 下小时的 00:00:000
    public static final int TYPE_THIS_HOUR = 6;

    //昨天的 0:00 至 今天的 0:00
    public static final int TYPE_YESTERDAY = 7;

    /**
     * 根据 type 的值的不同返回起始时间和结束时间
     *
     * @param type 生成的时间类型
     * @param time 时间点
     * @return 包含起始时间和结束时间的一个HashMap
     */
    public static HashMap<String, Long> getDBTime(int type, long time) {
        HashMap<String, Long> timeMap = new HashMap<>();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int dayOfWeek;

        // type 为 1 ，获取当天时间 0:00 至 24:00
        switch (type) {

            case TYPE_TODAY:
                c.set(Calendar.HOUR_OF_DAY, 0);
                timeMap.put("start", c.getTimeInMillis());

                c.add(Calendar.DAY_OF_MONTH, 1);
                timeMap.put("end", c.getTimeInMillis());
                break;

            case TYPE_THIS_WEEK:
                c.set(Calendar.HOUR_OF_DAY, 0);
                dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek > 1) {
                    c.add(Calendar.DAY_OF_MONTH, 2 - dayOfWeek);
                } else {
                    c.add(Calendar.DAY_OF_MONTH, -6);
                }
                timeMap.put("start", c.getTimeInMillis());

                c.add(Calendar.DAY_OF_MONTH, 7);
                timeMap.put("end", c.getTimeInMillis());
                break;

            case TYPE_LAST_WEEK:
                c.set(Calendar.HOUR_OF_DAY, 0);
                dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek > 1) {
                    c.add(Calendar.DAY_OF_MONTH, 2 - dayOfWeek);
                } else {
                    c.add(Calendar.DAY_OF_MONTH, -6);
                }
                timeMap.put("end", c.getTimeInMillis());

                c.add(Calendar.DAY_OF_MONTH, -7);
                timeMap.put("start", c.getTimeInMillis());
                break;

            case TYPE_THIS_MONTH:
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.DAY_OF_MONTH, 1);
                timeMap.put("start", c.getTimeInMillis());

                c.add(Calendar.MONTH, 1);
                timeMap.put("end", c.getTimeInMillis());
                break;

            case TYPE_THIS_YEAR:
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MONTH, 0);
                c.set(Calendar.DAY_OF_MONTH, 1);
                timeMap.put("start", c.getTimeInMillis());

                c.add(Calendar.YEAR, 1);
                timeMap.put("end", c.getTimeInMillis());
                break;

            case TYPE_THIS_HOUR:
                timeMap.put("start", c.getTimeInMillis());

                c.add(Calendar.HOUR, 1);
                timeMap.put("end", c.getTimeInMillis());
                break;

            case TYPE_YESTERDAY:
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.add(Calendar.DAY_OF_MONTH, -1);
                timeMap.put("start", c.getTimeInMillis());

                c.add(Calendar.DAY_OF_MONTH, 1);
                timeMap.put("end", c.getTimeInMillis());
                break;

            default:
                timeMap.put("start", time);
                timeMap.put("end", time);
                break;
        }
        return timeMap;
    }

    /**
     * 获取某一个时间点的特定时间段
     *
     * @param type 时间段的类型
     * @param date 时间点
     * @return 该特定的时间点的时间段的起始时间点和结束时间点
     */
    public static HashMap<String, Long> getDBTime(int type, Date date) {
        return getDBTime(type, date.getTime());
    }

    /**
     * 获取某一个时间点的特定时间段
     *
     * @param type 时间段的类型
     * @return 该特定的时间点的时间段的起始时间点和结束时间点
     */
    public static HashMap<String, Long> getDBTime(int type) {
        return getDBTime(type, System.currentTimeMillis());
    }

    /**
     * 获取坐标点
     * 经纬度得到的数据格式是 "num1/denom1,num2/denom2,num3/denom3"，如何得到真正的经纬度呢？
     *
     * @param ef ExifInterface
     * @return Location
     */
    public static Location exifLoc(ExifInterface ef) {
        String sLat, sLatR, sLon, sLonR;
        try {
            sLat = ef.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            sLon = ef.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            sLatR = ef.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            sLonR = ef.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        } catch (Exception e) {
            Log.e("my_log", "经纬度异常");
            return null;
        }
        if (sLat == null || sLon == null || sLat.equals("") || sLon.equals("")
                || sLatR == null || sLonR == null) {
            return null;
        }
        double lat = dms2Dbl(sLat);
        if (lat > 180.0)
            return null;
        double lon = dms2Dbl(sLon);
        if (lon > 180.0)
            return null;
        lat = sLatR.contains("S") ? -lat : lat;
        lon = sLonR.contains("W") ? -lon : lon;
        Location loc = new Location("exif");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        return loc;
    }

    // 换算正常经纬度
    public static double dms2Dbl(String sDMS) {
        double dRV = 999.0;
        try {
            String[] DMSs = sDMS.split(",", 3);
            String s[] = DMSs[0].split("/", 2);
            dRV = (Double.valueOf(s[0]) / Double.valueOf(s[1]));
            s = DMSs[1].split("/", 2);
            dRV += ((Double.valueOf(s[0]) / Double.valueOf(s[1])) / 60);
            s = DMSs[2].split("/", 2);
            dRV += ((Double.valueOf(s[0]) / Double.valueOf(s[1])) / 3600);
        } catch (Exception e) {
            Log.e("my_log", "换算正常经纬度异常");
        }
        return dRV;
    }

    // 根据经纬度获取地址
    public static String getAddress(Context context, double latitude, double longitude) {
        Geocoder gc = new Geocoder(context, Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        try {
            List<Address> add = gc.getFromLocation(latitude, longitude, 1);
            if (add.size() > 0) {
                Address ad = add.get(0);
                sb.append(ad.getAddressLine(0));//省市
                sb.append(ad.getAddressLine(1));//区
                //sb.append(ad.getAddressLine(2));
            }
        } catch (Exception e) {
            Log.e("my_log", "根据经纬度获取地址异常");
            return "";
        }
        return sb.toString();
    }

    /**
     * 通过秒数来计算小时数分钟数和秒数
     *
     * @param duration 秒数
     * @return 时间字符串
     */
    public static String getTimeBySecond(long duration) {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        String result = "";
        if (hours > 0) {
            result += hours + "小时";
        }
        if (minutes > 0) {
            result += minutes + "分";
        }
        if (seconds > 0 || result.length() == 0) {
            result += seconds + "秒";
        }
        return result;
    }

    /**
     * 通过秒数来计算小时数分钟数和秒数
     *
     * @param duration 秒数
     * @return 时间字符串
     */
    public static String getTimeByRank(long duration) {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        String result = "";
        if (hours > 0) {
            result += hours + "小时";
        }
        if (minutes > 0) {
            result += minutes + "分";
        }
        if (hours <= 0 && minutes <= 0) {
            result += seconds + "秒";
        }
        return result;
    }
}
