package mchenys.net.csdn.blog.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by mChenys on 2016/12/24.
 */
public class BDMapActivity extends AppCompatActivity {
    private static final String TAG = "BDMapActivity";
    private MapView mMapView;
    private BaiduMap baiduMap;
    private LocationClient mLocationClient;
    private boolean isFirstLocate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBaidu();
        setContentView(R.layout.activity_bdmap);
        initView();
        requestLocation();
    }


    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void initBaidu() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
    }

    /**
     * 获取定位信息
     */
    private void requestLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(60 * 1000);//设置刷新间隔
        option.setIsNeedAddress(true);//需要获取当前位置的详细地址
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度模式(gps/wifi/蓝牙/网络方式定位)
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);//启用我的位置
        MapStatusUpdate update = MapStatusUpdateFactory.zoomTo(16f);
        baiduMap.animateMapStatus(update);
    }

    private void navigateTo(BDLocation location) {
        if (!isFirstLocate) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            double latitude = prefs.getString("latitude", null) == null ? -1 : Double.parseDouble(prefs.getString("latitude", null));
            double longitude = prefs.getString("longitude", null) == null ? -1 : Double.parseDouble(prefs.getString("longitude", null));
            Log.d(TAG, "navigateTo 首次进入:latitude:" + latitude + " longitude:" + longitude);
            if (-1 != latitude && -1 != longitude) {
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                baiduMap.animateMapStatus(update);

            }

            isFirstLocate = true;
        }
        Log.d(TAG, "navigateTo 再次进入:latitude:" + location.getLatitude() + " longitude:" + location.getLongitude());
        MyLocationData data = new MyLocationData.Builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        baiduMap.setMyLocationData(data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d(TAG, "onReceiveLocation:" + bdLocation.getProvince() + "-" + bdLocation.getCity() + "-" + bdLocation.getDistrict());
            if (!TextUtils.isEmpty(bdLocation.getProvince()) &&
                    !TextUtils.isEmpty(bdLocation.getCity()) &&
                    !TextUtils.isEmpty(bdLocation.getDistrict())) {
                navigateTo(bdLocation);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mMapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
