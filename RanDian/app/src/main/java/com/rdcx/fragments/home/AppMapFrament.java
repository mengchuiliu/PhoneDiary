package com.rdcx.fragments.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.MarkerOptions;
import com.rdcx.fragments.HomeFragment;
import com.rdcx.fragments.MyHomeFragment;
import com.rdcx.loction.Photo;
import com.rdcx.myview.ChatView;
import com.rdcx.myview.LoadingView;
import com.rdcx.randian.R;
import com.rdcx.tools.DB;
import com.rdcx.tools.ImageInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20 0020.
 * <p/>
 * 照片地图
 */
public class AppMapFrament extends HomeFragment {

    private MapView mapView;

    private AMap aMap;

    private CameraUpdate defaultCamera;

    private List<Photo> photoList;

    private LatLngBounds latLngBounds;

    private Photo.Group[] groups;

    @Override
    protected View initView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        view = inflater.inflate(R.layout.home_2, null);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        defaultCamera = CameraUpdateFactory.newCameraPosition(aMap.getCameraPosition());
//        if (groups != null) {
//            showChampion();
//        }
        return view;
    }

    @Override
    protected void refreshDataInThread(Context context, Handler handler) {

        try {
            DB.syncImage(context, System.currentTimeMillis()); // 同步照片
        } catch (Exception e) {
            ruleText = "您没有查看手机存储空间的权限";
            return;
        }

        List<ImageInfo> imageInfoList = DB.selectImageInfo(context, dbTime.get("start"), dbTime.get("end"));
        photoList = new ArrayList<>();
        if (imageInfoList.size() > 0) {
            for (ImageInfo imageInfo : imageInfoList) {
                if (new File(imageInfo.path).exists()) {
                    photoList.add(new Photo(imageInfo.latitude, imageInfo.longitude, imageInfo.path, new Date
                            (imageInfo.time)));
                }
            }
        }
        ruleText = DB.selectRuleText(context, ruleType, dimensionType, photoList.size(), "[nick]为小主记录了[count]张图片。");
        init(context, photoList);

    }

    @Override
    public void startLoading() {
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }

        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.startAnimation();
    }

    public void stopLoading() {
        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.GONE);
        loadingView.stopAnimation();

        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void invalidate(MyHomeFragment myHomeFragment) {

        stopLoading();

        myHomeFragment.showText(ruleText, appDateList);

        aMap.clear();
        aMap.moveCamera(photoList.size() > 0 ? CameraUpdateFactory.newLatLngBounds(latLngBounds, 0) : defaultCamera);

        if (groups != null) {
            for (int gi = 0; gi < groups.length; gi++) {
                Photo.Group g = groups[gi];
                // 添加图片
                aMap.addMarker(new MarkerOptions()
                        // 图片正中心为指定地理位置
                        .anchor(0.5f, 0.5f)
                        // 分组的重心位置
                        .position(g.GetLatLng()).icons(g.getBmplist())
                        // 动画速度，1-20单位不固定
                        .period(10)
                        // 确保低纬度盖住高纬度
                        .zIndex(gi));
            }
        }

    }

