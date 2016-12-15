package com.rdcx.randian;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.PermissionTools;
import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import clipheadphoto.ClipActivity;

@SuppressWarnings("ALL")
public class UpdateUserInfo extends AppCompatActivity implements View.OnClickListener {
    ImageView update_icon;
    EditText update_name;
    String iconPath, nickName, userId;
    private PopupWindow popWindow;
    private LayoutInflater layoutInflater;
    public static final int PHOTOZOOM = 0; // 相册
    public static final int PHOTOTAKE = 1; // 拍照
    public static final int IMAGE_COMPLETE = 2; // 结果

    private String photoSaveName;// 图片名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);
        ((MyApplication) getApplication()).addActivity(this);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
    }

    private String getPhotoSavePath() {
        if (PermissionTools.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_external_storage), true)) {
            File file = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera");
            if (file.exists() || file.mkdir()) {
                return file.getPath() + File.separator;
            }
        }
        return null;
    }

    private void initView() {
        update_icon = (ImageView) findViewById(R.id.update_icon);
        update_name = (EditText) findViewById(R.id.update_name);
        findViewById(R.id.info_back).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);
        findViewById(R.id.waterWave).setOnClickListener(this);
        update_icon.setImageBitmap(Utils.getPortraitBitmap(this, handler, true));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_back:
                UpdateUserInfo.this.finish();
                break;
            case R.id.complete://完成
                final String name = update_name.getText().toString().trim();
                if (name.equals(nickName)) {
                    finish();
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "昵称不能为空！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                DataInterface dataInterface = new NetDataGetter(getApplicationContext());
                dataInterface.updateName(SP.getString(UpdateUserInfo.this, SP.USER_ID, "-1"),
                        SP.getString(UpdateUserInfo.this, "phoneNumber", ""), name,
                        new NetManager.DataArray() {
                            @Override
                            public void getServiceData(JSONArray jsonArray) {
                                try {
                                    JSONObject object = jsonArray.getJSONObject(0);
                                    String resp = object.getString("resp");
                                    String str = object.getString("msg");
                                    if (resp.equals("000000")) {

                                        TaskTools.addStageTask(UpdateUserInfo.this, 1002); // 完成阶段任务 1002：修改昵称一次

                                        Toast.makeText(getApplicationContext(), "昵称修改成功",
                                                Toast.LENGTH_SHORT).show();
                                        SP.set(UpdateUserInfo.this, "nickName", name);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                if (Utils.isNetworkAvailable(getApplicationContext())) {
                                    Toast.makeText(getApplicationContext(),
                                            "网络异常!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
            case R.id.waterWave://头像
                showPopupWindow(update_icon);
                break;
            case R.id.photograph://拍照
                popWindow.dismiss();
                photoSaveName = String.valueOf(System.currentTimeMillis()) + ".jpg";
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                openCameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                String photoSavePath = getPhotoSavePath();
                if (photoSavePath == null) {
                    Toast.makeText(getApplication(), getResources().getString(R.string.permission_external_storage), Toast.LENGTH_LONG).show();
                } else {
                    // 下面这句指定调用相机拍照后的照片存储的路径
                    Uri imageUri = Uri.fromFile(new File(photoSavePath, photoSaveName));
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(openCameraIntent, PHOTOTAKE);
                }
                break;
            case R.id.albums://相册
                popWindow.dismiss();
                if (getPhotoSavePath() == null) {
                    Toast.makeText(getApplication(), getResources().getString(R.string.permission_external_storage), Toast.LENGTH_LONG).show();
                } else {
                    // 下面这句指定调用相机拍照后的照片存储的路径
                    Intent openAlbumIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(openAlbumIntent, PHOTOZOOM);
                }
                break;
            case R.id.cancel://取消
                popWindow.dismiss();
                break;
            default:
                break;
        }
    }

    /**
     * 选择图片方式(相册和照相)
     *
     * @param parent
     */
    private void showPopupWindow(View parent) {
        View view = layoutInflater.inflate(R.layout.pop_select_photo, null);
        if (popWindow == null) {
            popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, true);
        }
        view.findViewById(R.id.photograph).setOnClickListener(this);// 拍照
        view.findViewById(R.id.albums).setOnClickListener(this);// 相册
        view.findViewById(R.id.cancel).setOnClickListener(this);// 取消
        popWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri uri;
        switch (requestCode) {
            case PHOTOZOOM:// 相册
                if (data == null || data.getData() == null || data.getData().getPath() == null) {
                    return;
                }
                String path = data.getData().getPath();
                if (!new File(path).exists()) {
                    uri = data.getData();
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    path = cursor.getString(column_index);// 图片在的路径
                }
                Intent intent3 = new Intent(UpdateUserInfo.this, ClipActivity.class);
                intent3.putExtra("path", path);
                startActivityForResult(intent3, IMAGE_COMPLETE);
                break;
            case PHOTOTAKE:// 拍照
                String photoPath = getPhotoSavePath() + photoSaveName;
                Intent intent2 = new Intent(UpdateUserInfo.this, ClipActivity.class);
                intent2.putExtra("path", photoPath);
                startActivityForResult(intent2, IMAGE_COMPLETE);
                break;
            case IMAGE_COMPLETE://剪辑返回的头像
                final String tempPath = data.getStringExtra("path");
                if (TextUtils.isEmpty(tempPath)) {
                    if (TextUtils.isEmpty(iconPath)) {
                        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(
                                R.mipmap.default_portrait)).getBitmap();
                        update_icon.setImageBitmap(bitmap);
                    } else {
                        String locpath = Environment.getExternalStorageDirectory() +
                                "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
                        File file = new File(locpath);
                        if (file.exists()) {
                            update_icon.setImageBitmap(Utils.toRoundBitmap(BitmapFactory.decodeFile(locpath)));
                        } else {
                            Utils.getImage(Constants.head_url + iconPath.replace("\\", "/"), handler, 0);
                        }
                    }
                } else {
                    update_icon.setImageBitmap(Utils.toRoundBitmap(BitmapFactory.decodeFile(tempPath)));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 16:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Utils.setPortraitBitmap(UpdateUserInfo.this, bitmap);
                    } else {
                        Log.e("my_log", "头像加载失败！");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        nickName = SP.getString(UpdateUserInfo.this, "nickName", "");
        userId = SP.getString(UpdateUserInfo.this, SP.USER_ID, "-1");
        if (TextUtils.isEmpty(nickName)) {
            update_name.setHint(Build.MODEL);
        } else {
            update_name.setText(nickName);
        }

        update_icon.setImageBitmap(Utils.getPortraitBitmap(this, handler, false));
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
