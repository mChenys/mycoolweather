package mchenys.net.csdn.blog.coolweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.RouteStep;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.BikingRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.DrivingRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.MassTransitRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.TransitRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.WalkingRouteOverlay;

/**
 * Created by mChenys on 2016/12/24.
 */
public class BDMapActivity extends AppCompatActivity {
    private static final String TAG = "BDMapActivity";
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Toolbar mToolbar;
    private LocationClient mLocationClient;
    private boolean isFirstLoc;
    private MenuItem mTrafficItem, mHeatItem;
    private Button mChangeMarkerBtn; //指针的类型,普通,跟随,罗盘
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private ImageButton mRouteIb;
    private String startCityName, startPlaceName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMapCustomFile(this);
        setContentView(R.layout.activity_bdmap);
        initView();
        requestLocation();
        initListener();
    }


    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRouteIb = (ImageButton) findViewById(R.id.ib_route);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mChangeMarkerBtn = (Button) findViewById(R.id.btn_chg_marker);
        mChangeMarkerBtn.setOnClickListener(mChangeMarkerListener);

    }

    /**
     * 获取定位信息
     */
    private void requestLocation() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);//启用我的位置

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);//设置刷新间隔
        option.setCoorType("bd09ll"); // 设置坐标类型为百度坐标系统
        option.setIsNeedAddress(true);//需要获取当前位置的详细地址
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度模式(gps/wifi/蓝牙/网络方式定位)
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }

    private void initListener() {
        mRouteIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BDMapActivity.this, RoutePlanActiviy.class);
                intent.putExtra("cityName", startCityName);
                intent.putExtra("address", startPlaceName);
                startActivityForResult(intent, 100, null);
            }
        });
    }

    /**
     * 更新位置时调用
     *
     * @param location
     */
    private void navigateTo(BDLocation location) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
        if (!isFirstLoc) {
            isFirstLoc = true;
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(16.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
        startCityName = location.getCity();
        startPlaceName = location.getDistrict() + location.getStreet();
    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d(TAG, "onReceiveLocation:" + bdLocation.getProvince() + "-" + bdLocation.getCity() + "-" + bdLocation.getDistrict());
            // map view 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            if (!TextUtils.isEmpty(bdLocation.getProvince()) &&
                    !TextUtils.isEmpty(bdLocation.getCity()) &&
                    !TextUtils.isEmpty(bdLocation.getDistrict())) {
                navigateTo(bdLocation);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "普通地图");
        menu.add(0, 1, 1, "卫星地图");
        menu.add(0, 2, 2, "个性化地图");
        mTrafficItem = menu.add(0, 3, 3, "开启实时交通图");
        mHeatItem = menu.add(0, 4, 4, "开启城市热力图");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case 0:
                //普通地图
                MapView.setMapCustomEnable(false);
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case 2:
                //开启个性化地图
                MapView.setMapCustomEnable(true);
                break;
            case 3:
                //开启交通图
                if (mTrafficItem.getTitle().equals("开启实时交通图")) {
                    mBaiduMap.setTrafficEnabled(true);
                    mTrafficItem.setTitle("关闭实时交通图");
                } else {
                    mBaiduMap.setTrafficEnabled(false);
                    mTrafficItem.setTitle("开启实时交通图");
                }
                break;

            case 4:
                //显示百度热力图
                if (mHeatItem.getTitle().equals("开启热力图")) {
                    mBaiduMap.setBaiduHeatMapEnabled(true);
                    mHeatItem.setTitle("关闭热力图");
                } else {
                    mBaiduMap.setBaiduHeatMapEnabled(false);
                    mHeatItem.setTitle("开启热力图");
                }
                break;
        }
        return true;
    }

    public View.OnClickListener mChangeMarkerListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (mCurrentMode) {
                case NORMAL:
                    mChangeMarkerBtn.setText("跟随");
                    mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                    mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));
                    break;
                case COMPASS:
                    mChangeMarkerBtn.setText("普通");
                    mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                    mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));
                    break;
                case FOLLOWING:
                    mChangeMarkerBtn.setText("罗盘");
                    mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                    mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MapView.setMapCustomEnable(false);
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    // 设置个性化地图config文件路径
    private void setMapCustomFile(Context context) {
        FileOutputStream out = null;
        InputStream inputStream = null;
        String moduleName = null;
        try {
            inputStream = context.getAssets().open("customConfigdir/custom_config.txt");
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            moduleName = context.getFilesDir().getAbsolutePath();
            File f = new File(moduleName + "/" + "custom_config.txt");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            out = new FileOutputStream(f);
            out.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MapView.setCustomMapStylePath(moduleName + "/custom_config.txt");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && 100 == requestCode) {
            int nowSearchType = data.getIntExtra("nowSearchType", -1);
            RouteLine routeLine = data.getParcelableExtra("routeLine");
            boolean isSameCity = data.getBooleanExtra("isSameCity", false);//是否是同城,跨域交通用到
            if (null == routeLine || -1 == nowSearchType) {
                Toast.makeText(BDMapActivity.this, "获取路线失败", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (nowSearchType) {
                case 0://跨城交通
                    MassTransitRouteOverlay overlay0 = new MassTransitRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay0);
                    MassTransitRouteLine nowRouteMass = (MassTransitRouteLine) routeLine;
                    overlay0.setData(nowRouteMass);
                    if (isSameCity) {
                        overlay0.setSameCity(true); // 同城
                    } else {
                        overlay0.setSameCity(false);// 跨城
                    }
                    overlay0.addToMap();
                    overlay0.zoomToSpan();
                    break;
                case 1://驾车
                    DrivingRouteOverlay overlay1 = new DrivingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay1);
                    DrivingRouteLine nowRouteDriving = (DrivingRouteLine) routeLine;
                    overlay1.setData(nowRouteDriving);
                    overlay1.addToMap();
                    overlay1.zoomToSpan();
                    break;
                case 2://公交
                    TransitRouteOverlay overlay2 = new TransitRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay2);
                    TransitRouteLine nowRouteTransit = (TransitRouteLine) routeLine;
                    overlay2.setData(nowRouteTransit);
                    overlay2.addToMap();
                    overlay2.zoomToSpan();
                    break;
                case 3://步行
                    WalkingRouteOverlay overlay3 = new WalkingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay3);
                    WalkingRouteLine nowRouteWalking = (WalkingRouteLine) routeLine;
                    overlay3.setData(nowRouteWalking);
                    overlay3.addToMap();
                    overlay3.zoomToSpan();
                    break;
                case 4://骑行
                    BikingRouteOverlay overlay4 = new BikingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay4);
                    BikingRouteLine nowRouteBiking = (BikingRouteLine) routeLine;
                    overlay4.setData(nowRouteBiking);
                    overlay4.addToMap();
                    overlay4.zoomToSpan();
                    break;
            }
            List<RouteStep> step = routeLine.getAllStep();
            if (step.size() > 0) {

            }
        }
    }
}
