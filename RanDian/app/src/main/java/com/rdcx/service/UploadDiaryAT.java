package com.rdcx.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.rdcx.tools.DB;
import com.rdcx.tools.Diary;
import com.rdcx.tools.Upload;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Administrator on 2016/3/25 0025.
 *
 * @author mengchuiliu
 */
public class UploadDiaryAT extends AsyncTask<Void, String, Boolean> {
    private Context context;
    private Diary diary;
    private List<Integer> list;

    public UploadDiaryAT(Context context, Diary diary, List<Integer> list) {
        this.context = context;
        this.diary = diary;
        this.list = list;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        for (int i = 0; i < list.size(); i++) {
            try {
                JSONObject jsonObject = new JSONArray(diary.path).getJSONObject(list.get(i));
                publishProgress(upLoadFile(jsonObject.getString("locaPath")), "" + list.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String str = values[0];
        int position = Integer.valueOf(values[1]);
        if (str != null && !str.equals("")) {
            try {
                JSONArray jsonArray = new JSONArray(str);
                JSONObject object = jsonArray.getJSONObject(0);
                String resp = object.getString("resp");
                if (resp.equals("000000")) {
                    String path = object.getString("filePath");
                    JSONArray array = new JSONArray(diary.path);
                    JSONObject jsonObject = array.getJSONObject(position);
                    jsonObject.put("servicePath", path);
                    jsonObject.put("isUpload", "1");
                    diary.path = array.toString();
                    DB.insertOrUpdateDiary(context, diary);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean str) {
        super.onPostExecute(str);
        if (str) {
            Upload.uploadDiary(context, diary);
            File cacheFile = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/DiaryImage");
            if (cacheFile.exists()) {
                deleteDir(cacheFile);
            }
        }
    }


    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    private String upLoadFile(String str) {
        File cacheFile = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/DiaryImage");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        String mypath = Environment.getExternalStorageDirectory() + "/ZhangXin/DiaryImage/" + "diaryImage.png";
        Bitmap bitmap = BitmapFactory.decodeFile(str);
        File file = new File(mypath);
        compressBmpToFile(bitmap, file);
        Log.e("my_log", "------文件大小------->" + file.length() / 1024);
        String result = null;
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(Constants.PHOTO_UPLOAD);
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
            con.setChunkedStreamingMode(10240);
            con.setConnectTimeout(10 * 1000);
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
                sb.append("Content-Disposition: form-data; name=\"img\"; filename=\"").append(file.getName()).append("\"").append(end);
                sb.append("Content-Type: application/octet-stream; charset=" + "UTF-8").append(end);
                sb.append(end);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024 * 50];
                int len;
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
                Log.e("liu_test", "---diaryIconUp--->:" + res);
                if (res == 200) {
                    // 获取响应的输入流对象
                    InputStream is1 = con.getInputStream();
                    // 创建字节输出流对象
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    // 定义读取的长度
                    int len1;
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
                    result = new String(baos.toByteArray(), "UTF-8");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    public static void compressBmpToFile(Bitmap bmp, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > 100 && options >= 0) {
            baos.reset();
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            file.delete();
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
