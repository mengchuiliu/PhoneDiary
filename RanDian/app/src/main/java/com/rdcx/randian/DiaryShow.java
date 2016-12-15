package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.bean.DiaryImageInfo;
import com.rdcx.myview.DiaryImageAdapter;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.Diary;
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
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("HandlerLeak")
public class DiaryShow extends AppCompatActivity implements View.OnClickListener {
    HashMap<Integer, String> hashMap = new HashMap<>();//保存用户需要的维度
    private GridView mGridView;
    public static List<DiaryImageInfo> mDataList = new ArrayList<>();
    DiaryImageAdapter mAdapter;
    private PopupWindow popWindow;
    TextView showTitle, complete;
    EditText showText;
    boolean isEditor, noEditor;
    Diary diary;
    String diaryPath, servicePath;
    RelativeLayout reDiary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_show);
        ((MyApplication) getApplication()).addActivity(this);
        diaryPath = Environment.getExternalStorageDirectory() + "/ZhangXin/ScreenShot/DiaryScreen.png";

        hashMap = (HashMap<Integer, String>) getIntent().getSerializableExtra("map");

        boolean clear = getIntent().getBooleanExtra("clear", false);
        if (clear) {
            mDataList.clear();
        }

        isEditor = getIntent().getBooleanExtra("isEditor", false);
        diary = (Diary) getIntent().getSerializableExtra("diary");
        initData();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        mAdapter.notifyDataSetChanged();
    }

    private void initData() {
        if (isEditor) {
            if (!noEditor) {
                mDataList.clear();
                try {
                    org.json.JSONArray jsonArray = new org.json.JSONArray(diary.path);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (jsonArray.optJSONObject(0) == null) {
                            DiaryImageInfo info = new DiaryImageInfo();
                            info.sourcePath = jsonArray.getString(i);
                            mDataList.add(info);
                        } else {
                            org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                            DiaryImageInfo info = new DiaryImageInfo();
                            info.sourcePath = jsonObject.getString("locaPath");
                            mDataList.add(info);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void init() {
        reDiary = (RelativeLayout) findViewById(R.id.re_diary);
        mGridView = (GridView) findViewById(R.id.gv_icon);
        mAdapter = new DiaryImageAdapter(this);
        mAdapter.setData(mDataList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == (mDataList == null ? 0 : mDataList.size())) {
                    showPopupWindow(mGridView, true);
                } else {
                    Intent intent = new Intent(DiaryShow.this, ImageZoomActivity.class);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            }
        });
        showTitle = (TextView) findViewById(R.id.show_title);
        showText = (EditText) findViewById(R.id.show_text);
        complete = (TextView) findViewById(R.id.show_complete);
        complete.setOnClickListener(this);
        findViewById(R.id.show_back).setOnClickListener(this);

        if (hashMap != null && hashMap.size() > 0) {
            showTitle.setVisibility(View.VISIBLE);
        } else {
            showTitle.setVisibility(View.GONE);
        }

        if (isEditor) {
            if (TextUtils.isEmpty(diary.datatext)) {
                showTitle.setVisibility(View.GONE);
            } else {
                showTitle.setVisibility(View.VISIBLE);
            }
            showTitle.setText(diary.datatext);
            if (!noEditor) {
                showText.setText(diary.text);
                showText.setEnabled(false);
                complete.setText("编辑");
                mGridView.setEnabled(false);
                mGridView.setFocusable(false);
            } else {
                showText.setSelection(diary.text.length());
            }
        } else {
            if (hashMap != null) {
                showTitle.setText(getDimenTitle(hashMap));
            }
        }
    }


    AlertDialog dialog;

    private void showPopupWindow(View parent, boolean b) {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_select_photo, null);
        popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);
        TextView photograph = (TextView) view.findViewById(R.id.photograph);
        TextView albums = (TextView) view.findViewById(R.id.albums);
        view.findViewById(R.id.cancel).setOnClickListener(this);// 取消;
        if (b) {
            photograph.setOnClickListener(this);// 拍照
            albums.setOnClickListener(this);// 相册
            popWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        } else {
            photograph.setText("保存");
            photograph.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popWindow.dismiss();
                    getServiceTime(1);
                }
            });

            albums.setText("保存并分享");
            albums.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popWindow.dismiss();
                    getServiceTime(3);
                }
            });
        }
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    private void getServiceTime(final int what) {
        progressDialog = ProgressDialog.show(DiaryShow.this, "", "日记生成中...");
        progressDialog.show();
        DB.getDataInterface(DiaryShow.this).getCurTime(SP.getString(DiaryShow.this, SP.USER_ID, ""),
                new NetManager.DataArray() {
                    @Override
                    public void getServiceData(org.json.JSONArray jsonArray) {
                        try {
                            org.json.JSONObject jo = jsonArray.getJSONObject(0);
                            if ("000000".equals(jo.getString("resp"))) {

                                TaskTools.addStageTask(DiaryShow.this, 1004); // 完成阶段任务 1004：写 1 篇手机日记

                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putLong("servierTime", jo.getJSONObject("nowDate").getLong("time"));
                                message.what = what;
                                message.setData(bundle);
                                handler.sendMessage(message);
                            } else {
                                handler.sendEmptyMessage(what);
                            }
                        } catch (Exception e) {
                            handler.sendEmptyMessage(what);
                            Log.e("liu_test", "getCurTime向服务请求时返回值不正确->:" + e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        handler.sendEmptyMessage(what);
                        Log.e("liu_test", "getCurTime volleyError->:" + volleyError);
                    }
                });
    }

    private void showView(View view) {
        view.findViewById(R.id.diary_cover).setOnClickListener(this);
        view.findViewById(R.id.diary_qq).setOnClickListener(this);
        view.findViewById(R.id.diary_wx).setOnClickListener(this);
        view.findViewById(R.id.diary_wb).setOnClickListener(this);
        view.findViewById(R.id.diary_cancel).setOnClickListener(this);
    }

    /**
     * @ param isUp新增（false）或者更新(true)
     * @ return
     */
    private boolean uploadData(boolean isUp, long servierTime) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < mDataList.size(); i++) {
            if (!TextUtils.isEmpty(mDataList.get(i).sourcePath)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("locaPath", mDataList.get(i).sourcePath);
                jsonObject.put("servicePath", "");
                jsonObject.put("isUpload", "0");
                // 返回一个JSONArray对象
                jsonArray.add(i, jsonObject);
            }
            if (!TextUtils.isEmpty(mDataList.get(i).thumbnailPath)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("locaPath", mDataList.get(i).thumbnailPath);
                jsonObject.put("servicePath", "");
                jsonObject.put("isUpload", "0");
                // 返回一个JSONArray对象
                jsonArray.add(i, jsonObject);
            }
        }
        String str = showText.getText().toString().trim();
        if (servierTime == 0) {
            servierTime = System.currentTimeMillis();
        }
        if (isUp) {
            //diary.upload = 0;
            diary.text = str;
            diary.time = System.currentTimeMillis();
            diary.path = jsonArray.toString();
            diary.nowDate = servierTime;
            return DB.insertOrUpdateDiary(DiaryShow.this, diary);
        } else {
            Diary diaryAdd = new Diary();
            diaryAdd.text = str;
            diaryAdd.time = System.currentTimeMillis();
            diaryAdd.nowDate = servierTime;
            diaryAdd.path = jsonArray.toString();
            diaryAdd.data = JSONObject.toJSONString(hashMap);
            diaryAdd.datatext = getDimenTitle(hashMap);
            return DB.insertOrUpdateDiary(DiaryShow.this, diaryAdd);
        }
    }

    //根据维度组装显示数据
    private String getDimenTitle(HashMap<Integer, String> hashMap) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Integer key : hashMap.keySet()) {
            i++;
            builder.append(getDimenData(key, hashMap.get(key))).append(";  ");
            if (i % 2 == 0 && i != hashMap.size()) {
                builder.append("\n");
            }
        }
