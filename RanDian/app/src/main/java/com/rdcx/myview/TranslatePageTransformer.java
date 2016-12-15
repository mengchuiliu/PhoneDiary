package com.rdcx.myview;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;
import android.view.ViewGroup;

import com.rdcx.randian.R;


public class TranslatePageTransformer implements PageTransformer {

    /**
     * 当我们的ViewPager滑动的时候，每一个页面都会回调该方法 position:当前第几个页面 view:某个页面对应的视图 --- 布局的视图
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void transformPage(View view, float position) {
        // 渐变效果，判断区间（-1，1）
        ViewGroup rl = (ViewGroup) view.findViewById(R.id.rl);
        if (position < 1 && position > -1) {
            // 缩放效果
            // 缩放的范围：0-1
            rl.setScaleX(Math.max(0.8f, 1 - Math.abs(position)));
            rl.setScaleY(Math.max(0.8f, 1 - Math.abs(position)));

            // 3D翻转动画 往内翻转
            rl.setPivotX(position < 0f ? rl.getWidth() : 0f);
            rl.setPivotY(rl.getHeight() * 0.5f);
            rl.setRotationY(-position * 90);

        } else {
            rl.setScaleX(1.0f);
            rl.setScaleY(1.0f);
            rl.setPivotX(position < 0f ? rl.getWidth() : 0f);
            rl.setPivotY(rl.getHeight());
            rl.setRotationY(0);
        }
    }
}
