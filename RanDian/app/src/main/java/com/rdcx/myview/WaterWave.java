package com.rdcx.myview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/11/12 0012.
 *
 * @author mengchuiliu
 */
public class WaterWave extends LinearLayout {
    /**
     * 波形的List
     */
    private List<Wave> waveList;

    /**
     * 最大的不透明度，完全不透明
     */
    private static final int MAX_ALPHA = 100;

    private boolean isStart = true;

    public WaterWave(Context context, AttributeSet attrs) {
        super(context, attrs);
        waveList = Collections.synchronizedList(new ArrayList<Wave>());
    }

    public WaterWave(Context context) {
        super(context);
        waveList = Collections.synchronizedList(new ArrayList<Wave>());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public WaterWave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        waveList = Collections.synchronizedList(new ArrayList<Wave>());
    }

    /**
     * onMeasure方法，确定控件大小，这里使用默认的
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // 重绘所有圆环
        for (int i = 0; i < waveList.size(); i++) {
            Wave wave = waveList.get(i);
            canvas.drawCircle(wave.xDown, wave.yDown, wave.radius, wave.paint);
        }
    }

    /**
     * 初始化paint
     */
    private Paint initPaint(int alpha, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(width);
        // 设置是环形方式绘制
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(alpha);
        paint.setColor(0xff6699ff);
        return paint;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    flushState();
                    invalidate();
                    if (waveList != null && waveList.size() > 0) {
                        handler.sendEmptyMessageDelayed(0, 50);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Wave wave = new Wave();
                wave.radius = 16;
                wave.alpha = MAX_ALPHA;
                wave.width = wave.radius / 4;
                wave.xDown = (int) event.getX();
                wave.yDown = (int) event.getY();
                wave.paint = initPaint(wave.alpha, wave.width);
                if (waveList.size() == 0) {
                    isStart = true;
                }
                waveList.add(wave);
                // 点击之后刷洗一次图形
                invalidate();
                if (isStart) {
                    handler.sendEmptyMessage(0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 刷新状态
     */
    private void flushState() {
        for (int i = 0; i < waveList.size(); i++) {
            Wave wave = waveList.get(i);
            if (!isStart && wave.alpha == 0) {
                waveList.remove(i);
                wave.paint = null;
                continue;
            } else if (isStart) {
                isStart = false;
            }
            wave.radius += 6;
            wave.alpha -= 10;
            if (wave.alpha < 0) {
                wave.alpha = 0;
            }
            wave.width = wave.radius / 4;
            wave.paint.setAlpha(wave.alpha);
            wave.paint.setStrokeWidth(wave.width);
        }
    }

    private class Wave {
        /**
         * 用来表示圆环的半径
         */
        float radius;
        Paint paint;
        /**
         * 按下的时候x坐标
         */
        int xDown;
        /**
         * 按下的时候y的坐标
         */
        int yDown;
        float width;
        int alpha;
    }
}
