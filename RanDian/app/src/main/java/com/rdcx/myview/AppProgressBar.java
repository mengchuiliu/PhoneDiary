package com.rdcx.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.rdcx.animation.SmoothAnimation;

/**
 * TODO: document your custom view class.
 */
public class AppProgressBar extends ProgressBar {

    public AppProgressBar(Context context) {
        super(context);
    }

    public AppProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Paint mPaint = new Paint();
    private RectF r = new RectF();
    private LinearGradient shader;
    private Animation animation;

    private boolean first = true;

    @Override
    protected void onDraw(Canvas canvas) {

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int step = 24, count = width / step;
        for (; step > 0; step -= 3) {
            count = width / step;
            if (count > 30) {
                break;
            }
        }
        count--;
        int left = (width - count * step) / 2, right = (width - count * step) / 2, top = (int) (0.2 * height), bottom = (int) (0.2 * height);
        int percent = getProgress() * count / getMax();
        int secondPercent = Math.max(1, getSecondaryProgress() * count / getMax());
        if (shader == null) {
            shader = new LinearGradient(left, top, width - right, height - bottom, new int[]{0xFF33CCFF, 0xFFCCFF33}, null, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(shader);
        r.set(0, 0, width, height);
        canvas.drawRoundRect(r, (int) (0.1 * height), (int) (0.1 * height), mPaint);

        mPaint.setShader(null);
        mPaint.setColor(0xFFFFFFFF);
        r.set(2, 2, width - 2, height - 2);
        canvas.drawRoundRect(r, (int) (0.1 * height), (int) (0.1 * height), mPaint);

        mPaint.setShader(shader);
        for (int i = 0; i < secondPercent; i++) {
            r.left = left + i * step + step / 3;
            r.top = top;
            r.right = r.left + step / 3;
            r.bottom = height - bottom;
            canvas.drawRect(r, mPaint);
            if (i == percent) {
                mPaint.setShader(null);
                mPaint.setColor(0xFF999999);
            }
        }
    }

    public void setProgressSmooth(int progress) {
        if (first) {
            first = false;
            SmoothAnimation animation = new SmoothAnimation(this, getProgress(), progress);
            animation.setSetSecondaryProgress(true);
            animation.setDuration(800);
            animation.setInterpolator(new DecelerateInterpolator());
            startAnimation(animation);
        } else {
            clearAnimation();
            setProgress(progress);
            setSecondaryProgress(getMax());
        }
    }

}
