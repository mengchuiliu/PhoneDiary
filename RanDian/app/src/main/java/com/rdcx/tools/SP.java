package com.rdcx.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.NetManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/26 0026.
 * <p/>
 * 用于在系统内存储少量数据
 */
public class SP {

    // SP 的名字
    private static final String SP_NAME = "UserInfo";

    // 是否有读取用户当前正在运行的应用的权限 的 key 值。
    public static final String USAGE_PERMISSION = "usagePermission";

    // 是否能获取到用户当前正在运行的应用 的 key 值
    public static final String USAGE_GET = "usageGet";

    // 用户 ID
    public static final String USER_ID = "userId";

    // 账户绑定状态
    public static final String BIND_STATE = "BIND_STATE";

    // 上次通话记录同步时间
    public static final String LAST_PHONE_SYNC_TIME = "LAST_PHONE_SYNC_TIME";

    // 上次照片记录同步时间
    public static final String LAST_IMAGE_SYNC_TIME = "LAST_IMAGE_SYNC_TIME";

    // 本地文案时间戳
    public static final String LOCAL_TEXT_TIME_STAMP = "LOCAL_TEXT_TIME_STAMP";

    // 已从网络中 下载的 应用使用数据 的时间的集合。
    public static final String DOWNLOAD_OPERATION_TIME = "DOWNLOAD_OPERATION_TIME_";

    public static final String DOWNLOAD_GPS_TIME = "DOWNLOAD_GPS_TIME_";

    public static final String DOWNLOAD_PHONE_TIME = "DOWNLOAD_PHONE_TIME_";

    // 闪屏页的数据
    public static final String FLASH_PAGE = "FLASH_PAGE";

    // 闪屏页数据的时间戳
    public static final String FLASH_PAGE_TIME_STAMP = "FLASH_PAGE_TIME_STAMP";

    // 推送规则的数据
    public static final String PUSH_RULE = "PUSH_RULE";

    // 任务清单，阶段任务，当前所处的阶段
    public static final String TASK_GUIDE_PARSE = "TASK_GUIDE_PARSE";

    // 任务清单，阶段任务，当前所处的阶段的 ID
    public static final String TASK_GUIDE_PARSE_ID = "TASK_GUIDE_PARSE_ID";

    // 任务清单，阶段任务，当前所处的阶段的内容
    public static final String TASK_GUIDE_PARSE_CONTENT = "TASK_GUIDE_PARSE_CONTENT";

    // 推送规则的时间戳
    public static final String PUSH_RULE_TIME_STAMP = "PUSH_RULE_TIME_STAMP";

    // 任务清单用户完成情况
    public static final String TASK_USER_COMPLETED = "TASK_USER_COMPLETED_";

    // 任务清单人工验证的任务，当前所处的状态
    public static final String TASK_USER_STATUS = "TASK_USER_STATUS";

    // 阻止任务清单所有任务的完成，阶段任务触发的时候开启
    public static final String STOP_ALL_TASK_COMPLETE = "STOP_ALL_TASK_COMPLETE";

    private static String userId;

    // 获取 SharedPreferences 对象
    public static SharedPreferences getSharedPreferces(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    // 获取 Editor 对象
    public static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferces(context).edit();
    }

