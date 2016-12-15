package com.rdcx.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by Administrator on 2016/3/15 0015.
 * <p/>
 * 用来展示 Loading 画面的 View
 */
public class LoadingView extends View {

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean first = true;
    private float width;
    private float height;
    private float r;

    private Paint mPaint;
    private Paint tPaint;
    private RectF rectF;
    private SweepGradient shader;

    private Animation animation;
    private float animationRate;

    /**
     * 启动动画效果
     */
    public void startAnimation() {

        clearAnimation();

        if (animation == null) {
            animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    animationRate = interpolatedTime;
                    invalidate();
                }
            };
            animation.setDuration(800L);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setInterpolator(new LinearInterpolator());
        }
        startAnimation(animation);

    }

    /**
     * 停止动画效果
     */
    public void stopAnimation() {

        clearAnimation();

    }

    private void init() {
        first = false;
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        r = Math.min(width, height);
        if (r > 100) {
            r = Math.max(100, r * 0.1F);
        }
        shader = new SweepGradient(width / 2, height / 2, Color.TRANSPARENT, Color.YELLOW);
        mPaint = new Paint();
        tPaint = new Paint();
        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (first) {
            init();

            mPaint.setColor(Color.TRANSPARENT);

            tPaint.setShader(shader);
            tPaint.setStyle(Paint.Style.STROKE);
            tPaint.setStrokeWidth(r * 0.2F);
        }

        canvas.drawRect((width - r) / 2, (height - r) / 2, (width + r) / 2, (height + r) / 2, mPaint);
        canvas.save();
        canvas.rotate(360 * animationRate, width / 2, height / 2);
        canvas.drawCircle(width / 2, height / 2, r, tPaint);
        canvas.restore();

        super.onDraw(canvas);
    }

}
