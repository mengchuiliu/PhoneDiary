package com.rdcx.randian;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;
import java.util.List;

public class GetLocationActivity extends AppCompatActivity implements
        AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener {

    ListView listView;
    private LocationManagerProxy mLocationManagerProxy;
    Double geoLat, geoLng;
    GeocodeSearch geocoderSearch;
    List<locaBean> loca = new ArrayList<>();
    MyAdapter adapter;
    ProgressBar bar;
    TextView loca_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).addActivity(this);
        setContentView(R.layout.activity_get_location);
        adapter = new MyAdapter();
        // 初始化定位，只采用网络定位
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.setGpsEnable(false);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, this);

        geocoderSearch = new GeocodeSearch(getApplicationContext());
        geocoderSearch.setOnGeocodeSearchListener(this);
        init();
    }

    private void init() {
        bar = (ProgressBar) findViewById(R.id.bar);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent();
                intent.putExtra("location", loca.get(arg2).getContent());
                setResult(10, intent);
                finish();
            }
        });
        findViewById(R.id.loca_back).setOnClickListener(onClickListener);
        loca_show = (TextView) findViewById(R.id.loca_show);
        loca_show.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.loca_back:
                    setResult(8);
                    finish();
                    break;
                case R.id.loca_show:
                    setResult(8);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    class MyAdapter extends BaseAdapter {
        private List<locaBean> list;

        public void setDate(List<locaBean> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return list == null ? null : list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Hodler hodler;
            if (convertView == null) {
                hodler = new Hodler();
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_list, null);
                hodler.title = (TextView) convertView.findViewById(R.id.loc_title);
                hodler.content = (TextView) convertView.findViewById(R.id.loc_content);
                convertView.setTag(hodler);
            } else {
                hodler = (Hodler) convertView.getTag();
            }
            hodler.title.setText(list.get(position).getTitle());
            hodler.content.setText(list.get(position).getContent());
            return convertView;
        }

        class Hodler {
            TextView title;
            TextView content;
        }
    }

    @Override
    public void onLocationChanged(Location arg0) {

    }

    @Override
    public void onProviderDisabled(String arg0) {

    }

    @Override
    public void onProviderEnabled(String arg0) {

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
            // 获取位置信息
            geoLat = amapLocation.getLatitude();
            geoLng = amapLocation.getLongitude();
            // Log.e("my_log", "geoLat===" + geoLat + "==" + geoLng);

            // latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(geoLat, geoLng), 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        } else {
            Log.e("my_log", "Location ERR:" + amapLocation.getAMapException().getErrorCode());
            getBar();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除定位请求
        mLocationManagerProxy.removeUpdates(this);
        // 销毁定位
        mLocationManagerProxy.destroy();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        Log.e("my_log", "rCode===" + rCode);
        List<PoiItem> list = result.getRegeocodeAddress().getPois();
        if (rCode == 0) {
            if (list != null && list.size() > 0) {
                String str = result.getRegeocodeAddress().getProvince()
                        + result.getRegeocodeAddress().getCity()
                        + result.getRegeocodeAddress().getTownship();
                for (int i = 0; i < list.size(); i++) {
                    locaBean bean = new locaBean();
                    bean.setTitle(list.get(i).getTitle());
                    bean.setContent(str + list.get(i).getTitle());
                    loca.add(bean);
                }
                bar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                adapter.setDate(loca);
                adapter.notifyDataSetChanged();
            } else {
                Log.e("my_log", "result===" + result);
                bar.setVisibility(View.GONE);
                loca_show.setText("无位置信息");
            }
        } else if (rCode == 27) {
            getBar();
            // 搜索失败,请检查网络连接！
            Toast.makeText(getApplicationContext(), "位置搜索失败,请检查网络连接！",
                    Toast.LENGTH_SHORT).show();
        } else if (rCode == 32) {
            getBar();
            // key验证无效！
            Toast.makeText(getApplicationContext(), "地图key验证无效！", Toast.LENGTH_SHORT).show();
        } else {
            getBar();
            // 未知错误，请稍后重试!错误码为
            Toast.makeText(getApplicationContext(), "地图key验证无效！" + rCode, Toast.LENGTH_SHORT).show();
        }
    }

    class locaBean {
        private String title;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    private void getBar() {
        bar.setVisibility(View.GONE);
        loca_show.setText("位置搜索失败");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(8);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