    // 删除某个值
    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(key);
        editor.commit();
    }

    // 设置 boolean 值
    public static void set(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(key, value);
        editor.commit();
    }

    // 获取 boolean 值
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getSharedPreferces(context).getBoolean(key, defaultValue);
    }

    // 设置 int 值
    public static void set(Context context, String key, int value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(key, value);
        editor.commit();
    }

    // 获取 int 值
    public static int getInt(Context context, String key, int defaultValue) {
        return getSharedPreferces(context).getInt(key, defaultValue);
    }

    // int 值自增一
    public static int increment(Context context, String key, int defaultValue) {
        int value = getInt(context, key, defaultValue) + 1;
        set(context, key, value);
        return value;
    }

    // 设置 String 值
    public static void set(Context context, String key, String value) {
        if (USER_ID.equals(key)) {
            userId = value; // 缓存 userId
        }
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(key, value);
        editor.commit();
    }

    // 获取 String 值
    public static String getString(Context context, String key, String defaultValue) {
        return getSharedPreferces(context).getString(key, defaultValue);
    }

    //设置Long类型的值
    public static void set(Context context, String key, long l) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putLong(key, l);
        editor.commit();
    }

    //获取long类型的值
    public static long getLong(Context context, String key, long defaultValue) {
        return getSharedPreferces(context).getLong(key, defaultValue);
    }

    private static final String ONE_DAY_ONCE_STAMP = "ONE_DAY_ONCE_STAMP_"; // 判断是否是当天的第一次调用的键的前缀

    // 对于一个特定的 key，判断是否是当天的第一次调用
    // 对于一个特定的 key，当天第一次调用时，返回 true，其余不论调用多少次，都返回 false，直到第二天的第一次调用为止。
    public static boolean oneDayOnceStamp(Context context, String key, long curTime) {
        String today = String.valueOf(DateFormat.format("yyyy-MM-dd", curTime));
        String spStamp = getString(context, ONE_DAY_ONCE_STAMP + key, "");
        boolean returnBoolean;
        if (today != null && today.equals(spStamp)) {
            returnBoolean = false;
        } else {
            set(context, ONE_DAY_ONCE_STAMP + key, today);
            returnBoolean = true;
        }
        Log.d("test", "SP oneDayOnceStamp key=>:" + key + ",spStamp=>:" + spStamp + ",today=>:" + today + ",return " + returnBoolean + ".");
        return returnBoolean;
    }

    private static final String ONE_DAY_ONCE_STRING = "ONE_DAY_ONCE_STRING_";

    // 对于一个特定的 key，如果是当天的第一次调用，则必然返回 null
    // 对于一个特定的 key，如果不是当天的第一次调用，则返回它的值
    public static String oneDayOnceString(Context context, String key, long curTime) {
        if (oneDayOnceStamp(context, key, curTime)) {
            set(context, ONE_DAY_ONCE_STRING + key, "");
        }
        return getString(context, ONE_DAY_ONCE_STRING + key, null);
    }

    // 设置值
    public static void setOneDayOnceString(Context context, String key, String value) {
        set(context, ONE_DAY_ONCE_STRING + key, value);
    }

    public static String getUserId(Context context) {
        if (userId == null) {
            userId = SP.getString(context, USER_ID, "");
        }
        return userId;
    }

    public static String getUserIdKey(Context context, String key) {
        return key + getUserId(context);
    }

    public static void cacheDelete(Context context, String url) {
        setOneDayOnceString(context, url, "");
    }

    public static String cache(Context context, String url, Map<String, String> map, long time) {
        if (context == null || url == null) {
            return null;
        }
        final String suffix = map == null ? "" : map.toString();
        String result = oneDayOnceString(context, url + suffix, time);
        if (result == null || result.length() < 1) {
            try {
                if (map == null) {
                    map = new HashMap<>();
                }
                map.put("userId", getUserId(context));
                JSONArray jsonArray = DB.getDataInterface(context).commonRequest(url, map);
                if ("000000".equals(jsonArray.getJSONObject(0).getString("resp"))) {
                    result = jsonArray.getJSONObject(0).toString();
                    setOneDayOnceString(context, url + suffix, result);
                }
                Log.d("test", "SP result from Internet url=>:" + url + ",jsonArray=>:" + jsonArray + ",result=>:" + result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("test", "SP result from cache url=>:" + url + ",result=>:" + result);
        }
        return result;
    }

    public static void cache(final Context context, final String url, Map<String, String> map, long time, final OnCache onCache) {
        if (context == null || url == null || onCache == null) {
            return;
        }
        final String suffix = map == null ? "" : map.toString();
        String result = oneDayOnceString(context, url + suffix, time);
        if (result == null || result.length() < 1) {
            if (map == null) {
                map = new HashMap<>();
            }
            if (TextUtils.isEmpty(map.get("userId"))) {
                map.put("userId", getUserId(context));
            }
            DB.getDataInterface(context).commonRequest(url, new NetManager.DataArray() {
                @Override
                public void getServiceData(JSONArray jsonArray) {
                    try {
                        if ("000000".equals(jsonArray.getJSONObject(0).getString("resp"))) {
                            String str = jsonArray.getJSONObject(0).toString();
                            setOneDayOnceString(context, url + suffix, str);
                            onCache.onCache(str);
                        }
                    } catch (JSONException e) {
                        onCache.onCache(null);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    onCache.onCache(null);
                }
            }, map);
        } else {
            onCache.onCache(result);
        }
    }

    public interface OnCache {
        void onCache(String cache);
    }


}
