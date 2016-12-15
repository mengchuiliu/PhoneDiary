package com.rdcx.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.rdcx.tools.DB;
import com.rdcx.tools.Diary;
import com.rdcx.tools.SP;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Administrator on 2016/3/29 0029.
 * 日记本图片下载类
 *
 * @author mengchuiliu
 */
public class DiaryIconUpload extends AsyncTask<Void, String, Boolean> {
    private Diary diary;
    private Context context;
    private Handler handler;
    private int position;

    public DiaryIconUpload(Diary diary, Context context, Handler handler, int position) {
        this.diary = diary;
        this.context = context;
        this.handler = handler;
        this.position = position;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        File cacheFile = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/DiaryCacheImage");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean allUp = true;
        try {
            JSONArray array = new JSONArray(diary.path);
            Bitmap bitmap;
            String mypath = Environment.getExternalStorageDirectory() + "/ZhangXin/DiaryCacheImage/" + "diaryImage" + System.currentTimeMillis() + ".jpg";
            bitmap = Utils.getServiceBitmap(Constants.head_url + (array.getJSONObject(position).getString("servicePath")).replace("\\", "/"));
            if (bitmap != null) {
                Utils.savePhotoToSDCard(bitmap, mypath);
                publishProgress(mypath, "" + position);
            } else {
                allUp = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allUp;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String path = values[0];
        int position = Integer.valueOf(values[1]);
        try {
            JSONArray array = new JSONArray(diary.path);
            JSONObject jsonObject = array.getJSONObject(position);
            jsonObject.put("locaPath", path);
            diary.path = array.toString();
            DB.insertOrUpdateDiary(context, diary);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        if (b) {
            handler.sendEmptyMessage(2);
        }
    }
}
