package com.rdcx.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Created by Administrator on 2015/12/10 0010.
 * <p/>
 * 进度条平滑动画
 */
public class SmoothAnimation extends Animation {

    private ProgressBar progressBar;

    private int oldProgress = 0;

    private int targetProgress = 0;

    private boolean setSecondaryProgress = false;

    public SmoothAnimation(ProgressBar progressBar, int oldProgress, int targetProgress) {
        this.progressBar = progressBar;
        this.oldProgress = oldProgress;
        this.targetProgress = targetProgress;
    }

    public SmoothAnimation(ProgressBar progressBar, int targetProgress) {
        this(progressBar, progressBar.getProgress(), targetProgress);
    }

    public void setProgress(int oldProgress, int targetProgress) {
        this.oldProgress = oldProgress;
        this.targetProgress = targetProgress;
    }

    public void setSetSecondaryProgress(boolean setSecondaryProgress) {
        this.setSecondaryProgress = setSecondaryProgress;
    }

    @Override
    protected void applyTransformation(float interpolatdTime, Transformation t) {

        progressBar.setProgress((int) Math.ceil(oldProgress + (targetProgress - oldProgress) * interpolatdTime));

        if (setSecondaryProgress) {
            progressBar.setSecondaryProgress((int) Math.ceil(oldProgress + (progressBar.getMax() - oldProgress) * interpolatdTime));
        }

    }
}
