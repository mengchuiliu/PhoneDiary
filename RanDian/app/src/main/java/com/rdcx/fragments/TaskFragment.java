package com.rdcx.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.bean.TaskRankFriend;
import com.rdcx.myview.TaskBannerView;
import com.rdcx.myview.TaskRankAdapter;
import com.rdcx.randian.MyApplication;
import com.rdcx.randian.MyWeiBoShareActivity;
import com.rdcx.randian.R;
import com.rdcx.randian.TaskActivity;
import com.rdcx.randian.UpdateUserInfo;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.MessageTools;
import com.rdcx.randian.WebHtmlActivity;
import com.rdcx.tools.Operation;
import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/3/14 0014.
 * 任务清单
 *
 * @author mengchuiliu
 */
@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class TaskFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ViewFlipper viewFlipper;
    private LinearLayout taskNav;
    private Thread flipperThread;
    private RelativeLayout tast_host;
    private LinearLayout tast_rank_show, tast_rank_national, tast_rank_friend;
    TaskRankAdapter taskRankAdapter;
    TextView task_id_1, task_id_2, task_id_3, task_rank_list, no_friend;
    TextView task_content_1, task_content_2, task_content_3, task_complete, task_gap;
    ImageView task_icon, task_icon_1, task_icon_2, task_icon_3;
    GridView gridView;

    List<TaskRankFriend> list = new ArrayList<>();

    private static class FlipperThreadHandler extends Handler implements Runnable {

        private WeakReference<TaskFragment> reference;
        private boolean running;
        private boolean ready;

        public FlipperThreadHandler(TaskFragment taskFragment) {
            this.reference = new WeakReference<>(taskFragment);
            this.running = true;
            this.ready = false;
        }

        @Override
        public void run() {
            try {

                TaskBannerView.getTaskBannerViews(reference.get().getContext(), this, 1);

                while (running) {
                    Thread.sleep(4000L);
                    if (ready) {
                        sendEmptyMessage(0);
                    }
                }
            } catch (Exception e) {
                stopRunning();
            }
        }

        public void stopRunning() {
            running = false;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if (reference != null && reference.get() != null) {
                    if (msg.what == 0) {
                        reference.get().animationToRight(true);
                    } else if (msg.what == 1) {
                        List<TaskBannerView> taskBannerViewList = (List<TaskBannerView>) msg.obj;
                        reference.get().setViewFlipper(taskBannerViewList);
                        if (taskBannerViewList.size() == 1) {
                            stopRunning();
                        } else {
                            ready = true;
                        }
                    }
                }
            } catch (Exception e) {
                stopRunning();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task, null);
        return view;
    }

    private boolean init = false;

    public void init() {
        if (!init) {
            setView();
            init = true;
            onResume();
        }
    }

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (Utils.isNetworkAvailable(getActivity())) {
                Toast.makeText(getActivity(), "网络异常，请稍后重试！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (init) {
            getUserAnswer();

            DB.getDataInterface(getActivity()).getNationalRank(SP.getUserId(getContext()), new NetManager.DataArray() {
                @Override
                public void getServiceData(JSONArray jsonArray) {
                    try {
                        JSONObject object = jsonArray.getJSONObject(0);
                        String resp = object.getString("resp");
                        if (resp.equals("000000")) {
                            task_rank_list.setText("当前排名：第" + object.getInt("ranking") + "名");
                            task_complete.setText("完成指数：" + object.getInt("count") + "任务");
                            task_gap.setText("距离上一名用户仅有" + object.getInt("topCount") + "个任务\n加油超越吧！");
                            tv_content = String.valueOf(object.getInt("topCount"));
                            JSONArray array = object.getJSONArray("modelList");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                if (i == 0) {
                                    task_icon_1.setTag(jsonObject.getInt("userId"));
                                    task_id_1.setText(String.valueOf(jsonObject.getInt("userId")));
                                    task_content_1.setText("已完成" + jsonObject.getInt("count") + "个");
                                    getRankPath(String.valueOf(jsonObject.getInt("userId")), 1, null);
                                } else if (i == 1) {
                                    task_icon_2.setTag(jsonObject.getInt("userId"));
                                    task_id_2.setText(String.valueOf(jsonObject.getInt("userId")));
                                    task_content_2.setText("已完成" + jsonObject.getInt("count") + "个");
                                    getRankPath(String.valueOf(jsonObject.getInt("userId")), 2, null);
                                } else if (i == 2) {
                                    task_icon_3.setTag(jsonObject.getInt("userId"));
                                    task_id_3.setText(String.valueOf(jsonObject.getInt("userId")));
                                    task_content_3.setText("已完成" + jsonObject.getInt("count") + "个");
                                    getRankPath(String.valueOf(jsonObject.getInt("userId")), 3, null);
                                }
                                if (SP.getUserId(getContext()).equals(jsonObject.getString("userId"))) {
                                    task_rank_list.setText("当前排名：第" + (i + 1) + "名");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, errorListener);

            DB.getDataInterface(getActivity()).getFriendRank(SP.getUserId(getContext()), new NetManager.DataArray() {
                @Override
                public void getServiceData(JSONArray jsonArray) {
                    try {
                        JSONObject object = jsonArray.getJSONObject(0);
                        String resp = object.getString("resp");
                        if (resp.equals("000000")) {
                            JSONArray array = object.getJSONArray("modelList");
                            if (array.length() > 0) {
                                no_friend.setVisibility(View.GONE);
                                gridView.setVisibility(View.VISIBLE);
                                isComplete = 0;
                                list.clear();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = array.getJSONObject(i);
                                    TaskRankFriend rankFriend = new TaskRankFriend();
                                    rankFriend.taskCount = jsonObject.getInt("count") + "个任务";
                                    rankFriend.userId = jsonObject.getString("userId");
                                    getRankPath(String.valueOf(jsonObject.getInt("userId")), 0, rankFriend);
                                    list.add(rankFriend);
                                    taskRankAdapter.setData(list);
                                    gridView.setAdapter(taskRankAdapter);
                                }
                            } else {
                                no_friend.setVisibility(View.VISIBLE);
                                gridView.setVisibility(View.GONE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, errorListener);
        }

    }

    private void getRankPath(String userid, final int type, final TaskRankFriend rankFriend) {
        DB.getDataInterface(getActivity()).getRankIcon(userid, new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject object = jsonArray.getJSONObject(0);
                    String resp = object.getString("resp");
                    if (resp.equals("000000")) {
                        String path = object.getString("phonePath");
                        String name = object.optString("phoneName");
                        if (rankFriend != null) {
                            rankFriend.nickName = name;
                            new TaskRankIcon(rankFriend).execute(path);
                        } else {
                            if (!TextUtils.isEmpty(name)) {
                                if (type == 1) {
                                    task_id_1.setText(name);
                                } else if (type == 2) {
                                    task_id_2.setText(name);
                                } else if (type == 3) {
                                    task_id_3.setText(name);
                                }
                            }
                            new TaskRankIcon(type).execute(path);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
    }

    int isComplete = 0;

    /**
     * 图片加载任务
     */
    class TaskRankIcon extends AsyncTask<String, Void, Bitmap> {
        private TaskRankFriend rankFriend = null;
        private int type;

        public TaskRankIcon(TaskRankFriend rankFriend) {
            this.rankFriend = rankFriend;
        }

        public TaskRankIcon(int type) {
            this.type = type;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (TextUtils.isEmpty(params[0])) {
                return null;
            } else {
                return Utils.getBitmap(Constants.head_url + params[0].replace("\\", "/"));
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (rankFriend != null) {
                isComplete++;
                if (bitmap == null) {
                    //Toast.makeText(getActivity(), "头像获取失败！", Toast.LENGTH_SHORT).show();
                } else {
                    rankFriend.taskIcon = bitmap;
                }
                if (isComplete == list.size()) {
                    taskRankAdapter.notifyDataSetChanged();
                }
            } else {
                if (bitmap == null) {
                    //Toast.makeText(getActivity(), "头像获取失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (type == 1) {
                    task_icon_1.setImageBitmap(Utils.toRoundBitmap(bitmap));
                } else if (type == 2) {
                    task_icon_2.setImageBitmap(Utils.toRoundBitmap(bitmap));
                } else if (type == 3) {
                    task_icon_3.setImageBitmap(Utils.toRoundBitmap(bitmap));
                }
            }
        }
    }

    private void setView() {
        tast_host = (RelativeLayout) view.findViewById(R.id.tast_host);
        tast_rank_show = (LinearLayout) view.findViewById(R.id.tast_rank_show);
        tast_rank_national = (LinearLayout) view.findViewById(R.id.tast_rank_national);
        tast_rank_friend = (LinearLayout) view.findViewById(R.id.tast_rank_friend);

        task_rank_list = (TextView) view.findViewById(R.id.task_rank_list);
        task_complete = (TextView) view.findViewById(R.id.task_complete);
        task_gap = (TextView) view.findViewById(R.id.task_gap);
        task_icon = (ImageView) view.findViewById(R.id.task_icon);
        task_icon.setImageBitmap(Utils.getPortraitBitmap(getActivity(), handler, true));

        task_id_1 = (TextView) view.findViewById(R.id.task_id_1);
        task_id_2 = (TextView) view.findViewById(R.id.task_id_2);
        task_id_3 = (TextView) view.findViewById(R.id.task_id_3);
        task_content_1 = (TextView) view.findViewById(R.id.task_content_1);
        task_content_2 = (TextView) view.findViewById(R.id.task_content_2);
        task_content_3 = (TextView) view.findViewById(R.id.task_content_3);
        task_icon_1 = (ImageView) view.findViewById(R.id.task_icon_1);
        task_icon_2 = (ImageView) view.findViewById(R.id.task_icon_2);
        task_icon_3 = (ImageView) view.findViewById(R.id.task_icon_3);
        task_icon_1.setOnClickListener(this);
        task_icon_2.setOnClickListener(this);
        task_icon_3.setOnClickListener(this);
        no_friend = (TextView) view.findViewById(R.id.no_friend);

        gridView = (GridView) view.findViewById(R.id.task_rank_grid);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    TaskRankFriend taskRankFriend = (TaskRankFriend) parent.getAdapter().getItem(position);
                    Intent intent = new Intent(getActivity(), WebHtmlActivity.class);
                    intent.putExtra("url", "home.html?beUserId=" + taskRankFriend.userId);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        taskRankAdapter = new TaskRankAdapter(getActivity());

        view.findViewById(R.id.tv_notional).setOnClickListener(this);
        view.findViewById(R.id.tv_friend).setOnClickListener(this);
        view.findViewById(R.id.common_life).setOnClickListener(this);
        view.findViewById(R.id.i_am_strange).setOnClickListener(this);
        view.findViewById(R.id.life_winner).setOnClickListener(this);
        view.findViewById(R.id.task_rank).setOnClickListener(this);
        view.findViewById(R.id.task_back).setOnClickListener(this);
        view.findViewById(R.id.task_rank_share).setOnClickListener(this);

        viewFlipper = (ViewFlipper) view.findViewById(R.id.task_flipper);
        viewFlipper.addView(new TaskBannerView(getContext(), R.mipmap.cloud, null));

    }

    AlertDialog dialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (thread != null) {
                        thread = null;
                    }
                    File f = new File(rankPath);
                    if (f.exists()) {
                        Utils.uploadFile(handler, f, Constants.PHOTO_UPLOAD, 2);
                    } else {
                        if (pd != null) {
                            pd.dismiss();
                            pd = null;
                        }
                        Toast.makeText(getActivity(), "排行榜图片分享失败!请稍后重试", Toast.LENGTH_SHORT).show();
                        Log.e("my_log", "排行榜截图失败!");
                    }
                    break;
                case 2:
                    if (pd != null) {
                        pd.dismiss();
                        pd = null;
                    }
                    if (msg.obj.toString() == null || msg.obj.toString().equals("")) {
                        if (Utils.isNetworkAvailable(getActivity().getApplicationContext())) {
                            Toast.makeText(getActivity().getApplicationContext(), "分享发现失败,请稍后再试！",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(msg.obj.toString());
                        org.json.JSONObject object = jsonArray.getJSONObject(0);
                        String resp = object.getString("resp");
                        String photoPath = object.getString("filePath");
                        if (resp.equals("000000")) {
                            isCover = false;
                            servicePath = photoPath;

                            View view = LayoutInflater.from(getActivity()).inflate(R.layout.diary_share_view, null);
                            view.findViewById(R.id.diary_cover).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_qq).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_wx).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_wb).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_cancel).setOnClickListener(onClickListener);
                            if (dialog == null) {
                                dialog = new AlertDialog.Builder(getActivity()).create();
                            }
                            dialog.show();
                            Window window = dialog.getWindow();
                            window.setGravity(Gravity.CENTER);
                            window.setContentView(view);
                            dialog.setCanceledOnTouchOutside(false);
                        } else {
                            isCover2 = true;
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 16:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) {
                            task_icon.setImageBitmap(Utils.toRoundBitmap(bitmap));
                            Utils.setPortraitBitmap(getActivity(), bitmap);
                        }
                    } else {
                        Log.e("my_log", "头像加载失败！");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.diary_cover:
                    dialogCancel();
                    if (isCover && !isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享内容生成中,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享失败,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    new NetDataGetter(getActivity().getApplicationContext()).dynamic(SP.getString(getActivity(), SP.USER_ID, "-1"),
                            "", servicePath, "", "", new NetManager.DataArray() {
                                @Override
                                public void getServiceData(org.json.JSONArray jsonArray) {
                                    try {
                                        org.json.JSONObject object = jsonArray.getJSONObject(0);
                                        String resp = object.getString("resp");
                                        if (resp.equals("000000")) {
                                            //分享成功
                                            Toast.makeText(getActivity(), "任务排行分享成功!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "发现分享失败,请稍后再试！",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } catch (org.json.JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "系统繁忙，发现分享失败!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
                case R.id.diary_qq:
                    dialogCancel();
                    if (isCover && !isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享内容生成中,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享失败,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    final Bundle params = new Bundle();
                    params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                    params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "手机日记•分享");// 必填
                    // 必填,点击分享内容所需要的链接地址
                    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, Constants.head_url + servicePath);
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(rankPath);
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);// 分享的图片，选填
                    MyApplication.mTencent.shareToQzone(getActivity(), params, new IUiListener() {
                        @Override
                        public void onError(UiError error) {
                            Toast.makeText(getActivity().getApplicationContext(), "QQ分享失败！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete(Object response) {
                            Toast.makeText(getActivity().getApplicationContext(), "日记本QQ分享成功！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(getActivity().getApplicationContext(), "QQ分享取消！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case R.id.diary_wx:
                    dialogCancel();
                    MyApplication.flag = false;
                    MyApplication.wxType = 3;
                    Bitmap thumb;
                    WXImageObject imgObj;
                    thumb = BitmapFactory.decodeFile(rankPath);
                    imgObj = new WXImageObject();
                    imgObj.setImagePath(rankPath);

                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;
                    Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 150, 150, true);
                    thumb.recycle();
                    msg.thumbData = Utils.bmpByte(thumbBmp, true); // 设置缩略图

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = Utils.buildTransaction("img");
                    req.message = msg;
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
                    boolean fla = MyApplication.api.sendReq(req);
                    if (!fla) {
                        Toast.makeText(getActivity(), "微信分享失败！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.diary_wb:
                    dialogCancel();
                    Intent intent1 = new Intent(getActivity(), MyWeiBoShareActivity.class);
                    intent1.putExtra("content", "");
                    intent1.putExtra("sharePath", rankPath);
                    startActivityForResult(intent1, 66);
                    break;
                case R.id.diary_cancel:
                    dialog.dismiss();
                    break;
                case R.id.task_rank_back:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

    private void dialogCancel() {
        dialog.dismiss();
        if (pd == null) {
            pd = ProgressDialog.show(getActivity(), "", "任务排名分享中...");
        }
        pd.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
                pd = null;
            }
        }, 1000);
    }

    ProgressDialog pd = null;
    Thread thread = null;
    String rankPath = Environment.getExternalStorageDirectory() + "/ZhangXin/ScreenShot/TaskRankScreen.png";
    boolean isCover, isCover2;
    String servicePath;

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.common_life:
                intent = new Intent(getActivity(), TaskActivity.class);
                intent.putExtra("title", "平凡的人生");
                intent.putExtra("type", 1);
                startActivity(intent);
                break;
            case R.id.i_am_strange:
                intent = new Intent(getActivity(), TaskActivity.class);
                intent.putExtra("title", "我是奇葩");
                intent.putExtra("type", 2);
                startActivity(intent);
                break;
            case R.id.life_winner:
                intent = new Intent(getActivity(), TaskActivity.class);
                intent.putExtra("title", "我是人生赢家");
                intent.putExtra("type", 3);
                startActivity(intent);
                break;
            case R.id.task_rank:
                tast_host.setVisibility(View.GONE);
                tast_rank_show.setVisibility(View.VISIBLE);
                break;
            case R.id.task_back:
                tast_host.setVisibility(View.VISIBLE);
                tast_rank_show.setVisibility(View.GONE);
                break;
            case R.id.tv_friend:
                tast_rank_friend.setVisibility(View.VISIBLE);
                tast_rank_national.setVisibility(View.GONE);
                break;
            case R.id.tv_notional:
                tast_rank_friend.setVisibility(View.GONE);
                tast_rank_national.setVisibility(View.VISIBLE);
                break;
            case R.id.task_rank_share:
                if (pd == null) {
                    pd = ProgressDialog.show(getActivity(), "", "分享图片生成中...", true);
                    pd.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                if (pd.isShowing()) {
                                    pd.dismiss();
                                }
                            }
                            return false;
                        }
                    });
                }
                final Bitmap bitmap = takeScreenShot(getActivity());
                if (thread == null) {
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/ScreenShot");
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            if (bitmap != null) {
                                Utils.savePhotoToSDCard(bitmap, rankPath);
                            } else {
                                Log.e("my_log", "截图失败!");
                            }
                            handler.sendEmptyMessage(0);
                        }
                    });
                    thread.start();
                }
                break;
            case R.id.task_icon_1:
            case R.id.task_icon_2:
            case R.id.task_icon_3:
                try {
                    int userId = (Integer) v.getTag();
                    Intent intent1 = new Intent(getActivity(), WebHtmlActivity.class);
                    intent1.putExtra("url", "home.html?beUserId=" + userId);
                    startActivity(intent1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        // 点击 Banner 条
        if (v instanceof TaskBannerView) {
            ((TaskBannerView) v).onClick();
        }

    }

    private void setViewFlipper(List<TaskBannerView> taskBannerViewList) {

        viewFlipper = (ViewFlipper) view.findViewById(R.id.task_flipper);
        viewFlipper.removeAllViews();
        taskNav = (LinearLayout) view.findViewById(R.id.task_nav);
        taskNav.removeAllViews();

        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e1.getX() - e2.getX()) < Math.abs(e1.getY() - e2.getY())) {
                    return false;
                }
                if (e1.getX() - e2.getX() > 50) { // 向左滑
                    animationToRight(true);
                    return true;
                } else if (e1.getX() - e2.getX() < -50) { // 向右滑
                    animationToRight(false);
                    return true;
                }
                return false;
            }
        });

        for (TaskBannerView taskBannerView : taskBannerViewList) {
            taskBannerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            taskBannerView.setOnClickListener(this);
            viewFlipper.addView(taskBannerView);
            viewFlipper.setFlipInterval(50);
            if (taskBannerViewList.size() > 1) {
                getActivity().getLayoutInflater().inflate(R.layout.fragment_task_nav, taskNav);
            }
        }
        if (taskBannerViewList.size() > 1) {
            taskNav.getChildAt(0).setBackgroundResource(R.drawable.task_nav_hover);
        }

    }

    public void animationToRight(boolean right) {
        if (right) {
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_out));
            viewFlipper.showNext();
        } else {
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_in));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out));
            viewFlipper.showPrevious();
        }

        for (int i = 0; i < taskNav.getChildCount(); i++) {
            taskNav.getChildAt(i).setBackgroundResource(i == viewFlipper.getDisplayedChild() ? R.drawable.task_nav_hover : R.drawable.task_nav_back);
        }
    }

    public boolean onKeyDown() {
        if (tast_rank_show.getVisibility() == View.VISIBLE) {
            tast_rank_show.setVisibility(View.GONE);
            tast_host.setVisibility(View.VISIBLE);
            return true;
        } else {
            return false;
        }
    }

    // 获取指定Activity的截屏，保存到png文件
    private Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = 0;

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    private boolean shouldShowFlash = false;

    private int completeCount = 0;

    private com.alibaba.fastjson.JSONArray taskList;

    /**
     * 获取用户任务清单的完成情况，并将结果保存在 {@link SP}之中
     * <p/>
     * 成功之后再获取所有任务清单{@link #getAllTask()}.
     */
    private void getUserAnswer() {
        DB.getDataInterface(getContext()).taskAnswerByUserId(SP.getString(getContext(), SP.USER_ID, ""), new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if ("000000".equals(jsonObject.getString("resp"))) {
                        JSONArray modelList = jsonObject.getJSONArray("modelList");
                        String str = "";
                        completeCount = 0;
                        for (int i = 0, len = modelList.length(); i < len; i++) {
                            JSONObject model = modelList.getJSONObject(i);
                            str += model.getInt("taskId") + ",";
                            completeCount++;
                        }
                        String taskUserCompleted = SP.getString(getContext(), SP.TASK_USER_COMPLETED + SP.getString(getContext(), SP.USER_ID, ""), "");
                        int oldCompleteCount = 0;
                        for (String taskId : taskUserCompleted.split(",")) {
                            if (taskId.length() > 0) {
                                oldCompleteCount++;
                            }
                        }
                        if (oldCompleteCount != completeCount) {
                            shouldShowFlash = true;
                        }
                        SP.set(getContext(), SP.TASK_USER_COMPLETED + SP.getString(getContext(), SP.USER_ID, ""), str);

                        JSONArray modelPhoto = jsonObject.getJSONArray("modelPhoto");
                        HashMap<Integer, Integer> statusMap = new HashMap<>();
                        for (int i = 0, len = modelPhoto.length(); i < len; i++) {
                            JSONObject model = modelPhoto.getJSONObject(i);
                            int taskId = model.getInt("taskId");
                            int status = model.getInt("status");
                            if (status != 1) { // 0：审核中，1：已通过，2：未通过。已通过的在 modelList 中会有数据。
                                statusMap.put(taskId, status);
                            }
                        }
                        String statusStr = com.alibaba.fastjson.JSONObject.toJSONString(statusMap);
                        SP.set(getContext(), SP.TASK_USER_STATUS + SP.getString(getContext(), SP.USER_ID, ""), statusStr);

                        getAllTask();

                        if (flipperThread == null || !flipperThread.isAlive()) {
                            flipperThread = new Thread(new FlipperThreadHandler(TaskFragment.this));
                            flipperThread.start();
                        }

                    }
                } catch (JSONException e) {
                    Log.e("test", "TaskFragment getUserAnswer");
                }
            }
        }, errorListener);
    }

    /**
     * 获取所有的任务清单
     * 再将任务清单中所需要的数据保存到 {@link SP}
     */
    private void getAllTask() {
        SP.cache(getContext(), Constants.GET_TASK_BY_ALL, null, System.currentTimeMillis(), new SP.OnCache() {
            @Override
            public void onCache(String cache) {
                if (cache == null) {
                    errorListener.onErrorResponse(null);
                } else {

                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(cache);
                    taskList = jsonObject.getJSONArray("modelList");

                    checkStageTask();
                }
            }
        });
    }


    AlertDialog alertDialog;
    String tv_complete, tv_national, tv_content, tv_friend;
    int imgId = -1;

    /**
     * 阶段任务检测
     */
    public void checkStageTask() {
        // 完成的任务是8的倍数据的时候触发阶段任务
        try {
            if (completeCount / 8 > SP.getInt(getContext(), SP.getUserIdKey(getContext(), SP.TASK_GUIDE_PARSE), 0)) {
                findTaskGuideByUserId(completeCount / 8);
            } else if (shouldShowFlash) {
                // 需要显示闪屏页
                DB.getDataInterface(getActivity()).getSplashScreen(SP.getUserId(getContext()), new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(0);
                            String resp = object.getString("resp");
                            if (resp.equals("000000")) {
                                tv_friend = String.valueOf(object.getInt("rankUser"));
                                tv_complete = String.valueOf(object.getInt("count"));
                                tv_national = String.valueOf(object.getInt("ranking"));
                                imgId = object.getInt("imgId");
                                showDialog(imgId);
                                shouldShowFlash = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, errorListener);

            } else if (SP.oneDayOnceStamp(getActivity(), "shouldShowScreen", System.currentTimeMillis())) {

                DB.getDataInterface(getActivity()).getSplashScreen(SP.getUserId(getContext()), new NetManager.DataArray() {
                    @Override
                    public void getServiceData(JSONArray jsonArray) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(0);
                            String resp = object.getString("resp");
                            if (resp.equals("000000")) {
                                tv_friend = String.valueOf(object.getInt("rankUser"));
                                tv_complete = String.valueOf(object.getInt("count"));
                                tv_national = String.valueOf(object.getInt("ranking"));
                                imgId = object.getInt("imgId");
                                showDialog(66);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, errorListener);

            }
        } catch (Exception e) {
            Log.e("test", "TaskFragment checkStageTask cause an Exception=>:", e);
        }
    }

    private void showDialog(int type) {
        View view;
        TextView tv_1, tv_2, tv_3, tv_4, tv_5;

        int diff = 0;
        int complete = Integer.valueOf(tv_complete);
        if (0 < complete && complete <= 10) {
            diff = 10 - complete;
        } else if (10 < complete && complete <= 30) {
            diff = 30 - complete;
        } else if (30 < complete && complete <= 50) {
            diff = 50 - complete;
        } else if (50 < complete && complete <= 80) {
            diff = 80 - complete;
        }
        switch (type) {
            case 1:
            case 8:
                view = LayoutInflater.from(getActivity()).inflate(R.layout.task_screen_1, null);
                tv_1 = (TextView) view.findViewById(R.id.task_show_complete);
                tv_2 = (TextView) view.findViewById(R.id.task_show_national);
                tv_4 = (TextView) view.findViewById(R.id.task_show_content);
                tv_3 = (TextView) view.findViewById(R.id.reward);
                tv_5 = (TextView) view.findViewById(R.id.count);
                tv_1.setText("已完成任务：" + tv_complete);
                tv_2.setText("当前排名：" + tv_national + "\t\t");
                tv_4.setText("只需要在完成" + diff + "个任务\n即可获取奖励");
                if (0 < complete && complete <= 10) {
                    tv_3.setText("5元现金红包");
                    tv_5.setText("仅限排名前1000名用户");
                } else if (10 < complete && complete <= 30) {
                    tv_3.setText("10元现金红包");
                    tv_5.setText("仅限排名前500名用户");
                } else if (30 < complete && complete <= 50) {
                    tv_3.setText("50元现金红包");
                    tv_5.setText("仅限排名前100名用户");
                } else if (50 < complete && complete <= 80) {
                    tv_3.setText("200元现金红包");
                    tv_5.setText("仅限排名前50名用户");
                }
                break;
            case 2:
            case 7:
                view = LayoutInflater.from(getActivity()).inflate(R.layout.task_screen_2, null);
                tv_1 = (TextView) view.findViewById(R.id.task_show_complete);
                tv_2 = (TextView) view.findViewById(R.id.task_show_national);
                tv_4 = (TextView) view.findViewById(R.id.task_show_content);
                tv_3 = (TextView) view.findViewById(R.id.reward);
                tv_5 = (TextView) view.findViewById(R.id.count);
                tv_1.setText("已完成任务：" + tv_complete);
                tv_2.setText("当前排名：" + tv_national + "\t\t");
                tv_4.setText("只需要在完成" + diff + "个任务\n即可获取奖励");
                if (0 < complete && complete <= 10) {
                    tv_3.setText("5元现金红包");
                    tv_5.setText("仅限排名前1000名用户");
                } else if (10 < complete && complete <= 30) {
                    tv_3.setText("10元现金红包");
                    tv_5.setText("仅限排名前500名用户");
                } else if (30 < complete && complete <= 50) {
                    tv_3.setText("50元现金红包");
                    tv_5.setText("仅限排名前100名用户");
                } else if (50 < complete && complete <= 80) {
                    tv_3.setText("200元现金红包");
                    tv_5.setText("仅限排名前50名用户");
                }
                break;
            case 3:
                view = LayoutInflater.from(getActivity()).inflate(R.layout.task_screen_3, null);
                tv_1 = (TextView) view.findViewById(R.id.task_show_complete);
                tv_2 = (TextView) view.findViewById(R.id.task_show_national);
                tv_1.setText("已完成任务：" + tv_complete + "个");
                tv_2.setText("当前排名：" + tv_national);
                break;
            case 4:
                view = LayoutInflater.from(getActivity()).inflate(R.layout.task_screen_4, null);
                tv_1 = (TextView) view.findViewById(R.id.task_show_complete);
                tv_2 = (TextView) view.findViewById(R.id.task_show_national);
                tv_3 = (TextView) view.findViewById(R.id.reward);
                tv_1.setText("已完成任务：" + tv_complete + "个");
                tv_2.setText("当前排名：" + tv_national);
                if (0 < complete && complete <= 10) {
                    tv_3.setText("恭喜小主5元现金红包!");
                } else if (10 < complete && complete <= 30) {
                    tv_3.setText("10元现金红包");
                } else if (30 < complete && complete <= 50) {
                    tv_3.setText("50元现金红包");
                } else if (50 < complete && complete <= 80) {
                    tv_3.setText("200元现金红包");
                }
                break;
            case 6:
                view = LayoutInflater.from(getActivity()).inflate(R.layout.task_screen_6, null);
                tv_1 = (TextView) view.findViewById(R.id.task_show_complete);
                tv_1.setText("已完成任务：" + tv_complete + "个");
                break;
            default:
                view = LayoutInflater.from(getActivity()).inflate(R.layout.task_screen, null);
                tv_1 = (TextView) view.findViewById(R.id.task_show_complete);
                tv_2 = (TextView) view.findViewById(R.id.task_show_national);
                tv_3 = (TextView) view.findViewById(R.id.task_show_friend);
                tv_4 = (TextView) view.findViewById(R.id.task_show_content);
                tv_1.setText("完成指数：" + tv_complete + "个");
                tv_2.setText("全国排名：" + tv_national + "名");
                tv_3.setText("好友排名：" + tv_friend + "名");
                tv_4.setText("距离上一名用户仅有" + tv_content + "个任务，\n翻滚吧，小主");
                break;
        }
        view.findViewById(R.id.task_rank_back).setOnClickListener(onClickListener);
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.show();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = dm.widthPixels * 4 / 5;
        lp.height = dm.heightPixels * 2 / 3;
        window.setGravity(Gravity.CENTER);
        window.setContentView(view, params);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private void findTaskGuideByUserId(final int parse) {
        DB.getDataInterface(getContext()).findTaskGuideByUserId(SP.getUserId(getContext()), new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if ("000000".equals(jsonObject.getString("resp"))) {
                        JSONArray modelList = jsonObject.getJSONArray("modelList");
                        for (int i = 0; i < modelList.length(); i++) {
                            JSONObject model = modelList.getJSONObject(i);
                            int taskId = model.getInt("taskId");
                            if (taskId == parse + 1000) {
                                SP.set(getContext(), SP.getUserIdKey(getContext(), SP.TASK_GUIDE_PARSE), parse);
                                return;
                            }
                        }
                        for (int i = 0; i < taskList.size(); i++) {
                            com.alibaba.fastjson.JSONObject model = taskList.getJSONObject(i);
                            int id = model.getIntValue("id");
                            if (id == parse + 1000) {
                                SP.set(getContext(), SP.getUserIdKey(getContext(), SP.TASK_GUIDE_PARSE_ID), id);
                                SP.set(getContext(), SP.getUserIdKey(getContext(), SP.TASK_GUIDE_PARSE_CONTENT), model.getString("content"));
                                TaskTools.showStagetTask(getActivity());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, errorListener);
    }

}
