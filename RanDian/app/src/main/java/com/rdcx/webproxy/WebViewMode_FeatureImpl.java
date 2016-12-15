package com.rdcx.webproxy;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.randian.MyApplication;
import com.rdcx.randian.R;
import com.rdcx.randian.WebHtmlActivity;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.dcloud.common.DHInterface.AbsMgr;
import io.dcloud.common.DHInterface.IFeature;
import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.util.JSUtil;

@SuppressWarnings("deprecation")
public class WebViewMode_FeatureImpl implements IFeature {

    public static WebDiary webDiary;

    @Override
    public String execute(final IWebview pWebViewImpl, String pActionName, String[] pJsArgs) {
        String value = null;
        if ("getUserId".equals(pActionName)) {
            value = SP.getString(pWebViewImpl.getContext(), SP.USER_ID, "");
        } else if ("getUserName".equals(pActionName)) {
            value = SP.getString(pWebViewImpl.getContext(), "nickName", "");
            if (TextUtils.isEmpty(value)) {
                value = Build.MODEL;
            }
        } else if ("setMenu".equals(pActionName)) {
//            if (webDiary != null) {
//                if (pJsArgs.length > 0) {
//                    webDiary.getDate(pJsArgs[0], pJsArgs.length > 1 ? pJsArgs[1] : "");
//                }
//            }
            // args1 存放菜单按钮的显示值
            String args1 = pJsArgs.length > 0 ? pJsArgs[0] : "";
            TextView webMenu = ((TextView) pWebViewImpl.getActivity().findViewById(R.id.webMenu));
            if (args1 != null && args1.length() > 0) {
                webMenu.setText(args1);
                webMenu.setVisibility(View.VISIBLE);
            } else {
                webMenu.setText(args1);
                webMenu.setVisibility(View.GONE);
            }

            // args2 存放按钮点击时需要触发的页面内的事件
            String args2 = pJsArgs.length > 1 ? pJsArgs[1] : null;
            webMenu.setTag(args2);

        } else if ("setHomeFlag".equals(pActionName)) {

            String args1 = pJsArgs.length > 0 ? pJsArgs[0] : "";
            Button buttonHome = ((Button) pWebViewImpl.getActivity().findViewById(R.id.btn_home));
            if (args1 == null || args1.length() < 1 || args1.equals("0")) {
                buttonHome.setVisibility(View.GONE);
            } else {
                buttonHome.setVisibility(View.VISIBLE);
            }

        } else if ("goHome".equals(pActionName)) {

            pWebViewImpl.getActivity().finish();

        } else if ("getContactCount".equals(pActionName)) {
            try {
                HashMap<String, String> contactCountMap = DB.getContactCount(pWebViewImpl.getContext());
                JSONObject jo = new JSONObject();
                for (String key : contactCountMap.keySet()) {
                    jo.put(key, contactCountMap.get(key));
                }
                return JSUtil.wrapJsVar(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("reLogin".equals(pActionName)) {
            final String args1 = pJsArgs.length > 0 ? pJsArgs[0] : "";
            final String args2 = pJsArgs.length > 1 ? pJsArgs[1] : "";
            Context context = pWebViewImpl.getContext().getApplicationContext();
            DB.getDataInterface(context).login(MyApplication.getPhoneStr(pWebViewImpl.getActivity()),
                    SP.getString(context, "phoneNumber", ""),
                    SP.getString(context, "password", ""),
                    SP.getString(context, "login_type", ""),
                    SP.getString(context, "token", ""), new NetManager.DataArray() {
                        @Override
                        public void getServiceData(JSONArray jsonArray) {
                            try {
                                JSONObject object = jsonArray.getJSONObject(0);
                                String resp = object.getString("resp");
                                if (resp.equals("000000")) {
                                    ((WebHtmlActivity) (pWebViewImpl.getActivity())).sendMessage(2, args1);
                                } else {
                                    ((WebHtmlActivity) (pWebViewImpl.getActivity())).sendMessage(2, args2);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            ((WebHtmlActivity) (pWebViewImpl.getActivity())).sendMessage(2, args2);
                        }
                    });
        } else if ("showStagetTask".equals(pActionName)) {
            try {
                String args1 = pJsArgs.length > 0 ? pJsArgs[0] : "";
                int id = Integer.parseInt(args1);
                TaskTools.addStageTask(pWebViewImpl.getContext(), id);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        Log.d("test", "pActionName->:" + pActionName + " value->:" + value);
        return JSUtil.wrapJsVar(value);
    }

    @Override
    public void init(AbsMgr pFeatureMgr, String pFeatureName) {
        // 初始化Feature
    }

    @Override
    public void dispose(String pAppid) {

    }

    public static void setWebDiary(WebDiary diary) {
        webDiary = diary;
    }

    public interface WebDiary {
        void getDate(String title, String type);
    }

}
