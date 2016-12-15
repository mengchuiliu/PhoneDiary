package com.rdcx.webproxy;

import android.app.Activity;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rdcx.animation.SmoothAnimation;
import com.rdcx.randian.R;

import io.dcloud.common.DHInterface.ICore;
import io.dcloud.common.DHInterface.ICore.ICoreStatusListener;
import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.DHInterface.IWebviewStateListener;
import io.dcloud.feature.internal.sdk.SDK;

public class WebviewMode implements ICoreStatusListener {

    Activity activity = null;
    ViewGroup mRootView = null;
    String url = null;
    ProgressBar webLoading = null;
    TextView webMenu = null;
    TextView webTitle = null;
    public boolean isshow = false;

    public WebviewMode(Activity activity, ViewGroup rootView, String url) {
        this.activity = activity;
        this.mRootView = rootView;
        this.url = url;
        webLoading = (ProgressBar) activity.findViewById(R.id.webLoading);
        webTitle = (TextView) activity.findViewById(R.id.webTitle);
        webMenu = (TextView) activity.findViewById(R.id.webMenu);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        webview.onRootViewGlobalLayout(mRootView);
                    }
                });
    }

    @Override
    public void onCoreInitEnd(ICore coreHandler) {
        // SDK.initSDK(coreHandler);
        // 注册扩展的Feature
        // 1，featureName 为特征名称
        // 2, className 为处理扩展Feature的接收类全名称
        // 3, content 为扩展Feature而创建的js代码，代码中必须使用
        // plus.bridge.execSync(featureName,actionName,[arguments])或plus.bridge.exec(featureName,actionName,
        // [arguments])与native层进行数据交互
        SDK.registerJsApi(featureName, className, content);
        // 创建默认webapp，赋值appid
        // String url = "file:///android_asset/apps/H5Plugin/www/index.html";
        // String url = "http://192.168.0.248/home.html";
        showWebview("Porsche", url);
    }

    IWebview webview = null;

    private void showWebview(String appid, final String url) {

        webview = SDK.createWebview(activity, url, appid,
                new IWebviewStateListener() {
                    @Override
                    public Object onCallBack(int pType, Object pArgs) {
                        switch (pType) {
                            case IWebviewStateListener.ON_WEBVIEW_READY:
                                // 准备完毕之后添加webview到显示父View中，设置排版不显示状态，避免显示webview时，html内容排版错乱问题
                                ((IWebview) pArgs).obtainFrameView().obtainMainView().setVisibility(View.INVISIBLE);
                                SDK.attach(mRootView, ((IWebview) pArgs));
                                break;
                            case IWebviewStateListener.ON_LOAD_RESOURCE:
                            case IWebviewStateListener.ON_RECEIVED_TITLE:
                                webTitle.setText(webview.obtainWebview().getTitle());
                                break;
                            case IWebviewStateListener.ON_PAGE_STARTED:
                                webLoading.setProgress(0);
                                webLoading.setVisibility(View.VISIBLE);
                                webMenu.setVisibility(View.GONE);
                                webMenu.setText("");
                                break;
                            case IWebviewStateListener.ON_PROGRESS_CHANGED:
                                setSmoothProgress(Math.max((int) pArgs, webLoading.getProgress()));
                                break;
                            case IWebviewStateListener.ON_PAGE_FINISHED:
                                setProgressLoaded();
                                // 页面加载完毕，设置显示webview
                                webview.obtainFrameView().obtainMainView().setVisibility(View.VISIBLE);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        isshow = true;
                                    }
                                }, 1000);
                                break;
                        }
                        return null;
                    }
                });
        final WebView webviewInstance = webview.obtainWebview();
        webviewInstance.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 监听返回键
        webviewInstance.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && webviewInstance.canGoBack()) {
                        webviewInstance.goBack();
                        return true;
                    }
                }
                return false;
            }
        });

    }

    public WebView getWeb() {
        return webview.obtainWebview();
    }

    @Override
    public void onCoreReady(ICore coreHandler) {
        try {
            SDK.initSDK(coreHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String featureName = "T";
    String className = "com.rdcx.webproxy.WebViewMode_FeatureImpl";
    String content = "(function(plus){function test(){return plus.bridge.execSync('T','test',[arguments]);}plus.T = " +
            "{test:test};})(window.plus);";

    @Override
    public boolean onCoreStop() {
        return false;
    }

    private SmoothAnimation animation = null;

    private void setSmoothProgress(int progress) {
        webLoading.clearAnimation();
        if (animation == null) {
            animation = new SmoothAnimation(webLoading, webLoading.getProgress(), progress);
            animation.setInterpolator(new DecelerateInterpolator());
        }
        animation.setDuration(2000);
        animation.setProgress(webLoading.getProgress(), progress);
        webLoading.startAnimation(animation);
    }

    private void setProgressLoaded() {
        webLoading.clearAnimation();
        if (animation == null) {
            animation = new SmoothAnimation(webLoading, webLoading.getProgress(), webLoading.getMax());
            animation.setInterpolator(new DecelerateInterpolator());
        }
        animation.setDuration(600);
        animation.setProgress(webLoading.getProgress(), webLoading.getMax());
        webLoading.startAnimation(animation);
        webLoading.setVisibility(View.GONE);
    }
}