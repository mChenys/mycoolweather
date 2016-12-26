package mchenys.net.csdn.blog.coolweather.baidu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mchenys.net.csdn.blog.coolweather.R;

/**
 * Created by mChenys on 2016/12/24.
 */
public class BDMapActivity extends AppCompatActivity {
    private static final String TAG = "BDMapActivity";
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Toolbar mToolbar;
    private LocationClient mLocationClient;
    private boolean isFirstLocate;
    private MenuItem mTrafficItem, mHeatItem;
    private Button mChangeMarkerBtn; //指针的类型,普通,跟随,罗盘
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private ImageButton mRouteIb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMapCustomFile(this);
        setContentView(R.layout.activity_bdmap);
        initView();
        requestLocation();
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
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);//需要获取当前位置的详细地址
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度模式(gps/wifi/蓝牙/网络方式定位)
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }

    /**
     * 更新位置时调用
     *
     * @param location
     */
    private void navigateTo(BDLocation location) {
        if (!isFirstLocate) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            double latitude = prefs.getString("latitude", null) == null ? -1 : Double.parseDouble(prefs.getString("latitude", null));
            double longitude = prefs.getString("longitude", null) == null ? -1 : Double.parseDouble(prefs.getString("longitude", null));
            Log.d(TAG, "navigateTo 首次进入:latitude:" + latitude + " longitude:" + longitude);
            if (-1 != latitude && -1 != longitude) {
                LatLng ll = new LatLng(latitude, longitude);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(16.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            isFirstLocate = true;
        }
        Log.d(TAG, "navigateTo 再次进入:latitude:" + location.getLatitude() + " longitude:" + location.getLongitude());
        MyLocationData data = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        mBaiduMap.setMyLocationData(data);
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
}
