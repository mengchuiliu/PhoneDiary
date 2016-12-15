package com.rdcx.randian;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

import java.io.File;

public class MyWeiBoShareActivity extends AppCompatActivity implements Response {

    public static final int WB_Result_ok = 666;
    public static final int WB_Result_no = 444;

    // 微博微博分享接口实例
    private IWeiboShareAPI mWeiboShareAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_wei_bo_share);

        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, MyApplication.APP_KEY);
        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
        // 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
        // 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
        // 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
        // 失败返回 false，不调用上述回调
        if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }

        shareWB(getIntent().getStringExtra("sharePath"), getIntent().getStringExtra("content"));
    }

    private void shareWB(String path, String content) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        TextObject textObject = new TextObject();
        textObject.text = "手机日记•分享  \n" + content;
        weiboMessage.textObject = textObject;

        ImageObject imageObject = new ImageObject();
        Bitmap bitmap;
        if ((new File(path).exists())) {
            bitmap = BitmapFactory.decodeFile(path);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.randian);
        }
        imageObject.setImageObject(bitmap);
        weiboMessage.imageObject = imageObject;
        bitmap.recycle();

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        mWeiboShareAPI.sendRequest(this, request);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                Toast.makeText(getApplicationContext(), "微博分享成功", Toast.LENGTH_SHORT).show();
                setResult(WB_Result_ok);
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                Toast.makeText(getApplicationContext(), "微博分享取消", Toast.LENGTH_SHORT).show();
                setResult(WB_Result_no);
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                Toast.makeText(getApplicationContext(), "微博分享失败", Toast.LENGTH_SHORT).show();
                setResult(WB_Result_no);
                Log.e("my_log", "微博分享失败====>" + "Error Message: " + baseResp.errMsg);
                break;
        }
        this.finish();
    }
}
