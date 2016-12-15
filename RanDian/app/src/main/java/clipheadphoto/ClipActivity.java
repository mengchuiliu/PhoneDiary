package clipheadphoto;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.randian.MyApplication;
import com.rdcx.randian.R;
import com.rdcx.service.DataInterface;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ClipActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressDialog loadingDialog;
    private String path;//图片路径
    private ClipImageLayout mClipImageLayout;
    String mypath, locpath;
    Bitmap mybitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);
        ((MyApplication) getApplication()).addActivity(this);
        // 这步必须要加
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("请稍后...");
        path = getIntent().getStringExtra("path");
        initView();
    }

    private void initView() {
        findViewById(R.id.clip_back).setOnClickListener(this);
        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            Toast.makeText(this, "11111图片加载失败!", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = Utils.convertToBitmap(path, 600, 600);
        if (bitmap == null) {
            Toast.makeText(this, "2222图片加载失败!", Toast.LENGTH_SHORT).show();
            return;
        }
        mClipImageLayout.setBitmap(rotateBitmapByDegree(bitmap, getBitmapDegree(path)));
        findViewById(R.id.action_clip).setOnClickListener(this);
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError ignored) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
//            bm.recycle();
        }
        return returnBm;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clip_back:
                finish();
                break;
            case R.id.action_clip:
                loadingDialog.show();
                mybitmap = mClipImageLayout.clip();
                File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/cache");
                if (!file.exists()) {
                    file.mkdirs();
                }
                mypath = Environment.getExternalStorageDirectory() + "/ZhangXin/cache/" + "serPortrait.png";
                Utils.savePhotoToSDCard(mybitmap, mypath);
                // 上传头像到服务器
                File f = new File(mypath);
                Utils.uploadFile(handler, f, Constants.PHOTO_UPLOAD, 0);
                break;
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
                        if (Utils.isNetworkAvailable(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), "网络异常!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("path", "");
                        setResult(RESULT_OK, intent);
                        finish();
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(msg.obj.toString());
                        JSONObject object = jsonArray.getJSONObject(0);
                        final String resp = object.getString("resp");
                        final String photoPath = object.getString("filePath");
                        if (resp.equals("000000")) {
                            DataInterface dataInterface = new NetDataGetter(getApplicationContext());
                            final String userId = SP.getString(ClipActivity.this, SP.USER_ID, "-1");
                            dataInterface.uploadIcon(userId, photoPath,
                                    new NetManager.DataArray() {
                                        @Override
                                        public void getServiceData(JSONArray jsonArray) {
                                            try {
                                                JSONObject object = jsonArray.getJSONObject(0);
                                                String resp = object.getString("resp");
                                                if (resp.equals("000000")) {

                                                    TaskTools.addStageTask(ClipActivity.this, 1001); // 完成阶段任务 1001：更新头像一次

                                                    locpath = Environment.getExternalStorageDirectory() +
                                                            "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
                                                    Utils.setBitmapToCache(locpath, null); // 清除头像缓存

                                                    Utils.savePhotoToSDCard(mybitmap, locpath);
                                                    SP.set(ClipActivity.this, "photoPath", photoPath);
                                                    Toast.makeText(getApplicationContext(), "图片上传成功！",
                                                            Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent();
                                                    intent.putExtra("path", mypath);
                                                    setResult(RESULT_OK, intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(getApplicationContext(),
                                                            "图片上传失败！", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent();
                                                    intent.putExtra("path", "");
                                                    setResult(RESULT_OK, intent);
                                                    finish();
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
                                                        "图片上传失败！", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                                            }
                                            Intent intent = new Intent();
                                            intent.putExtra("path", "");
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
}
