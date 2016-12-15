package com.rdcx.randian;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;
import com.rdcx.webproxy.WebviewMode;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.lang.ref.WeakReference;

import io.dcloud.EntryProxy;
import io.dcloud.common.DHInterface.ISysEventListener;
import io.dcloud.common.DHInterface.ISysEventListener.SysEventType;
import io.dcloud.feature.internal.sdk.SDK;

public class WebHtmlActivity extends AppCompatActivity implements View.OnClickListener {
    EntryProxy mEntryProxy = null;
    WebviewMode wm;
    FrameLayout web_html;
    ImageButton webBack;
    TextView webMenu;
    Button butHome;

    private ProgressDialog pd;

    boolean search = false, message_box = false, discover = false,
            program = false, question = false;
    String desUrl;

    /**
     * 事件处理器，全部转发到 Activity 的 handleMessage 方法中去处理
     */
    static class WebHandler extends Handler {

        WeakReference<WebHtmlActivity> reference;

        public WebHandler(WebHtmlActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (reference != null && reference.get() != null) {
                reference.get().handleMessage(msg);
            }
        }
    }

    /**
     * WebHandler
     */
    WebHandler handler = new WebHandler(this);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_html);
        ((MyApplication) getApplication()).addActivity(this);

        Intent intent = getIntent();
        search = intent.getBooleanExtra("search", false);//搜索
        discover = intent.getBooleanExtra("discover", false);//发现
        message_box = intent.getBooleanExtra("msg_box", false);//消息盒子
        program = intent.getBooleanExtra("program", false);//进程保护
        question = intent.getBooleanExtra("question", false);//常见问题
        desUrl = intent.getStringExtra("url");

        webBack = (ImageButton) findViewById(R.id.webBack);
        webBack.setOnClickListener(this);
        webMenu = (TextView) findViewById(R.id.webMenu);
        webMenu.setOnClickListener(this);
        butHome = (Button) findViewById(R.id.btn_home);
        butHome.setOnClickListener(this);

        web_html = (FrameLayout) findViewById(R.id.web_html);

        // 防止 EntryProxy 没有被 destroy 而报错。
        EntryProxy instanceEntryProxy = EntryProxy.getInstnace();
        if (instanceEntryProxy != null) {
            instanceEntryProxy.destroy();
        }
        // ------------------------------------
        if (mEntryProxy == null) {
            wm = new WebviewMode(this, web_html, getUrl());
            mEntryProxy = EntryProxy.init(this, wm);
            mEntryProxy.onCreate(savedInstanceState, SDK.IntegratedMode.WEBVIEW, null);
        }

    }

    public void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                String path = Environment.getExternalStorageDirectory() + "/ZhangXin/cache/scanCapture.png";
                File f = new File(path);
                if (f.exists()) {
                    if (pd != null) {
                        pd.dismiss();
                        pd = null;
                    }
                    startActivity(new Intent(WebHtmlActivity.this, ReleaseActivity.class));
                } else {
                    Toast.makeText(WebHtmlActivity.this, "分享失败!", Toast.LENGTH_SHORT).show();
                    Log.e("my_log", "截图失败!");
                }
                break;
            case 2:
                if (msg.obj != null) {
                    webFunction(msg.obj.toString());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.webBack:
                if (mEntryProxy != null) {
                    mEntryProxy.onStop(this);
                    mEntryProxy.destroy();
                }
                finish();
                break;
            case R.id.webMenu:
                webMenuClick((TextView) v);
                break;
            case R.id.btn_home:
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
                break;
            default:
                break;
        }
    }

    // 处理右上角菜单事件
    private void webMenuClick(TextView v) {
        String text = v.getText().toString();
        Object tag = v.getTag();
        if (tag != null) {
            webFunction(tag.toString());
            return;
        }
        if ("分享".equals(text)) {
            webMenuShare();
        }
    }

    // 调用页面内的方法
    private void webFunction(String funcName) {
        try {
            wm.getWeb().loadUrl("javascript:" + funcName + "();");
        } catch (Exception e) {
            Log.d("test", "WebHtmlActivity webFunction 方法 funcName=>:" + funcName + " 调用时失败。");
        }
    }

    // 分享事件
    private void webMenuShare() {
        pd = ProgressDialog.show(this, "", "截图中 ...", true);
        final Bitmap bitmap = captureWebView(wm.getWeb());
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/cache");
                if (!file.exists()) {
                    file.mkdirs();
                }
                String path = Environment.getExternalStorageDirectory() + "/ZhangXin/cache/scanCapture.png";
                if (bitmap != null) {
                    Utils.savePhotoToSDCard(bitmap, path);
                    handler.sendEmptyMessage(1);
                } else {
                    Log.e("my_log", "截图失败!");
                }
            }
        }).start();
    }

    /**
     * 截取webView快照(webView加载的整个内容的大小)
     *
     * @param webView
     * @return
     */
    private Bitmap captureWebView(WebView webView) {
        Picture snapShot = webView.capturePicture();
        Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(),
                snapShot.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        snapShot.draw(canvas);
        return bmp;
    }

    //获取html网址
    private String getUrl() {
        String url = "";
        if (search) {
            url = Constants.web_url + "search.html";
        } else if (discover) {
            url = Constants.web_url + "discovery.html";
        } else if (message_box) {
            url = Constants.web_url + "msgBox.html";
        } else if (program) {
            url = Constants.web_url + "protect.html?name=" + Build.BRAND;
        } else if (question) {
            url = Constants.web_url + "label.html";
        } else if (desUrl != null) {
            url = Constants.web_url + desUrl;
        }
        return url;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        mEntryProxy.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        mEntryProxy.onPause(this);
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getFlags() != 0x10600000) {// 非点击icon调用activity时才调用newintent事件
            mEntryProxy.onNewIntent(this, intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEntryProxy.onStop(this);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean _ret = mEntryProxy.onActivityExecute(this,
                ISysEventListener.SysEventType.onKeyDown, new Object[]{keyCode, event});
        if (!_ret) {
            mEntryProxy.destroy();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean _ret = mEntryProxy.onActivityExecute(this,
                SysEventType.onKeyUp, new Object[]{keyCode, event});
        return _ret || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        boolean _ret = mEntryProxy.onActivityExecute(this,
                SysEventType.onKeyLongPress, new Object[]{keyCode, event});
        return _ret || super.onKeyLongPress(keyCode, event);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        try {
            int temp = this.getResources().getConfiguration().orientation;
            if (mEntryProxy != null) {
                mEntryProxy.onConfigurationChanged(this, temp);
            }
            super.onConfigurationChanged(newConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mEntryProxy.onActivityExecute(this, ISysEventListener.SysEventType.onActivityResult,
                new Object[]{requestCode, resultCode, data});
    }
}
