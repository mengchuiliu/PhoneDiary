package com.rdcx.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.rdcx.randian.R;
import com.rdcx.tools.PermissionTools;
import com.rdcx.tools.SP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Created by Administrator on 2015/10/29 0029.
 *
 * @author mengchuiliu
 */
public class Utils {

    /*
     * MD5加密
	 */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (Exception e) {
            System.out.println("MD5 加密异常");
        }
        assert messageDigest != null;
        byte[] byteArray = messageDigest.digest();
        StringBuilder md5StrBuff = new StringBuilder();
        for (byte aByteArray : byteArray) {
            if (Integer.toHexString(0xFF & aByteArray).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & aByteArray));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & aByteArray));
            }
        }
        // 16位加密，从第9位到25位
        // return md5StrBuff.substring(8, 24).toString().toUpperCase();
        // 32位大写MD5加密
        return md5StrBuff.toString().toUpperCase();
    }

    /**
     * @param root         最外层布局，需要调整的布局
     * @param scrollToView 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
     */
    public static void controlKeyboardLayout(final View root, final View scrollToView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    boolean flag = true;

                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        // 获取root在窗体的可视区域
                        root.getWindowVisibleDisplayFrame(rect);
                        // 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                        int rootInvisibleHeight = root.getRootView()
                                .getHeight() - rect.bottom;
                        // 若不可视区域高度大于100，则键盘显示
                        if (rootInvisibleHeight > 100) {
                            if (flag) {
                                int[] location = new int[2];
                                // 获取scrollToView在窗体的坐标
                                scrollToView.getLocationInWindow(location);
                                // 计算root滚动高度，使scrollToView在可见区域
                                int srollHeight = (location[1] + scrollToView
                                        .getHeight()) - rect.bottom;
                                root.scrollTo(0, srollHeight);
                                flag = false;
                            }
                        } else {
                            // 键盘隐藏
                            root.scrollTo(0, 0);
                            flag = true;
                        }
                    }
                });
    }

    // 获取唯一且不变的UUID
    public static String getMyUUID(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(),
                ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

    // 过滤字符串中的特殊字符
    public static String StringFilter(String str) throws PatternSyntaxException {
        // 要过滤掉的字符
        String regEx = "[`~!@#$%^&*()+=|{}':;,\\[\\].<>/?！￥…（）—【】‘；：”“’。，、？\"\n\t]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 验证码倒计时线程
     */
    public static void timing(final Handler handler, final int what) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 59; i >= 0; i--) {
                    SystemClock.sleep(1000);
                    Message message = new Message();
                    message.what = what;
                    message.arg1 = i;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 检查当前网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return false;
            } else {
                // 获取NetworkInfo对象
                NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
                if (networkInfo != null && networkInfo.length > 0) {
                    for (NetworkInfo aNetworkInfo : networkInfo) {
                        // 判断当前网络状态是否为连接状态
                        if (aNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("test", "Utils isNetworkAvailable cause an Exception=>:", e);
        }
        return false;
    }

    /**
     * 根据路径加载bitmap
     *
     * @param path 路径
     * @param w    宽
     * @param h    长
     * @return
     */
    public static Bitmap convertToBitmap(String path, int w, int h) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 设置为ture只获取图片大小
            opts.inJustDecodeBounds = true;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // 返回为空
            BitmapFactory.decodeFile(path, opts);
            int width = opts.outWidth;
            int height = opts.outHeight;
            float scaleWidth = 0.f, scaleHeight = 0.f;
            if (width > w || height > h) {
                // 缩放
                scaleWidth = ((float) width) / w;
                scaleHeight = ((float) height) / h;
            }
            opts.inJustDecodeBounds = false;
            float scale = Math.max(scaleWidth, scaleHeight);
            opts.inSampleSize = (int) scale;
            WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
                    BitmapFactory.decodeFile(path, opts));
            Bitmap bMapRotate = Bitmap.createBitmap(weak.get(), 0, 0, weak
                    .get().getWidth(), weak.get().getHeight(), null, true);
            if (bMapRotate != null) {
                return bMapRotate;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 检查sdcard是否存在
     *
     * @return
     */
    public static boolean checkSDCardAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 保存图片到文件
     *
     * @param photoBitmap 需要保存的图片
     * @param path        保存图片所在的文件
     */
    public static void savePhotoToSDCard(Bitmap photoBitmap, String path) {
        if (checkSDCardAvailable()) {
            File photoFile = new File(path);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (compressImage(photoBitmap).compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                        fileOutputStream.flush();
                    }
                }
            } catch (Exception e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    assert fileOutputStream != null;
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 压缩图片100kb
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100 && options >= 0) {
            // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;// 每次都减少10
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     * 设置用户的头像
     *
     * @param activity Activity 对象
     * @param bitmap   用户的头像
     */
    public static void setPortraitBitmap(Activity activity, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("test", "Utils setPortraitBitmap bitmap==null.");
            return;
        }
        if (PermissionTools.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, activity.getString(R.string.permission_external_storage), true)) {
            String userId = SP.getString(activity, SP.USER_ID, "-1");
            File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/cache");
            if (file.exists() || file.mkdirs()) {
                Log.d("test", "Utils setPortraitBitmap file.mkdir()=>:" + true);
                String locpath = Environment.getExternalStorageDirectory() +
                        "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
                Utils.savePhotoToSDCard(bitmap, locpath);
            }
        }
    }

    /**
     * 获取用户的头像
     *
     * @param activity Activity 对象
     * @param handler  Handler 对象
     * @return 头像
     */
    public static Bitmap getPortraitBitmap(Activity activity, Handler handler, boolean needRequest) {
        String iconPath = SP.getString(activity, "photoPath", "");
        // 该用户已经设置了头像 并且 有读取外部存储的权限
        try {
            if (!TextUtils.isEmpty(iconPath) && PermissionTools.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, activity.getString(R.string.permission_external_storage), needRequest)) {
                String userId = SP.getString(activity, SP.USER_ID, "-1");
                String locpath = Environment.getExternalStorageDirectory() +
                        "/ZhangXin/cache/" + "locPortrait" + userId + ".png";
                File file = new File(locpath);
                if (file.exists()) {
                    Bitmap bitmap = getBitmapFromCache(file.getPath());
                    if (bitmap == null) {
                        byte[] data = readStream(new FileInputStream(file));
                        bitmap = Utils.toRoundBitmap(compressBitmap(data, 256, 256));
                        setBitmapToCache(file.getPath(), bitmap);
                    }
                    return bitmap;
                } else {
                    if (needRequest) {
                        Utils.getImage(Constants.head_url + iconPath.replace("\\", "/"), handler, 16);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("test", "Util getPortraitBitmap cause an Exception=>:", e);
        }


        int i = new Random().nextInt(4) + 1;
        switch (i) {
            case 1:
                return ((BitmapDrawable) activity.getResources().getDrawable(R.mipmap.random_1)).getBitmap();
            case 2:
                return ((BitmapDrawable) activity.getResources().getDrawable(R.mipmap.random_2)).getBitmap();
            case 3:
                return ((BitmapDrawable) activity.getResources().getDrawable(R.mipmap.random_3)).getBitmap();
            case 4:
                return ((BitmapDrawable) activity.getResources().getDrawable(R.mipmap.random_4)).getBitmap();
            default:
                return ((BitmapDrawable) activity.getResources().getDrawable(R.mipmap.default_portrait)).getBitmap();
        }
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        Paint paint = new Paint();
        Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right,
                (int) dst_bottom);
        RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);

        // 画一层白色圆圈
        paint.reset();
//        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width / 50);
        canvas.drawCircle(width / 2, width / 2, width / 2 - 1, paint);
        return output;
    }

    // 判断号码是联通或者电信或者移动
    public static String getMobileType(String phoneNumber) {

        String cm = "^((13[4-9])|(147)|(15[0-2,7-9])|(18[2-3,7-8]))\\d{8}$";
        String cu = "^((13[0-2])|(145)|(15[5-6])|(186))\\d{8}$";
        String ct = "^((133)|(153)|(18[0,9]))\\d{8}$";

        if (phoneNumber.matches(cm)) {
            return Constants.YZM_PHONE_YD;
        } else if (phoneNumber.matches(cu)) {
            return Constants.YZM_PHONE_LT;
        } else if (phoneNumber.matches(ct)) {
            return Constants.YZM_PHONE_DX;
        } else {
            return null;
        }
    }

    /**
     * 从短信字符窜提取验证码
     *
     * @param body      短信内容
     * @param YZMLENGTH 验证码的长度 一般6位或者4位
     * @return 接取出来的验证码
     * @about 有些验证码是纯数字的那么直接用这个就可以了 Pattern p
     * =Pattern.compile("(?<![0-9])([0-9]{" + YZMLENGTH+ "})(?![0-9])");
     */
    public static String getYZM(String body, int YZMLENGTH) {
        // 首先([a-zA-Z0-9]{YZMLENGTH})是得到一个连续的六位数字字母组合
        // (?<![a-zA-Z0-9])负向断言([0-9]{YZMLENGTH})前面不能有数字
        // (?![a-zA-Z0-9])断言([0-9]{YZMLENGTH})后面不能有数字出现
        // Pattern p = Pattern.compile("(?<![a-zA-Z0-9])([a-zA-Z0-9]{" +
        // YZMLENGTH
        // + "})(?![a-zA-Z0-9])");
        Pattern p = Pattern.compile("(?<![0-9])([0-9]{" + YZMLENGTH
                + "})(?![0-9])");
        Matcher m = p.matcher(body);
        if (m.find()) {
            return m.group(0);
        }
        return null;
    }

    /**
     * 上传图片到服务端
     *
     * @param handler
     * @param file      上传的文件
     * @param actionUrl 服务器地址
     * @param what
     */
    public static void uploadFile(final Handler handler, final File file, final String actionUrl, final int what) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String end = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                try {
                    URL url = new URL(actionUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    /* 允许Input、Output，不使用Cache */
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setUseCaches(false);
                    /* 设置传送的method=POST */
                    con.setRequestMethod("POST");
                    /* setRequestProperty */
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    if (file != null) {
                        // 当文件不为空，把文件包装并且上传
                        OutputStream outputSteam = con.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(outputSteam);
                        StringBuffer sb = new StringBuffer();
                        sb.append(twoHyphens);
                        sb.append(boundary);
                        sb.append(end);
                        /**
                         * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                         * filename是文件的名字，包含后缀名的 比如:abc.png
                         */

                        sb.append("Content-Disposition: form-data; name=\"img\"; filename=\"" + file.getName() + "\""
                                + end);
                        sb.append("Content-Type: application/octet-stream; charset=" + "UTF-8" + end);
                        sb.append(end);
                        dos.write(sb.toString().getBytes());
                        InputStream is = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                        }
                        is.close();
                        dos.write(end.getBytes());
                        byte[] end_data = (twoHyphens + boundary + twoHyphens + end).getBytes();
                        dos.write(end_data);
                        dos.flush();
                        // 获取响应码 200=成功 当响应成功，获取响应的流
                        int res = con.getResponseCode();
                        if (res == 200) {
                            // 获取响应的输入流对象
                            InputStream is1 = con.getInputStream();
                            // 创建字节输出流对象
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            // 定义读取的长度
                            int len1 = 0;
                            // 定义缓冲区
                            byte buffer[] = new byte[1024];
                            // 按照缓冲区的大小，循环读取
                            while ((len1 = is1.read(buffer)) != -1) {
                                // 根据读取的长度写入到os对象中
                                baos.write(buffer, 0, len1);
                            }
                            // 释放资源
                            is1.close();
                            baos.close();
                            // 返回字符串
                            String result = new String(baos.toByteArray(), "UTF-8");
                            Message message = new Message();
                            message.what = what;
                            message.obj = result.toString();
                            handler.sendMessage(message);
                        } else {
                            Message message = new Message();
                            message.what = what;
                            message.obj = "";
                            handler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = what;
                    message.obj = "";
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 获取网络图片
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static void getImage(final String path, final Handler handler, final int what) {
        new Thread() {
            @Override
            public void run() {
                URL url;
                byte[] data = null;
                try {
                    url = new URL(path);
                    HttpURLConnection httpURLconnection = (HttpURLConnection) url.openConnection();
                    httpURLconnection.setRequestMethod("GET");
                    httpURLconnection.setReadTimeout(6 * 1000);
                    InputStream in;
                    if (httpURLconnection.getResponseCode() == 200) {
                        in = httpURLconnection.getInputStream();
                        data = readStream(in);
                        in.close();
                    }
                    Message message = new Message();
                    message.what = what;
                    message.obj = data;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = what;
                    message.obj = null;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    public static byte[] readStream(InputStream in) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        in.close();
        return outputStream.toByteArray();
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * 获取渠道名
     *
     * @param context 此处习惯性的设置为activity，实际上context就可以
     * @return 如果没有获取成功，那么返回值为空
     */
    public static String getChannelName(Context context) {
        if (context == null) {
            return null;
        }
        String channelName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,
                // 因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.
                        getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = String.valueOf(applicationInfo.metaData.get("UMENG_CHANNEL"));
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return channelName;
    }

    //微信分享标识
    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }

    //微信分享的图片
    public static byte[] bmpByte(Bitmap bmp, boolean needRecycle) {
        int i;
        int j;
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                // F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }

    public static Bitmap getBitmap(String path) {
        return getBitmap(path, 128, 128);
    }

    public static Bitmap getServiceBitmap(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                byte[] data = readStream(inputStream);
                return compressBitmap(data, 480, 800);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static Bitmap getBitmap(String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = getBitmapFromCache(path);
        if (bitmap != null) {
            Log.d("test", "Utils getBitmap from Cache. path=>:" + path);
            return bitmap;
        }
        Log.d("test", "Utils getBitmap from Internet. path=>:" + path);
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                byte[] data = readStream(inputStream);

                bitmap = compressBitmap(data, reqWidth, reqHeight);
                setBitmapToCache(path, bitmap);
                return bitmap;
            }
        } catch (Exception e) {
            Log.e("test", "Utils getBitmap cause an Exception=>:", e);
        }
        return getBitmapFromCache("randian");
    }

    public static Bitmap compressBitmap(byte[] data, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    private static HashMap<String, Bitmap> bitmapCache = null;

    private static Bitmap getBitmapFromCache(String path) {
        if (bitmapCache == null) {
            bitmapCache = new HashMap<>();
        }
        return bitmapCache.get(path);
    }

    public static void setBitmapToCache(String path, Bitmap bitmap) {
        if (bitmapCache == null) {
            bitmapCache = new HashMap<>();
        }
        if (bitmap == null) {
            bitmapCache.remove(path);
        } else {
            bitmapCache.put(path, bitmap);
        }
    }

}