//        if (builder.length() > 1) {
//            builder.deleteCharAt(builder.length() - 1);
//        }
        return builder.toString();
    }

    public static String getDimenData(int key, String data) {
        long l = Long.parseLong(data);
        switch (key) {
            case 0:
                return "通话时长" + DB.getTimeBySecond(l);
            case 1:
                return "通话次数" + data + "次";
            case 2:
                return "拍摄照片" + data + "张";
            case 3:
                return "社交聊天" + DB.getTimeBySecond(l);
            case 4:
                return "网上购物" + DB.getTimeBySecond(l);
            case 5:
                return "走过城市" + data + "个";
            case 6:
                return "使用应用" + data + "个";
            case 7:
                return "手机工作" + DB.getTimeBySecond(l);
            default:
                return "";
        }
    }


    ProgressDialog pd = null;
    boolean isFirst = true, isCover = true, isCover2 = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_back:
                DiaryShow.this.finish();
                break;
            case R.id.show_complete://完成
                if (isEditor) {
                    if (isFirst && !noEditor) {
                        isFirst = false;
                        complete.setText("完成");
                        showText.setEnabled(true);
                        showText.setSelection(diary.text.length());
                        mGridView.setEnabled(true);
                        mGridView.setFocusable(true);
                        return;
                    }
                }
                String str = showText.getText().toString().trim();
                String str2 = showTitle.getText().toString().trim();
                if (mDataList.size() == 0 && TextUtils.isEmpty(str) && TextUtils.isEmpty(str2)) {
                    Toast.makeText(DiaryShow.this, "内容不能为空！", Toast.LENGTH_SHORT).show();
                    return;//照片，文本全为空
                }
                if (pd == null) {
                    pd = ProgressDialog.show(DiaryShow.this, "", "日记生成中...", true);
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
                final Bitmap bitmap = takeScreenShot(DiaryShow.this, reDiary);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/ScreenShot");
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        if (bitmap != null) {
                            Utils.savePhotoToSDCard(bitmap, diaryPath);
                        } else {
                            Log.e("my_log", "截图失败!");
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
                break;
            case R.id.photograph://拍照
                takePhoto();
                popWindow.dismiss();
                break;
            case R.id.albums://相册
                popWindow.dismiss();
                Intent intent = new Intent(DiaryShow.this, AlbumChooseActivity.class);
                intent.putExtra("AvailableSize", getAvailableSize());
                startActivityForResult(intent, 88);
                break;
            case R.id.cancel://取消
                popWindow.dismiss();
                break;
            case R.id.diary_cover://发现
                if (isCover && !isCover2) {
                    Toast.makeText(getApplicationContext(), "分享内容生成中,请稍后再试！",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();
                if (isCover2) {
                    Toast.makeText(getApplicationContext(), "分享失败,请稍后再试！",
                            Toast.LENGTH_LONG).show();
                    saveDiary();
                    return;
                }
                pd = ProgressDialog.show(DiaryShow.this, "", "发现分享中...", true);
                new NetDataGetter(getApplicationContext()).dynamic(SP.getString(DiaryShow.this, SP.USER_ID, "-1"),
                        "", servicePath, "", "", new NetManager.DataArray() {
                            @Override
                            public void getServiceData(org.json.JSONArray jsonArray) {
                                try {
                                    org.json.JSONObject object = jsonArray.getJSONObject(0);
                                    String resp = object.getString("resp");
                                    if (resp.equals("000000")) {
                                        //分享成功
                                        Toast.makeText(DiaryShow.this, "日记保存分享成功!", Toast.LENGTH_SHORT).show();
                                        saveDiary();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "发现分享失败,请稍后再试！",
                                                Toast.LENGTH_LONG).show();
                                        saveDiary();
                                    }
                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Toast.makeText(getApplicationContext(),
                                        "系统繁忙，发现分享失败!", Toast.LENGTH_SHORT).show();
                                saveDiary();
                            }
                        });
                break;
            case R.id.diary_qq://QQ
                if (isCover && !isCover2) {
                    Toast.makeText(getApplicationContext(), "分享内容生成中,请稍后再试！",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                dialogCancel();
                //dialog.dismiss();
                if (isCover2) {
                    Toast.makeText(getApplicationContext(), "分享失败,请稍后再试！",
                            Toast.LENGTH_LONG).show();
                    saveDiary();
                    return;
                }
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "手机日记•分享");// 必填
                // 必填,点击分享内容所需要的链接地址
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, Constants.head_url + servicePath);
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(diaryPath);
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);// 分享的图片，选填
                MyApplication.mTencent.shareToQzone(DiaryShow.this, params, new IUiListener() {
                    @Override
                    public void onError(UiError error) {
                        Toast.makeText(getApplicationContext(), "QQ分享失败！", Toast.LENGTH_SHORT).show();
                        saveDiary();
                    }

                    @Override
                    public void onComplete(Object response) {
                        Toast.makeText(getApplicationContext(), "日记本QQ分享成功！", Toast.LENGTH_SHORT).show();
                        saveDiary();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "QQ分享取消！", Toast.LENGTH_SHORT).show();
                        saveDiary();
                    }
                });
                break;
            case R.id.diary_wx://微信
                dialogCancel();
                MyApplication.flag = false;
                MyApplication.wxType = 3;
                Bitmap thumb;
                WXImageObject imgObj;
                thumb = BitmapFactory.decodeFile(diaryPath);
                imgObj = new WXImageObject();
                imgObj.setImagePath(diaryPath);

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
                    Toast.makeText(DiaryShow.this, "微信分享失败！", Toast.LENGTH_SHORT).show();
                }
                saveDiary();
                break;
            case R.id.diary_wb://微博
                dialogCancel();
                Intent intent1 = new Intent(DiaryShow.this, MyWeiBoShareActivity.class);
                intent1.putExtra("content", "");
                intent1.putExtra("sharePath", diaryPath);
                startActivityForResult(intent1, 66);
                break;
            case R.id.diary_cancel://取消分享
                dialog.dismiss();
                break;
        }
    }

    ProgressDialog progressDialog;

    private void dialogCancel() {
        dialog.dismiss();
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(DiaryShow.this, "", "日记分享中...");
        }
        progressDialog.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 1000);
    }

    private void saveDiary() {
        setResult(-1);
        DiaryShow.this.finish();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (pd != null) {
                        pd.dismiss();
                        pd = null;
                    }
                    showPopupWindow(mGridView, false);
                    break;
                case 2:
                    if (msg.obj.toString() == null || msg.obj.toString().equals("")) {
                        if (Utils.isNetworkAvailable(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), "分享发现失败,请稍后再试！",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
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
                        } else {
                            isCover2 = true;
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3://日记保存本分享
                    progressDialog.dismiss();
                    //SP.set(DiaryShow.this, "YesterDayState", false);
                    Bundle bundle = msg.getData();
                    long servierTime = bundle.getLong("servierTime", 0);
                    boolean isSuccess = uploadData(isEditor, servierTime);
                    if (!isSuccess) {
                        Toast.makeText(DiaryShow.this, "日记保存失败!请稍后重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File f = new File(diaryPath);
                    if (f.exists()) {
                        Utils.uploadFile(handler, f, Constants.PHOTO_UPLOAD, 2);
                        View view = LayoutInflater.from(DiaryShow.this).inflate(R.layout.diary_share_view, null);
                        showView(view);
                        if (dialog == null) {
                            dialog = new AlertDialog.Builder(DiaryShow.this).create();
                        }
                        dialog.show();
                        Window window = dialog.getWindow();
                        window.setGravity(Gravity.CENTER);
                        window.setContentView(view);
                        dialog.setCanceledOnTouchOutside(false);
                    } else {
                        Toast.makeText(DiaryShow.this, "日记图片分享失败!请稍后重试", Toast.LENGTH_SHORT).show();
                        saveDiary();
                        Log.e("my_log", "截图失败!");
                    }
                    break;
                case 1://日记保存
                    //SP.set(DiaryShow.this, "YesterDayState", false);
                    progressDialog.dismiss();
                    Bundle bundle1 = msg.getData();
                    long servierTime1 = bundle1.getLong("servierTime", 0);
                    boolean isSuccess1 = uploadData(isEditor, servierTime1);
                    if (isSuccess1) {
                        Toast.makeText(DiaryShow.this, "日记保存成功!", Toast.LENGTH_SHORT).show();
                        setResult(-1);
                        DiaryShow.this.finish();
                    } else {
                        Toast.makeText(DiaryShow.this, "日记保存失败!请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private static final int TAKE_PICTURE = 0x000000;
    private String path = "";

    public void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/iamge");
        if (!file.exists()) {
            file.mkdirs();
        }

        File vFile = new File(Environment.getExternalStorageDirectory()
                + "/ZhangXin/iamge", String.valueOf(System.currentTimeMillis()) + ".jpg");
        path = vFile.getPath();
        Uri cameraUri = Uri.fromFile(vFile);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }


    //获取可上传图片张数
    private int getAvailableSize() {
        int availSize = Constants.MAX_IMAGE_SIZE - mDataList.size();
        if (availSize >= 0) {
            return availSize;
        }
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApplication.mTencent.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE) {
            if (mDataList.size() < Constants.MAX_IMAGE_SIZE
                    && resultCode == -1 && !TextUtils.isEmpty(path)) {
                DiaryImageInfo item = new DiaryImageInfo();
                item.sourcePath = path;
                mDataList.add(item);
            }
        }
        switch (resultCode) {
            case 6:
                if (data != null) {
                    noEditor = data.getBooleanExtra("noEditor", false);
                    initData();
                    List<DiaryImageInfo> incomingDataList = (List<DiaryImageInfo>) data.getSerializableExtra("image_list");
                    if (incomingDataList != null) {
                        mDataList.addAll(incomingDataList);
                    }
                    mAdapter.setData(mDataList);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case MyWeiBoShareActivity.WB_Result_ok:
            case MyWeiBoShareActivity.WB_Result_no:
                saveDiary();
                break;
        }

    }

    // 获取指定Activity的截屏，保存到png文件
    private Bitmap takeScreenShot(Activity activity, RelativeLayout reDiary) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top + reDiary.getHeight();

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
