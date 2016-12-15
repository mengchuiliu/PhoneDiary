package com.rdcx.fragments.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rdcx.fragments.HomeFragment;
import com.rdcx.fragments.MyHomeFragment;
import com.rdcx.loction.Location;
import com.rdcx.myview.LoadingView;
import com.rdcx.randian.R;
import com.rdcx.tools.DB;
import com.rdcx.tools.Download;
import com.rdcx.tools.LocationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/12/20 0020.
 * <p/>
 * 足迹
 */
public class LocationFragment extends HomeFragment {

    private ImageView zuji;

    private ArrayList<Location> locationList;
    private Bitmap locationBmp;

    @Override
    protected View initView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        return inflater.inflate(R.layout.home_5, null);
    }

    @Override
    protected void clearCache() {
        zuji = (ImageView) view.findViewById(R.id.zuji);
        zuji.setImageBitmap(null);
        if (locationBmp != null && !locationBmp.isRecycled()) {
            locationBmp.recycle();
        }
    }

    @Override
    protected void refreshDataInThread(Context context, Handler handler) {

        Location.getAll(context);

        Download.downloadLocation(context, dbTime.get("start"), dbTime.get("end"));

        locationList = new ArrayList<>();
        List<HashMap<String, Object>> locationHashMapList = LocationInfo.selectLocation(context, dbTime.get("start"), dbTime.get("end"));
        for (HashMap<String, Object> locationMap : locationHashMapList) {
            this.locationList.add(new Location(Double.parseDouble((String) locationMap.get("longitude")), Double.parseDouble((String) locationMap.get("latitude"))));
        }
        // 背景图和图标
        Bitmap testbmp2 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.home_zuji);
        locationBmp = Location.CreateMap(testbmp2, BitmapFactory.decodeResource(context.getResources(), R.mipmap.location), this.locationList);
        testbmp2.recycle();

        int count = Location.getCitySize(this.locationList);
        if (count > 0) {
            ruleText = DB.selectRuleText(context, ruleType, dimensionType, count, "小主带着[nick]走过[count]个城市。");
        } else {
            ruleText = "咦，数据不准吗？小主是不是没开GPS定位呀。";
        }

    }

    @Override
    public void startLoading() {
        zuji = (ImageView) view.findViewById(R.id.zuji);
        if (zuji != null) {
            zuji.setVisibility(View.GONE);
        }

        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.startAnimation();
    }

    public void stopLoading() {
        LoadingView loadingView = (LoadingView) view.findViewById(R.id.home_loading_view);
        loadingView.setVisibility(View.GONE);
        loadingView.stopAnimation();

        zuji = (ImageView) view.findViewById(R.id.zuji);
        if (zuji != null) {
            zuji.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void invalidate(MyHomeFragment myHomeFragment) {

        stopLoading();

        myHomeFragment.showText(ruleText, appDateList);

        zuji = (ImageView) view.findViewById(R.id.zuji);
        zuji.setImageBitmap(locationBmp);

    }

//    protected void showChampionStr(TextView champion, TextView oneself, String championStr) {
//        com.alibaba.fastjson.JSONObject championJson = com.alibaba.fastjson.JSONObject.parseObject(championStr);
//        champion.setText(String.format("昨日全国第一ID：%s\n走过 %s 个地方", championJson.getString("champion"), championJson.getString("championCount")));
//        oneself.setText(String.format("昨日全国排名：%s\n走过 %s 个地方", championJson.getString("count"), championJson.getString("number")));
//    }

}