//    @Override
//    protected void showChampionStr(TextView champion, TextView oneself, String championStr) {
//        com.alibaba.fastjson.JSONObject championJson = com.alibaba.fastjson.JSONObject.parseObject(championStr);
//        champion.setText(String.format("昨日全国第一ID：%s\n拍了 %s 张照片", championJson.getString("champion"), championJson.getString("championCount")));
//        oneself.setText(String.format("昨日全国排名：%s\n拍了 %s 张照片", championJson.getString("count"), championJson.getString("number")));
//    }

    Bitmap testbmp;
    private static final int textSize = 20;
    private static final int textPadding = 0;

    //地图照片位置显示
    private void init(Context context, List<Photo> photos) {
        // 照片按照地理位置聚类分组
        groups = Photo.Group.Cluster(photos);

        // 设置地图显示的矩形区域
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        // int padding = Math.max(500, Math.max(w / 2, h / 2));
        // Log.i("padding", Integer.toString(padding));
        // 由于API中padding设置无效，下面手动计算四边，然后放大一些保证靠边上的图片不会只看到半张图
        double latmin = Double.MAX_VALUE, latmax = Double.MIN_VALUE, lngmin = Double.MAX_VALUE, lngmax = Double
                .MIN_VALUE;
        for (Photo.Group g : groups) {
            LatLng ll = g.GetLatLng();
            if (ll.latitude < latmin)
                latmin = ll.latitude;
            if (ll.latitude > latmax)
                latmax = ll.latitude;
            if (ll.longitude < lngmin)
                lngmin = ll.longitude;
            if (ll.longitude > lngmax)
                lngmax = ll.longitude;
        }

        // 放大系数设置
        double factor = 0.20, latd = latmax - latmin, lngd = lngmax - lngmin;
        latmin -= latd * factor;
        latmax += latd * factor;
        lngmin -= lngd * factor;
        lngmax += lngd * factor;

        // 地图保证四角可见即可
        builder.include(new LatLng(latmin, lngmin));
        builder.include(new LatLng(latmax, lngmin));
        builder.include(new LatLng(latmin, lngmax));
        builder.include(new LatLng(latmax, lngmax));
        latLngBounds = builder.build();

        Bitmap borderBmp1 = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.photo_frame1);
        Bitmap borderBmp2 = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.photo_frame2);
        // TESTCODE
        testbmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.test);
        /* TESTCODEEND */

        // 换算缩放
        // 边框原图大小210x210
        float bw = borderBmp1.getWidth() / 210.0f;
        float bh = borderBmp1.getHeight() / 210.0f;
        float textRightGap = 35 * bw;
        // 照片将贴在边框原图的目标区域30,30,180,180
        RectF taregtRect = new RectF(30 * bw, 30 * bh, 180 * bw, 180 * bh);

        for (int gi = 0; gi < groups.length; gi++) {
            Photo.Group g = groups[gi];
            ArrayList<BitmapDescriptor> bmplist = new ArrayList<>();
            // 加载前三张图片做轮播
            for (int i = 0; i < g.photos.length && i < 3; i++) {
                String countText = g.photos.length > 999 ? "999+" : Integer
                        .toString(g.photos.length);
                Bitmap b = CreatePhotoBitmap(g.photos.length > 1 ? borderBmp2
                                : borderBmp1, taregtRect, g.photos[i].path, countText,
                        textRightGap);
                bmplist.add(BitmapDescriptorFactory.fromBitmap(b));
            }
            g.setBmplist(bmplist);
        }
    }

    public Bitmap CreatePhotoBitmap(Bitmap borderBmp, RectF taregtRect,
                                    String filePath, String countText, float textRightGap) {
        // 由于后面要进行修改，这里拷贝一下
        taregtRect = new RectF(taregtRect.left, taregtRect.top,
                taregtRect.right, taregtRect.bottom);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textH = fm.bottom - fm.top;
        float rectH = textH + textPadding * 2;
        // 上部增加高度
        int dh = 0;
        float rectY = taregtRect.top - rectH / 2;
        if (rectY < 0) {
            dh = (int) Math.ceil(-rectY);
            rectY = 0;
            taregtRect.offset(0, dh);
        }
        int h = borderBmp.getHeight() + dh;
        int w = borderBmp.getWidth();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        // 边框最下面先画
        c.drawBitmap(borderBmp, null, new Rect(0, dh, w, h), null);

        Bitmap p;
        // TESTCODE 使用测试图片
        p = testbmp;
        /*TESTCODEEND */

        float tw = taregtRect.width(), th = taregtRect.height();
        // TODO: 载入磁盘图片
        // 读入图片宽高
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        p = BitmapFactory.decodeFile(filePath, op);
        // 粗略计算缩放比例
        int xScale = (int) (op.outWidth / tw);
        int yScale = (int) (op.outHeight / th);
        // 设置采样缩放比例省内存，取值偏小保证取出来的图相比目标一样或者大一点
        op.inSampleSize = Math.min(xScale, yScale);
        op.inJustDecodeBounds = false; // 载入粗略缩放图片，
        p = BitmapFactory.decodeFile(filePath, op);

        int pw = p.getWidth(), ph = p.getHeight();
        Rect srcRect;
        // 宽高比过宽，截断左右两边
        float whrate = tw / th;
        if (pw > whrate * ph) {
            int wd = (int) Math.round((pw - (float) ph * whrate) / 2);
            srcRect = new Rect(wd, 0, pw - wd, ph);
        }// 宽高比过高，截断上下两边
        else {
            int hd = (int) Math.round((ph - (float) pw / whrate) / 2);
            srcRect = new Rect(0, hd, pw, ph - hd);
        }
        c.drawBitmap(p, srcRect, taregtRect, null);

        float textWidth = textPaint.measureText(countText);
        RectF textRect = new RectF(w - textWidth - rectH, rectY, w, rectY
                + rectH);
        textRect.offset(-textRightGap, 0);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(255, 23, 129, 251);
        c.drawRoundRect(textRect, rectH / 2, rectH / 2, paint);

        textPaint.setColor(Color.WHITE);
        c.drawText(countText, textRect.left + rectH / 2, textRect.bottom
                - textPadding - fm.bottom, textPaint);
        return bmp;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
            newInit = true;
        }
    }


}
