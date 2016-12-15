package com.rdcx.myview;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by Administrator on 2015/12/14 0014.
 * <p/>
 * 扫描页面动画进度条
 */
public class ScanProgressBar extends ProgressBar {

    public ScanProgressBar(Context context) {
        super(context);
    }

    public ScanProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int times = 0;

    private Paint mPaint = new Paint();
    private RectF r = new RectF();
    private LinearGradient shader;
    private BlurMaskFilter blurMaskFilter;
    private Path path;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 获取宽度与高度
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // 绘制背景0xFF132D3E 0xA033FFFF
        mPaint.setColor(0xFF132D3E);
        canvas.drawRect(0, 0, width, height, mPaint);

        // 通过宽度与高度计算一些需要用到的参数
        int radius = (int) (width * 0.3F);
        int rectWidth = width / 16;
        int rectHeight = rectWidth / 6;
        int count = 72, ignore = 16;
        int step = width / count;
        int stepDegress = 360 / count;
        int progressCount = getProgress() * (count - ignore) / getMax();

        // 写入 % 号
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize((int) (radius * 0.3F));
        mPaint.setColor(0xFF7F7F86);
        canvas.drawText("%", width >> 1, (height >> 1) - radius * 0.4F, mPaint);

        // 写入进度
        mPaint.setTextSize((int) (radius * 0.8F));
        mPaint.setColor(0xFF00ccff);
        canvas.drawText(getProgress() + "", width >> 1, (height >> 1) + radius * 0.4F, mPaint);

        // 绘制进度条
        if (shader == null) {
            shader = new LinearGradient(0, (height >> 1) + radius, width, (height >> 1) + radius + rectWidth, new int[]{0xFF33CCFF, 0xFFCCFF33, 0xFFFF33CC}, null, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(shader);
        float angle = ignore * stepDegress / 2;
        for (int i = 0; i < count - ignore - 1; i++) {
            r.left = i * step;
            r.top = (height >> 1) + radius;
            r.right = r.left + rectHeight;
            r.bottom = r.top + rectWidth;

            if (i == progressCount) {
                mPaint.setShader(null);
                mPaint.setColor(0xFF666666);
            }

            angle += stepDegress;

            canvas.save();
            canvas.translate(((width - rectHeight) >> 1) - r.left, 0);
            canvas.rotate(angle, r.left + (rectHeight >> 1), (height >> 1));
            canvas.drawRoundRect(r, rectHeight / 2, rectHeight / 2, mPaint);
            canvas.restore();
        }
        mPaint.setShader(null);

        // 绘制外圈
        if (blurMaskFilter == null) {
            blurMaskFilter = new BlurMaskFilter(100f, BlurMaskFilter.Blur.SOLID);
        }
        mPaint.setColor(0xFF666666);
        mPaint.setMaskFilter(blurMaskFilter);

        if (path == null) {
            float circleUnit = rectHeight + 1;
            float circleRadius = radius + rectWidth;
            path = new Path();
            r.left = (width >> 1) - circleRadius - 2 * circleUnit;
            r.top = (height >> 1) - circleRadius - 3 * circleUnit;
            r.right = (width >> 1) + circleRadius + 2 * circleUnit;
            r.bottom = (height >> 1) + circleRadius + 3 * circleUnit;
            path.moveTo(r.right, height >> 1);
            path.arcTo(r, 0, 90);

            r.top = r.top + 2 * circleUnit;
            r.bottom = r.bottom - 2 * circleUnit;
            path.lineTo(width >> 1, r.bottom);
            path.arcTo(r, 90, -90);
            path.close();
        }
        canvas.save();
        canvas.rotate(10 * times++, (width >> 1), (height >> 1));
        canvas.drawPath(path, mPaint);
        canvas.restore();
        mPaint.setMaskFilter(null);
    }
}
