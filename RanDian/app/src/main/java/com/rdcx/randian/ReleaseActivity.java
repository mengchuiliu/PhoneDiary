package com.rdcx.randian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
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
import java.util.ArrayList;

@SuppressLint("HandlerLeak")
public class ReleaseActivity extends AppCompatActivity implements View.OnClickListener {
    EditText ed_release;
    ImageView release_image;
    TextView tv_location;
    final String pathName = Environment.getExternalStorageDirectory() + "/ZhangXin/cache/scanCapture.png";
    String path, locText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release);
        ((MyApplication) getApplication()).addActivity(this);

        File f = new File(pathName);
        if (f.exists()) {
            Utils.uploadFile(handler, f, Constants.PHOTO_UPLOAD, 0);
        } else {
            Log.e("my_log", "截图失败分享失败!");
        }
        init();
    }

    private void init() {
        findViewById(R.id.re_weixin).setOnClickListener(this);
        findViewById(R.id.re_qq).setOnClickListener(this);
        findViewById(R.id.re_weibo).setOnClickListener(this);
        findViewById(R.id.release_back).setOnClickListener(this);
        ed_release = (EditText) findViewById(R.id.ed_release);

        tv_location = (TextView) findViewById(R.id.tv_location);
        release_image = (ImageView) findViewById(R.id.release_image);

        if ((new File(pathName).exists())) {
            release_image.setImageBitmap(BitmapFactory.decodeFile(pathName));
        } else {
            release_image.setImageResource(R.mipmap.randian);
        }

        findViewById(R.id.ll_location).setOnClickListener(this);
    }

    long lastClick;
    boolean isShared = true;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.release_back:
                finish();
                break;
            case R.id.ll_location:// 显示位置
                Intent intent = new Intent(ReleaseActivity.this, GetLocationActivity.class);
                startActivityForResult(intent, 8);
                break;
            case R.id.re_weixin:
                if (System.currentTimeMillis() - lastClick < 2000) {
                    return;
                }
                lastClick = System.currentTimeMillis();
                MyApplication.flag = false;
                MyApplication.wxType = 1;
                Bitmap thumb;
                WXImageObject imgObj;
                if ((new File(pathName).exists())) {
                    thumb = BitmapFactory.decodeFile(pathName);
                    imgObj = new WXImageObject();
                    imgObj.setImagePath(pathName);
                } else {
                    thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.randian);
                    imgObj = new WXImageObject(thumb);
                }

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;
                Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 150, 150, true);
                thumb.recycle();
                msg.thumbData = Utils.bmpByte(thumbBmp, true); // 设置缩略图

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = Utils.buildTransaction("img");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;

//                String str = ed_release.getText().toString().trim();
//                WXWebpageObject webpage = new WXWebpageObject();
//                webpage.webpageUrl = Constants.head_url + path;
//                WXMediaMessage msg = new WXMediaMessage(webpage);
//                msg.title = "我的掌心分享" + "\n" + str;
//                msg.description = "掌心分享";
//
//                Log.e("my_log", "pathName==>" + pathName);
//                Bitmap thumb;
//                if ((new File(pathName).exists())) {
//                    Bitmap bmp = BitmapFactory.decodeFile(pathName);
//                    thumb = Bitmap.createScaledBitmap(bmp, 150, 300, true);
//                    bmp.recycle();
//                } else {
//                    thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.randian);
//                }
//                // // 这里替换一张自己工程里的图片资源
//                // Bitmap thumb = BitmapFactory.decodeResource(getResources(),
//                // R.drawable.randian);
//                msg.thumbData = bmpByte(thumb, true);
//
//                SendMessageToWX.Req req = new SendMessageToWX.Req();
//                req.transaction = buildTransaction("webpage");
//                req.message = msg;
//                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                boolean fla = MyApplication.api.sendReq(req);

                if (TextUtils.isEmpty(path)) {
                    SystemClock.sleep(500);
                }
                String str = ed_release.getText().toString().trim();
                if (isShared) {
                    locShare(str, path, locText, "2");
                    isShared = false;
                }
                Log.e("my_log", "fla====" + fla);
                if (!fla) {
                    Toast.makeText(ReleaseActivity.this, "微信分享失败！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.re_qq:
                if (System.currentTimeMillis() - lastClick < 2000) {
                    return;
                }
                lastClick = System.currentTimeMillis();
                SystemClock.sleep(500);
                // 分享类型
                String str1 = ed_release.getText().toString().trim();
                final Bundle params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "手机日记•分享");// 必填
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, str1);// 选填
                // 必填,点击分享内容所需要的链接地址
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(getApplicationContext(), "图片生成中,请稍后再试！", Toast.LENGTH_SHORT).show();
                    return;
                }
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, Constants.head_url + path);
                if ((new File(pathName).exists())) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(pathName);
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);// 分享的图片，选填
                }
                MyApplication.mTencent.shareToQzone(ReleaseActivity.this, params, new IUiListener() {
                    @Override
                    public void onError(UiError error) {
                        Toast.makeText(getApplicationContext(), "QQ分享失败！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(Object response) {
                        Toast.makeText(getApplicationContext(), "QQ分享成功！", Toast.LENGTH_SHORT).show();
                        ReleaseActivity.this.finish();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "QQ分享取消！", Toast.LENGTH_SHORT).show();
                    }
                });
                if (isShared) {
                    locShare(str1, path, locText, "1");
                    isShared = false;
                }
                break;
            case R.id.re_weibo:
                // 1. 初始化微博的分享消息
                String str2 = ed_release.getText().toString().trim();
                Intent intent1 = new Intent(ReleaseActivity.this, MyWeiBoShareActivity.class);
                intent1.putExtra("content", str2);
                intent1.putExtra("sharePath", pathName);
                startActivityForResult(intent1, 66);

                if (TextUtils.isEmpty(path)) {
                    SystemClock.sleep(500);
                }

                if (isShared) {
                    locShare(str2, path, locText, "3");
                    isShared = false;
                }
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (msg.obj.toString() == null || msg.obj.toString().equals("")) {
                        if (Utils.isNetworkAvailable(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), "网络异常，请稍后再试！",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(msg.obj.toString());
                        JSONObject object = jsonArray.getJSONObject(0);
                        String resp = object.getString("resp");
                        String photoPath = object.getString("filePath");
                        if (resp.equals("000000")) {
                            path = photoPath;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyApplication.mTencent.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8) {
            if (resultCode == 8) {
                tv_location.setText("位置无显示");
            } else if (resultCode == 10) {
                String str = data.getStringExtra("location");
                locText = str;
                tv_location.setText(str);
            }
        }

        if (resultCode == MyWeiBoShareActivity.WB_Result_ok) {
            ReleaseActivity.this.finish();
        }
    }

    private void locShare(final String dynamicText, final String dynamicUrl, final String address, final String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new NetDataGetter(getApplicationContext()).dynamic(SP.getString(ReleaseActivity.this, SP.USER_ID, "-1"),
                        dynamicText, dynamicUrl, address, type, new NetManager.DataArray() {
                            @Override
                            public void getServiceData(JSONArray jsonArray) {
                                try {
                                    JSONObject object = jsonArray.getJSONObject(0);
                                    String resp = object.getString("resp");
                                    if (!resp.equals("000000")) {
                                        Log.e("my_log", "本地分享失败");
                                    } else {
                                        TaskTools.addStageTask(getApplicationContext(), 1005); // 完成阶段任务 1005：分享内容 1 次
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Toast.makeText(getApplicationContext(),
                                        "系统繁忙，本地分享失败!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).start();
    }
}
