package com.rdcx.myview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rdcx.randian.R;
import com.rdcx.randian.WebHtmlActivity;
import com.rdcx.tools.SP;
import com.rdcx.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/5/10 0010.
 * <p/>
 * 任务清单的 Banner
 */
public class TaskBannerView extends ImageView {

    private List<TextEmbeb> textEmbebList;

    private boolean init = false;

    private int width;
    private int height;
    private Paint tPaint;
    private String url;

    public TaskBannerView(Context context) {
        super(context);
    }

    public TaskBannerView(Context context, int resource, List<TextEmbeb> textEmbebList) {
        this(context);
        setImageResource(resource);
        this.textEmbebList = textEmbebList;
    }

    public TaskBannerView(Context context, int resource, List<TextEmbeb> textEmbebList, String url) {
        this(context, resource, textEmbebList);
        this.url = url;
    }

    private void init() {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        tPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!init) {
            init = true;
            init();
        }

        if (textEmbebList != null) {
            for (TextEmbeb textEmbeb : textEmbebList) {
                tPaint.setColor(textEmbeb.color);
                tPaint.setTextSize(width * textEmbeb.size);
                tPaint.setTextAlign(textEmbeb.align);
                canvas.drawText(textEmbeb.text, width * textEmbeb.x, height * textEmbeb.y, tPaint);
            }
        }
    }

    public static class TextEmbeb {
        public String text;
        public float x;
        public float y;
        public float size;
        public int color;
        public Paint.Align align;

        public TextEmbeb() {
        }

        public TextEmbeb(String text, float x, float y, float size, int color, Paint.Align align) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.align = align;
        }

        @Override
        public String toString() {
            return "TextEmbeb{" +
                    "text='" + text + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", size=" + size +
                    ", color=" + color +
                    ", align=" + align +
                    '}';
        }
    }

    public static List<TextEmbeb> getBanner(int resource, String[] texts) {
        List<TextEmbeb> textEmbebList = new ArrayList<>();
        try {
            switch (resource) {
                case R.mipmap.banner1:
                    textEmbebList.add(new TextEmbeb(texts[0], 0.46F, 0.33F, 0.05F, 0xFF000000, Paint.Align.CENTER));
                    textEmbebList.add(new TextEmbeb(texts[1], 0.47F, 0.57F, 0.05F, 0xFFFFFFFF, Paint.Align.CENTER));
                    break;
                case R.mipmap.banner2:
                    textEmbebList.add(new TextEmbeb(texts[0], 0.55F, 0.37F, 0.06F, 0xFF000000, Paint.Align.CENTER));
                    textEmbebList.add(new TextEmbeb(texts[1], 0.55F, 0.55F, 0.06F, 0xFF000000, Paint.Align.CENTER));
                    break;
                case R.mipmap.banner3:
                    textEmbebList.add(new TextEmbeb(texts[0], 0.59F, 0.57F, 0.06F, 0xFF000000, Paint.Align.CENTER));
                    textEmbebList.add(new TextEmbeb(texts[1], 0.59F, 0.816F, 0.06F, 0xFF000000, Paint.Align.CENTER));
                    break;
                case R.mipmap.banner4:
                    textEmbebList.add(new TextEmbeb(texts[0], 0.62F, 0.32F, 0.04F, 0xFFFEDB38, Paint.Align.CENTER));
                    textEmbebList.add(new TextEmbeb(texts[1], 0.51F, 0.49F, 0.05F, 0xFFFEDB38, Paint.Align.CENTER));
                    textEmbebList.add(new TextEmbeb(texts[2], 0.59F, 0.67F, 0.06F, 0xFFFEDB38, Paint.Align.CENTER));
                    break;
                case R.mipmap.banner5:
                    textEmbebList.add(new TextEmbeb(texts[0], 0.28F, 0.40F, 0.027F, 0xFF000000, Paint.Align.LEFT));
                    textEmbebList.add(new TextEmbeb(texts[1], 0.28F, 0.49F, 0.027F, 0xFF000000, Paint.Align.LEFT));
                    textEmbebList.add(new TextEmbeb(texts[2], 0.28F, 0.58F, 0.027F, 0xFF000000, Paint.Align.LEFT));
                    textEmbebList.add(new TextEmbeb(texts[3], 0.28F, 0.67F, 0.027F, 0xFF000000, Paint.Align.LEFT));
                    textEmbebList.add(new TextEmbeb(texts[4], 0.28F, 0.76F, 0.027F, 0xFF000000, Paint.Align.LEFT));
                    textEmbebList.add(new TextEmbeb(texts[5], 0.28F, 0.85F, 0.027F, 0xFF000000, Paint.Align.LEFT));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e("test", "TaskBannerView getBanner cause an Exception=>:", e);
        }
        return textEmbebList;
    }

    public static void getTaskBannerViews(final Context context, final Handler handler, final int what) {

        List<TaskBannerView> taskBannerViewList = new ArrayList<>();

        try {

            // 1、计算用户完成任务数
            String taskUserCompleted = SP.getString(context, SP.getUserIdKey(context, SP.TASK_USER_COMPLETED), "");
            int userCompletedCount = 0;
            for (String str : taskUserCompleted.split(",")) {
                if (str.length() > 0) {
                    userCompletedCount++;
                }
            }
            String completed = String.valueOf(userCompletedCount);

            // 2、获取奖项，计算还差多少个任务
            JSONArray envelopModelList = null;
            String findEnvelopByAll = SP.cache(context, Constants.FIND_ENVELOP_BY_ALL, null, System.currentTimeMillis());
            if (findEnvelopByAll != null) {
                JSONObject jo = JSONObject.parseObject(findEnvelopByAll);
                envelopModelList = jo.getJSONArray("modelList");
                for (int i = 0; i < envelopModelList.size(); i++) {
                    int count = envelopModelList.getJSONObject(i).getIntValue("count");
                    if (userCompletedCount < count) {
                        String differ = String.valueOf(count - userCompletedCount);
                        int mipmap = new int[]{R.mipmap.banner1, R.mipmap.banner2}[(int) (Math.random() * 2)];
                        taskBannerViewList.add(new TaskBannerView(context, mipmap, getBanner(mipmap, new String[]{completed, differ})));
                        break;
                    }
                }
            } else {
                return;
            }

            // 3、获取当前用户获奖情况，并加入 Banner 条
            SP.cacheDelete(context, Constants.FIND_REWARD_BY_USER_ID);
            String findRewardByUserId = SP.cache(context, Constants.FIND_REWARD_BY_USER_ID, null, System.currentTimeMillis());
            if (findRewardByUserId != null) {
                JSONObject jo = JSONObject.parseObject(findRewardByUserId);
                JSONArray modelList = jo.getJSONArray("modelList");
                for (int i = 0; i < modelList.size(); i++) {
                    JSONObject model = modelList.getJSONObject(i);
                    int envelopeId = model.getIntValue("envelopeId");
                    int ranking = model.getIntValue("ranking");
                    int status = model.getIntValue("status"); // status 1：未领奖，2：已领奖

                    for (int j = 0; j < envelopModelList.size(); j++) {
                        if (envelopeId == envelopModelList.getJSONObject(j).getInteger("id") && status != 2) {
                            String name = envelopModelList.getJSONObject(j).getString("name");
                            taskBannerViewList.add(new TaskBannerView(context, R.mipmap.banner4, getBanner(R.mipmap.banner4, new String[]{name, completed, String.valueOf(ranking)}), "qrcode.html"));
                            break;
                        }
                    }

                }
            }

            // 4、获取所有用户获取情况，并加入 Banner 条
            SP.cacheDelete(context, Constants.FIND_REWARD_BY_TOP);
            String findRewardByTop = SP.cache(context, Constants.FIND_REWARD_BY_TOP, null, System.currentTimeMillis());
            if (findRewardByTop != null) {
                String[] rewards = new String[]{"", "", "", "", "", ""};
                JSONObject jo = JSONObject.parseObject(findRewardByTop);
                JSONArray modelList = jo.getJSONArray("modelList");
                for (int i = 0; i < modelList.size(); i++) {
                    JSONObject model = modelList.getJSONObject(i);
                    int envelopeId = model.getIntValue("envelopeId");
                    int userId = model.getIntValue("userId");
                    int ranking = model.getIntValue("ranking");

                    for (int j = 0; j < envelopModelList.size(); j++) {
                        if (envelopeId == envelopModelList.getJSONObject(j).getInteger("id")) {
                            String name = envelopModelList.getJSONObject(j).getString("name");
                            rewards[i] = String.format(Locale.getDefault(), "ID %d     排名%d,%s", userId, ranking, name);
                            break;
                        }
                    }
                }

                if (rewards[0].length() > 0) {
                    taskBannerViewList.add(new TaskBannerView(context, R.mipmap.banner5, getBanner(R.mipmap.banner5, rewards)));
                }
            }


        } catch (Exception e) {

            Log.d("test", "TaskBannerView getTaskBannerViews cause an Exception=>:", e);

        } finally {
            Message msg = new Message();
            msg.obj = taskBannerViewList;
            msg.what = what;
            handler.sendMessage(msg);
        }
    }

    public void onClick() {
        if (url != null) {
            Intent intent = new Intent(getContext(), WebHtmlActivity.class);
            intent.putExtra("url", url);
            getContext().startActivity(intent);
        }
    }

}
