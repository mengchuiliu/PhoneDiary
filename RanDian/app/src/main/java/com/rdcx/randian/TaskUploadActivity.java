package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.bean.DiaryImageInfo;
import com.rdcx.service.NetManager;
import com.rdcx.tools.DB;
import com.rdcx.tools.MessageTools;
import com.rdcx.tools.PermissionTools;
import com.rdcx.tools.SP;
import com.rdcx.utils.Constants;
import com.rdcx.utils.ImageDisplayer;
import com.rdcx.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/4/27 0027.
 * 任务清单中，需要手动验证的任务在这里进行验证上传操作
 */
public class TaskUploadActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final int TAKE_PICTURE = 0x000000;
    private static final int SELECT_PICTURE = 0x000001;

    private boolean needPointSave = false;// 返回时是否需要提示“任务还未保存”
    private String content;
    private int id;
    private int camera;
    private boolean firstShowDatePickerDialog;
    private String cameraPhotoPath;
    private long customDateMillions;

    private ImageView taskUploadAddImageView;
    private EditText taskUploadDateEt;
    private EditText taskUploadPeopleEt;
    private EditText taskUploadEventEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_upload);
        initParams();
        initView();
        queryTaskPhoto();
    }

    private void initParams() {
        Intent intent = getIntent();
        if (intent != null) {
            content = intent.getStringExtra("content");
            id = intent.getIntExtra("id", 0);
            camera = intent.getIntExtra("camera", 0);
        } else {
            content = "平凡的人生";
            id = 0;
            camera = 0;
        }
        firstShowDatePickerDialog = true;
    }

    private void initView() {
        TextView taskTitleTv = (TextView) findViewById(R.id.task_title_tv);
        taskTitleTv.setText(content);

        findViewById(R.id.set_back).setOnClickListener(this);
        findViewById(R.id.task_finish_tv).setOnClickListener(this);

        taskUploadDateEt = (EditText) findViewById(R.id.task_upload_date_et);
        taskUploadDateEt.setKeyListener(null);
        taskUploadDateEt.setOnClickListener(this);
        taskUploadDateEt.setOnFocusChangeListener(this);

        taskUploadPeopleEt = (EditText) findViewById(R.id.task_upload_people_et);
        taskUploadEventEt = (EditText) findViewById(R.id.task_upload_event_et);

        taskUploadAddImageView = (ImageView) findViewById(R.id.task_upload_add);
        taskUploadAddImageView.setOnClickListener(this);
    }

    private void queryTaskPhoto() {
        MessageTools.showTaskLoading(this, "");
        String userId = SP.getString(this, SP.USER_ID, "");
        DB.getDataInterface(this).findTaskPhotoByUserId(userId, String.valueOf(id), new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if ("000000".equals(jo.getString("resp")) && jo.getJSONObject("model") != null) {
                        Log.d("test", "TaskUploadActivity queryTaskPhoto jsonArray=>:" + jsonArray);
                        JSONObject model = jo.getJSONObject("model");
                        // 状态 0：未审核，1：已通过，2：未通过
                        int status = model.getInt("status");
                        if (status == 2) {
                            return;
                        }
                        // 图片
                        String photoPath = model.getString("photoPath");
                        Utils.getImage(Constants.head_url + photoPath.replace("\\", "/"), handler, 1);
                        taskUploadAddImageView.setOnClickListener(null);
                        // 去掉“添加照片”这几个字
                        findViewById(R.id.task_upload_add_photo).setVisibility(View.GONE);
                        // 日期
                        String customDate = model.getString("customDate");
                        long time = Long.parseLong(customDate);
                        if (time > 0) {
                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(time);
                            taskUploadDateEt.setText(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH));
                        }
                        taskUploadDateEt.setOnClickListener(null);
                        taskUploadDateEt.setOnFocusChangeListener(null);
                        // 人物
                        String figure = model.getString("figure");
                        taskUploadPeopleEt.setText(figure);
                        taskUploadPeopleEt.setKeyListener(null);
                        // 事件
                        String event = model.getString("event");
                        taskUploadEventEt.setText(event);
                        taskUploadEventEt.setKeyListener(null);
                        // 去掉完成按钮
                        findViewById(R.id.task_finish_tv).setVisibility(View.GONE);
                        // 返回按钮不需要提示是否保存
                        needPointSave = false;
                    }
                } catch (Exception e) {
                    Log.e("test", "TaskUploadActivity queryTaskPhoto cause an Exception=>:", e);
                } finally {
                    MessageTools.hideTaskLoading();
                }
            }
        }, errorFunc);
    }

    @Override
    public void onClick(View v) {
        Log.d("test", "TaskUploadActivity onClick v=>:" + v);
        switch (v.getId()) {
            case R.id.set_back:
                if (needPointSave) {
                    MessageTools.showTaskConfirm(this, "任务还没保存，确认放弃？", "确定", new MessageTools.OnClick() {
                        @Override
                        public void onClick() {
                            finish();
                        }
                    }, "取消", null);
                } else {
                    finish();
                }
                break;
            case R.id.task_upload_date_et:
                showDatePickerDialog();
                break;
            case R.id.task_finish_tv:
                String paramMsg = checkParams();
                if (paramMsg == null) {
                    MessageTools.showTaskLoading(this, null);
                    upload();
                } else {
                    Toast.makeText(this, paramMsg, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.task_upload_add:
                String photoWindowText;
                MessageTools.OnClick photoWindowOnClick;

                if (camera == 1) {
                    photoWindowText = null;
                    photoWindowOnClick = null;
                } else {
                    photoWindowText = "从相册选择";
                    photoWindowOnClick = new MessageTools.OnClick() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(TaskUploadActivity.this, AlbumChooseActivity.class);
                            intent.putExtra("AvailableSize", 1);
                            startActivityForResult(intent, SELECT_PICTURE);
                        }
                    };
                }

                MessageTools.showPhotoWindow(this, "拍照", new MessageTools.OnClick() {
                    @Override
                    public void onClick() {
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (PermissionTools.checkPermission(TaskUploadActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, TaskUploadActivity.this.getString(R.string.permission_external_storage), true)) {
                            File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/iamge");
                            if (file.exists() || file.mkdirs()) {
                                File vFile = new File(Environment.getExternalStorageDirectory() + "/ZhangXin/iamge", String.valueOf(System.currentTimeMillis()) + ".jpg");
                                cameraPhotoPath = vFile.getPath();
                                Uri cameraUri = Uri.fromFile(vFile);
                                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                                startActivityForResult(openCameraIntent, TAKE_PICTURE);
                            }
                        } else {
                            Toast.makeText(TaskUploadActivity.this, TaskUploadActivity.this.getString(R.string.no_permission_external_storage), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, photoWindowText, photoWindowOnClick, "取消", null);
                break;
            default:
                break;
        }
    }

    /**
     * {@link #taskUploadAddImageView} 是否选择了一张图片
     * <p/>
     * true：已经选择了一张图片
     * false：尚未选择图片，当前显示默认图片
     */
    private boolean taskUploadAddImageViewAdded = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PICTURE:
                if (new File(cameraPhotoPath).exists()) {
                    ImageDisplayer.getInstance(this).displayBmp(taskUploadAddImageView, null, cameraPhotoPath);
                    taskUploadAddImageView.setTag(cameraPhotoPath);
                    taskUploadAddImageViewAdded = true;
                    needPointSave = true;
                } else {
                    taskUploadAddImageView.setImageResource(R.mipmap.task_upload_add);
                    taskUploadAddImageViewAdded = false;
                    needPointSave = false;
                }
                break;
            case SELECT_PICTURE:
                if (data != null) {
                    List<DiaryImageInfo> incomingDataList = (List<DiaryImageInfo>) data.getSerializableExtra("image_list");
                    if (incomingDataList != null && incomingDataList.size() > 0) {
                        DiaryImageInfo diaryImageInfo = incomingDataList.get(0);
                        ImageDisplayer.getInstance(this).displayBmp(taskUploadAddImageView, diaryImageInfo.thumbnailPath, diaryImageInfo.sourcePath);
                        taskUploadAddImageView.setTag(diaryImageInfo.sourcePath);
                        taskUploadAddImageViewAdded = true;
                        needPointSave = true;
                    } else {
                        taskUploadAddImageView.setImageResource(R.mipmap.task_upload_add);
                        taskUploadAddImageViewAdded = false;
                        needPointSave = false;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.task_upload_date_et:
                if (hasFocus) {
                    showDatePickerDialog();
                }
                break;
            default:
                break;
        }
    }

    private void showDatePickerDialog() {
        if (firstShowDatePickerDialog) {
            firstShowDatePickerDialog = false;
            return;
        }
        MessageTools.showDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                String dateStr = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                taskUploadDateEt.setText(dateStr);

                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                customDateMillions = c.getTimeInMillis();
            }
        });
    }

    /**
     * 参数检查
     * {@link #taskUploadAddImageView} 是否选择了一张图片
     * {@link #taskUploadDateEt} 日期是否有值
     * {@link #taskUploadPeopleEt} 人物是否有值
     * {@link #taskUploadEventEt} 事件是否有值
     *
     * @return 上述控件都有值时返回空，其中一个没有值时返回相应的出错信息
     */
    private String checkParams() {
        if (!taskUploadAddImageViewAdded) {
            return "请先添加一张照片";
        }
        return null;
    }

    private void upload() {
        FileOutputStream fops = null;
        File file;
        try {
            Drawable drawable = taskUploadAddImageView.getDrawable();
            if (drawable != null) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                file = new File(Environment.getExternalStorageDirectory() + "/ZhangXin/cache/task.png");
                if (file.exists() || file.createNewFile()) {
                    Log.d("test", "TaskUploadActivity upload tempFileName=>:" + file.getAbsolutePath());
                    fops = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fops);
                    Utils.uploadFile(handler, file, Constants.PHOTO_UPLOAD, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fops != null) {
                    fops.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (msg.obj.toString() == null || msg.obj.toString().equals("")) {
                        if (Utils.isNetworkAvailable(TaskUploadActivity.this)) {
                            Toast.makeText(TaskUploadActivity.this, "网络异常!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TaskUploadActivity.this, "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(msg.obj.toString());
                        Log.d("test", "TaskUploadActivity handler handleMessage jsonArray=>:" + jsonArray);
                        JSONObject object = jsonArray.getJSONObject(0);
                        final String resp = object.getString("resp");
                        final String photoPath = object.getString("filePath");
                        if (resp.equals("000000")) {
                            String userId = SP.getString(TaskUploadActivity.this, SP.USER_ID, "");
                            String taskId = String.valueOf(id);
                            String customDate = String.valueOf(customDateMillions);
                            String figure = taskUploadPeopleEt.getText().toString();
                            String event = taskUploadEventEt.getText().toString();
                            DB.getDataInterface(TaskUploadActivity.this).addTaskPhoto(userId, taskId, photoPath, customDate, figure, event, success, errorFunc);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1: // 从网络获取图片之后刷新
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) {
                            taskUploadAddImageView.setImageBitmap(bitmap);
                        }
                    } else {
                        Toast.makeText(TaskUploadActivity.this, "图片加载失败。", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private NetManager.DataArray success = new NetManager.DataArray() {
        @Override
        public void getServiceData(JSONArray jsonArray) {
            try {
                JSONObject obj = jsonArray.getJSONObject(0);
                if ("000000".equals(obj.getString("resp"))) {
                    setResult(id);
                    MessageTools.showTaskAlert(TaskUploadActivity.this, "照片已成功提交，正在审核。\n审核通过后任务完成。", "确定", new MessageTools.OnClick() {
                        @Override
                        public void onClick() {
                            queryTaskPhoto();
                        }
                    });
                } else if ("000350".equals(obj.getString("resp"))) {
                    String msg = obj.getString("msg");
                    MessageTools.showTaskAlert(TaskUploadActivity.this, msg, "确定", null);
                } else {
                    MessageTools.showTaskAlert(TaskUploadActivity.this, "照片提交失败，请稍候再试。", "确定", null);
                }
            } catch (Exception e) {
                Log.e("test", "TaskUploadActivity success cause an Exception=>:", e);
            } finally {
                MessageTools.hideTaskLoading();
            }
        }
    };

    /**
     * 访问网络出现异常时调用该对象
     */
    private Response.ErrorListener errorFunc = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (Utils.isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "网络异常，请稍候再试。", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
            }
            MessageTools.hideTaskLoading();
        }
    };

}
