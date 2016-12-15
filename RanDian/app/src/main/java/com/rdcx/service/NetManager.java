package com.rdcx.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ClearCacheRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.rdcx.randian.LoginActivity;
import com.rdcx.randian.MyApplication;
import com.rdcx.tools.DB;
import com.rdcx.tools.SP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import volleyRequest.ArrayRequest;


/**
 * 网络数据获取的执行类
 * Created by Administrator on 2015/10/27 0027.
 *
 * @author 孟垂柳
 */
public class NetManager {
    private static RequestQueue mRequestQueue;
    private Context context;

    public NetManager(Context context) {
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        // 清理缓存防止内存溢出
        mRequestQueue.add(new ClearCacheRequest(mRequestQueue.getCache(), null));
    }

    /**
     * 通过post方法访问网络获取json数据返回
     *
     * @param url           服务器地址
     * @param dataArray     成功监听
     * @param errorListener 失败监听
     * @param map           post参数
     */
    public void postNetMsg(final String url, final DataArray dataArray, final ErrorListener errorListener,
                           final Map<String, String> map, final int count) {
        ArrayRequest arrayRequest = new ArrayRequest(url, new Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                if (count < 0) {
                    errorListener.onErrorResponse(new VolleyError("重新登陆次数过多。"));
                    return;
                }
                if (jsonArray.toString().contains("用户信息错误")) {
                    DB.getDataInterface(context).login(MyApplication.getPhoneStr(context),
                            SP.getString(context, "phoneNumber", ""),
                            SP.getString(context, "password", ""),
                            SP.getString(context, "login_type", ""),
                            SP.getString(context, "token", ""), new DataArray() {
                                @Override
                                public void getServiceData(JSONArray jsonArray) {
                                    try {
                                        JSONObject object = jsonArray.getJSONObject(0);
                                        String resp = object.getString("resp");
                                        if (resp.equals("000000")) { // 登陆成功
                                            postNetMsg(url, dataArray, errorListener, map, count - 1);
                                        } else { // 登陆失败
                                            Toast.makeText(context, "系统繁忙,请稍后再试！", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, errorListener);
                } else {
                    dataArray.getServiceData(jsonArray);
                }
            }
        }, errorListener, map);
        mRequestQueue.add(arrayRequest);
    }

    public JSONArray postNetMsg(final String url, final Map<String, String> map, final int count) {
        if (count < 0) {
            Log.w("test", "NetManager postNetMsg 登陆失败的次数过多。");
            return null;
        }
        JSONArray jsonArray = null;
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        ArrayRequest arrayRequest = new ArrayRequest(url, future, future, map);
        mRequestQueue.add(arrayRequest);
        try {
            jsonArray = future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (jsonArray.toString().contains("用户信息错误")) {
            // 用户尚未登陆，不能调用接口，尝试自动登陆
            try {
                JSONArray loginJsonArray = DB.getDataInterface(context).login(MyApplication.getPhoneStr(context),
                        SP.getString(context, "phoneNumber", ""),
                        SP.getString(context, "password", ""),
                        SP.getString(context, "login_type", ""),
                        SP.getString(context, "token", ""));
                JSONObject object = loginJsonArray.getJSONObject(0);
                String resp = object.getString("resp");
                if (resp.equals("000000")) {
                    return postNetMsg(url, map, count - 1);
                } else {
                    Toast.makeText(context, "系统繁忙,请稍后再试！", Toast.LENGTH_SHORT).show();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return jsonArray;
        }
    }

    public interface DataArray {
        void getServiceData(JSONArray jsonArray);
    }
    // ... 可以添加其他方式的获取，如get方式，或者使用别的解析方式
}
