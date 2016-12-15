package com.rdcx.tools;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rdcx.myview.LoadingView;
import com.rdcx.randian.R;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/4/21 0021.
 * <p/>
 * 消息工具类
 */
public class MessageTools {

    public static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    public static void showDatePickerDialog(Activity activity, DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        datePickerDialog.show();
    }

    private static void addListener(final Dialog dialog, final PopupWindow popupWindow, TextView textView, CharSequence text, final OnClick onClick) {
        if (text == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                        if (onClick != null) {
                            onClick.onClick();
                        }
                    } else if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        if (onClick != null) {
                            onClick.onClick();
                        }
                    }

                }
            });
        }
    }

    public static void showTaskConfirm(Activity activity, CharSequence msg, CharSequence okText, final OnClick okClick, CharSequence cancelText, final OnClick cancelClick) {
        final Dialog dialog = new Dialog(activity, R.style.dialogStyle);
        dialog.setContentView(R.layout.dialog_alert);
        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(msg == null ? "提示" : msg);

        addListener(dialog, null, (TextView) dialog.findViewById(R.id.dialog_ok), okText, okClick);
        addListener(dialog, null, (TextView) dialog.findViewById(R.id.dialog_cancel), cancelText, cancelClick);

        dialog.setCancelable(false);
        dialog.show();
    }

    public static void showTaskAlert(Activity activity, CharSequence msg, CharSequence okText, final OnClick okClick) {
        showTaskConfirm(activity, msg, okText, okClick, null, null);
    }

    private static Dialog dialog;

    public static void showTaskLoading(Activity activity, String msg) {

        hideTaskLoading();

        dialog = new Dialog(activity, R.style.dialogStyle);
        dialog.setContentView(R.layout.dialog_alert);

        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(msg == null ? "请稍后......" : msg);

        dialog.findViewById(R.id.dialog_ok).setVisibility(View.GONE);
        dialog.findViewById(R.id.dialog_cancel).setVisibility(View.GONE);

        LoadingView loadingView = (LoadingView) dialog.findViewById(R.id.dialog_loading);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.startAnimation();

        dialog.setCancelable(false);
        dialog.show();
    }

    public static void hideTaskLoading() {
        if (dialog != null && dialog.isShowing()) {
            LoadingView loadingView = (LoadingView) dialog.findViewById(R.id.dialog_loading);
            loadingView.stopAnimation();
            dialog.dismiss();
        }
    }

    public static void showPhotoWindow(Activity activity, CharSequence btn1Text, final OnClick btn1Click, CharSequence btn2Text, OnClick btn2Click, CharSequence cancelText, OnClick cancelClick) {
        View contentView = activity.getLayoutInflater().inflate(R.layout.pop_select_photo, null);
        final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        addListener(null, popupWindow, (TextView) contentView.findViewById(R.id.photograph), btn1Text, btn1Click);
        addListener(null, popupWindow, (TextView) contentView.findViewById(R.id.albums), btn2Text, btn2Click);
        addListener(null, popupWindow, (TextView) contentView.findViewById(R.id.cancel), cancelText, cancelClick);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    public interface OnClick {
        void onClick();
    }


}
