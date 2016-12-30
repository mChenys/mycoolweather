package mchenys.net.csdn.blog.coolweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.RouteStep;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.adapter.RouteStepAdapter;
import mchenys.net.csdn.blog.coolweather.adapter.RouteType;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.BikingRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.BusLineOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.DrivingRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.MassTransitRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.PoiOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.TransitRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.WalkingRouteOverlay;

/**
 * Created by mChenys on 2016/12/24.
 */
public class BDMapActivity extends AppCompatActivity implements OnGetGeoCoderResultListener {
    private static final String TAG = "BDMapActivity";
    private static final int REQ_SEARCH_BUSLINE = 200;
    private static final int REQ_SEARCH_ROUTE = 100;
    private MapView mMapView;
    private boolean isFirstLoc;
    private ImageButton mRouteIb;
    private String startCityName, startPlaceName, mStartNodeName, mEndNodeName;
    private LinearLayout mStepLayout;//显示所有路线步骤的布局
    private FrameLayout mModeLayout;//地图模式(普通,跟随,罗盘)跟布局
    private TextView mStepDescTv;
    private ListView mRouteStepLv;//显示节点的ListView
    private int mContentHeight;
    private FrameLayout mContentFl;
    private ImageView mStepBackIv;
    private ImageButton mBackIb, mSettingIb;
    private TextView mBusSearchTv;
    private LinearLayout mTitleBarLl;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mWXLayerLl, m2DLayerLl, m3DLayerLl;
    private CheckBox mPersonalMapCb, mRealRouteCb, mHotMapCb, mModeCb;
    private ImageView mNoModeIv;
    private LocationClient mLocationClient;
    private BaiduMap mBaiduMap;
    private GeoCoder mGeoCoder;
    private BusLineResult mBusLineResult;

    private LatLng mStartLatLng, mEndLatLng;
    private int nowSearchType;
    private boolean isOpenLocation;//是否开启自动定位

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMapCustomFile(this);
        setContentView(R.layout.activity_bdmap);
        initView();
        requestLocation();
        initListener();
        setLayerByPosition();
        setMapSettingByLastSave();
    }


    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mContentFl = (FrameLayout) findViewById(R.id.fl_content);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBusSearchTv = (TextView) findViewById(R.id.tv_bus_search);
        mBackIb = (ImageButton) findViewById(R.id.ib_back);
        mSettingIb = (ImageButton) findViewById(R.id.ib_setting);
        mTitleBarLl = (LinearLayout) findViewById(R.id.ll_titlebar);
        mStepLayout = (LinearLayout) findViewById(R.id.ll_step);
        mStepDescTv = (TextView) findViewById(R.id.tv_step_desc);
        mRouteStepLv = (ListView) findViewById(R.id.lv_node_step);
        mStepLayout.setVisibility(View.GONE);
        mRouteIb = (ImageButton) findViewById(R.id.ib_route);
        mStepBackIv = (ImageView) findViewById(R.id.iv_step_back);
        mStepBackIv.setVisibility(View.GONE);
        mWXLayerLl = (LinearLayout) findViewById(R.id.ll_layer_wx);
        m2DLayerLl = (LinearLayout) findViewById(R.id.ll_layer_2d);
        m3DLayerLl = (LinearLayout) findViewById(R.id.ll_layer_3d);
        mPersonalMapCb = (CheckBox) findViewById(R.id.cb_personal_map);
        mRealRouteCb = (CheckBox) findViewById(R.id.cb_real_route);
        mHotMapCb = (CheckBox) findViewById(R.id.cb_hot_map);
        mModeCb = (CheckBox) findViewById(R.id.cb_mode);
        mModeLayout = (FrameLayout) findViewById(R.id.fl_mode_layout);
        mNoModeIv = (ImageView) findViewById(R.id.iv_no_mode);
        mNoModeIv.setVisibility(View.VISIBLE);
        mModeCb.setVisibility(View.GONE);
        setMapBottomMargin(0);

    }

    private void setMapBottomMargin(int value) {
        if (value == 0) {
            mRouteIb.setVisibility(View.VISIBLE);
            mTitleBarLl.setVisibility(View.VISIBLE);
            mModeLayout.setVisibility(View.VISIBLE);
            //隐藏节点详情
            mStepLayout.setVisibility(View.GONE);
            mStepBackIv.setVisibility(View.GONE);
        } else {
            mTitleBarLl.setVisibility(View.GONE);
            mRouteIb.setVisibility(View.GONE);
            mModeLayout.setVisibility(View.GONE);
            mStepLayout.setVisibility(View.VISIBLE);
            mStepBackIv.setVisibility(View.VISIBLE);
        }
        mBaiduMap.setViewPadding(0, 0, 0, value);
    }

    /**
     * 获取定位信息
     */
    private void requestLocation() {
        mBaiduMap.setMyLocationEnabled(true);//启用我的位置
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);//设置刷新间隔
        option.setCoorType("bd09ll"); // 设置坐标类型为百度坐标系统
        option.setIsNeedAddress(true);//需要获取当前位置的详细地址
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度模式(gps/wifi/蓝牙/网络方式定位)
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        // 初始化地理编码功能
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);

    }

    private boolean isOpenSetpLayout;

    private void initListener() {
        // 地图点击事件处理
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();//清除地图弹出物
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        //点击marker弹出显示位置
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng ll = marker.getPosition();
                if (null != mBusLineResult) {
                    for (RouteNode routeNode : mBusLineResult.getStations()) {
                        if (routeNode.getLocation() == ll) {
                            showInfoWindow(routeNode.getTitle(), ll);
                            return true;
                        }
                    }
                } else {
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(ll));
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
                }
                return true;
            }
        });
        //触摸地图
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                mNoModeIv.setVisibility(View.VISIBLE);
                mModeCb.setVisibility(View.GONE);
                isOpenLocation = false;
            }
        });
        //点击当前位置切换为跟随模式
        mNoModeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoModeIv.setVisibility(View.GONE);
                mModeCb.setVisibility(View.VISIBLE);
                isOpenLocation = true;
                mModeCb.setChecked(false);
                mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
                m2DLayerLl.performClick();
            }
        });
        mStepBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();//清除路况
                //返回路线选择页
                Intent intent = new Intent(BDMapActivity.this, RoutePlanActiviy.class);
                intent.putExtra("startLatlng", mStartLatLng);
                intent.putExtra("endLatlng", mEndLatLng);
                intent.putExtra("cityName", startCityName);
                intent.putExtra("nowSearchType", nowSearchType);
                intent.putExtra("isAutoSearch", true);
                intent.putExtra("startNodeName", mStartNodeName);
                intent.putExtra("endNodeName", mEndNodeName);
                startActivityForResult(intent, REQ_SEARCH_ROUTE, null);
                setMapBottomMargin(0);
            }
        });
        mContentFl.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContentFl.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mContentHeight = mContentFl.getHeight();
                FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) mStepLayout.getLayoutParams();
                flp.gravity = Gravity.BOTTOM;
                flp.height = mContentHeight * 1 / 4;
                mStepLayout.setLayoutParams(flp);
            }
        });
        //跳到路线规划图
        mRouteIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();
                Intent intent = new Intent(BDMapActivity.this, RoutePlanActiviy.class);
                intent.putExtra("startLatlng", mStartLatLng);
                intent.putExtra("endLatlng", mStartLatLng);
                intent.putExtra("cityName", startCityName);
                intent.putExtra("nowSearchType", nowSearchType);
                startActivityForResult(intent, REQ_SEARCH_ROUTE, null);
            }
        });
        mStepLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) mStepLayout.getLayoutParams();
                flp.gravity = Gravity.BOTTOM;
                if (!isOpenSetpLayout) {
                    isOpenSetpLayout = !isOpenSetpLayout;
                    flp.height = mContentHeight * 1 / 2;//打开

                } else {
                    isOpenSetpLayout = !isOpenSetpLayout;
                    flp.height = mContentHeight * 1 / 4;//关闭
                }
                mStepDescTv.setSelected(isOpenSetpLayout);//打开:右边箭头向下,关闭:右边箭头向上
                mStepLayout.setLayoutParams(flp);
                setMapBottomMargin(flp.height);

            }
        });
        mBackIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSettingIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.END);
            }
        });
        mBusSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();
                Intent intent = new Intent(BDMapActivity.this, PoiSearchActivity.class);
                startActivityForResult(intent, REQ_SEARCH_BUSLINE, null);
            }
        });
        mWXLayerLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                saveLayerPosition(1);
            }
        });
        m2DLayerLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                //2D平面图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(0).build();
                MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
                mBaiduMap.animateMapStatus(u);
                saveLayerPosition(2);
            }
        });
        m3DLayerLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(-45).build();
                MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
                mBaiduMap.animateMapStatus(u);
                saveLayerPosition(3);

            }
        });
        mPersonalMapCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDrawerLayout.closeDrawers();
                MapView.setMapCustomEnable(isChecked);
                saveMapSetting("isPersonal", isChecked);
            }
        });
        mRealRouteCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDrawerLayout.closeDrawers();
                mBaiduMap.setTrafficEnabled(isChecked);
                saveMapSetting("isRealRoute", isChecked);
            }
        });
        mHotMapCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDrawerLayout.closeDrawers();
                mBaiduMap.setBaiduHeatMapEnabled(isChecked);
                saveMapSetting("isHotMap", isChecked);
            }
        });
        //跟随/罗盘模式切换
        mModeCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenLocation = true;
                MyLocationConfiguration.LocationMode mode = isChecked ? MyLocationConfiguration.LocationMode.COMPASS : MyLocationConfiguration.LocationMode.FOLLOWING;
                mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mode, true, null));
                if (!isChecked) {
                    m2DLayerLl.performClick();
                }
            }
        });
    }

    private void saveLayerPosition(int position) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BDMapActivity.this).edit();
        editor.putInt("layerPosition", position);
        editor.apply();
    }

    private void saveMapSetting(String key, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BDMapActivity.this).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void setLayerByPosition() {
        int position = PreferenceManager.getDefaultSharedPreferences(this).getInt("layerPosition", 2);
        switch (position) {
            case 1://卫星图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case 2://2d平面图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(0).build();
                MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
                mBaiduMap.animateMapStatus(u);
                break;
            case 3://3d俯视图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                MapStatus mss = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(-45).build();
                MapStatusUpdate uu = MapStatusUpdateFactory.newMapStatus(mss);
                mBaiduMap.animateMapStatus(uu);
                break;
        }
    }

    private void setMapSettingByLastSave() {
        boolean isPersonal = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isPersonal", false);
        boolean isRealRoute = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isRealRoute", false);
        boolean isHotMap = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isHotMap", false);
        mPersonalMapCb.setChecked(isPersonal);
        mRealRouteCb.setChecked(isRealRoute);
        mHotMapCb.setChecked(isHotMap);
    }

    /**
     * 更新位置时调用
     *
     * @param location
     */
    private void navigateTo(BDLocation location) {
        if (!isFirstLoc) {
            //首次进来定位
            isFirstLoc = true;
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }
        if (isOpenLocation) {
            //实时跟随定位
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }
        Log.d(TAG, isOpenLocation ? "开启自动定位" : "关闭自动定位");
        mStartLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        startCityName = location.getCity();
//        startPlaceName = location.getDistrict() + location.getStreet();
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
        if (RESULT_OK == resultCode) {
            if (REQ_SEARCH_ROUTE == requestCode) {
                int linePosition = data.getIntExtra("linePosition", 1);
                boolean isSameCity = data.getBooleanExtra("isSameCity", false);//是否是同城,跨域交通用到
                RouteLine routeLine = data.getParcelableExtra("routeLine");
                nowSearchType = data.getIntExtra("nowSearchType", -1);
                mStartLatLng = data.getParcelableExtra("startLatlng");
                mEndLatLng = data.getParcelableExtra("endLatlng");
                startCityName = data.getStringExtra("startCityName");
                mStartNodeName = data.getStringExtra("startNodeName");
                mEndNodeName = data.getStringExtra("endNodeName");
                if (null == routeLine || -1 == nowSearchType) {
                    Toast.makeText(BDMapActivity.this, "获取路线失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (nowSearchType) {
                    case RouteType.MASS_TRANSIT_ROUTE://跨城交通
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
                    case RouteType.DRIVING_ROUTE://驾车
                        DrivingRouteOverlay overlay1 = new DrivingRouteOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay1);
                        DrivingRouteLine nowRouteDriving = (DrivingRouteLine) routeLine;
                        overlay1.setData(nowRouteDriving);
                        overlay1.addToMap();
                        overlay1.zoomToSpan();
                        break;
                    case RouteType.TRANSIT_ROUTE://公交
                        TransitRouteOverlay overlay2 = new TransitRouteOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay2);
                        TransitRouteLine nowRouteTransit = (TransitRouteLine) routeLine;
                        overlay2.setData(nowRouteTransit);
                        overlay2.addToMap();
                        overlay2.zoomToSpan();
                        break;
                    case RouteType.WALKING_ROUTE://步行
                        WalkingRouteOverlay overlay3 = new WalkingRouteOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay3);
                        WalkingRouteLine nowRouteWalking = (WalkingRouteLine) routeLine;
                        overlay3.setData(nowRouteWalking);
                        overlay3.addToMap();
                        overlay3.zoomToSpan();
                        break;
                    case RouteType.BIKING_ROUTE://骑行
                        BikingRouteOverlay overlay4 = new BikingRouteOverlay(mBaiduMap);
                        mBaiduMap.setOnMarkerClickListener(overlay4);
                        BikingRouteLine nowRouteBiking = (BikingRouteLine) routeLine;
                        overlay4.setData(nowRouteBiking);
                        overlay4.addToMap();
                        overlay4.zoomToSpan();
                        break;
                }
                showRouteDesc(nowSearchType, routeLine, linePosition, isSameCity);

            } else if (requestCode == REQ_SEARCH_BUSLINE) {
                int type = data.getIntExtra("type", 0);
                if (type == PoiSearchActivity.TYPE_BUS_LINE) {
                    mBusLineResult = data.getParcelableExtra("busLineResult");
                    BusLineOverlay overlay = new BusLineOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(mBusLineResult);
                    overlay.addToMap();
                    overlay.zoomToSpan();
                } else if (type == PoiSearchActivity.TYPE_HAVE_FUN) {
                    PoiResult result = data.getParcelableExtra("poiResult");
                    mBaiduMap.clear();
                    PoiOverlay overlay = new PoiOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result);
                    overlay.addToMap();
                    overlay.zoomToSpan();
                }

            }
        }
    }

    /**
     * 显示线路描述
     *
     * @param nowSearchType
     * @param routeLine
     * @param linePosition
     * @param isSameCity
     */
    private void showRouteDesc(int nowSearchType, RouteLine routeLine, int linePosition, boolean isSameCity) {
        StringBuilder desc = new StringBuilder();
        switch (nowSearchType) {
            case RouteType.TRANSIT_ROUTE:
            case RouteType.WALKING_ROUTE:
            case RouteType.BIKING_ROUTE:
                desc.append("路线" + linePosition);
                int time = routeLine.getDuration();
                if (time / 3600 == 0) {
                    desc.append(" 大约需要：" + time / 60 + "分钟");
                } else {
                    desc.append(" 大约需要：" + time / 3600 + "小时" + (time % 3600) / 60 + "分钟");
                }
                desc.append(" 距离大约是：" + routeLine.getDistance() + "米");
                break;
            case RouteType.DRIVING_ROUTE:
                desc.append("路线" + linePosition);
                DrivingRouteLine drivingRouteLine = (DrivingRouteLine) routeLine;
                desc.append(" 红绿灯数：" + drivingRouteLine.getLightNum());
                desc.append(" 拥堵距离为：" + drivingRouteLine.getCongestionDistance() + "米");
                break;
            case RouteType.MASS_TRANSIT_ROUTE:
                MassTransitRouteLine massTransitRouteLine = (MassTransitRouteLine) routeLine;
                desc.append("路线" + linePosition);
                desc.append(" 预计达到时间：" + massTransitRouteLine.getArriveTime());
                desc.append(" 总票价：" + massTransitRouteLine.getPrice() + "元");
                break;
        }
        mStepDescTv.setText(desc.toString());
        List<RouteStep> steps = null;
        if (nowSearchType == 0) {
            MassTransitRouteLine massroute = (MassTransitRouteLine) routeLine;
            int size = 0;
            if (isSameCity) {
                size = massroute.getNewSteps().size();
            } else {
                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
                    size += massroute.getNewSteps().get(i).size();
                }
            }
            steps = new ArrayList<>();
            for (int nodeIndex = 0; nodeIndex < size; nodeIndex++) {
                if (isSameCity) {
                    // 同城
                    steps.add(massroute.getNewSteps().get(nodeIndex).get(0));
                } else {
                    // 跨城
                    int num = 0;
                    for (int j = 0; j < massroute.getNewSteps().size(); j++) {
                        num += massroute.getNewSteps().get(j).size();
                        if (nodeIndex - num < 0) {
                            int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
                            steps.add(massroute.getNewSteps().get(j).get(k));
                            break;
                        }
                    }
                }
            }
        } else {
            steps = routeLine.getAllStep();
        }
        if (null != steps && steps.size() > 0) {
            //显示路线详情
            setMapBottomMargin(mContentHeight * 1 / 4);
            RouteStepAdapter stepAdapter = new RouteStepAdapter(this, steps);
            mRouteStepLv.setAdapter(stepAdapter);
        }
        mRouteStepLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RouteStep step = (RouteStep) parent.getItemAtPosition(position);
                LatLng nodeLocation = null;
                if (step instanceof DrivingRouteLine.DrivingStep) {
                    DrivingRouteLine.DrivingStep drivingStep = (DrivingRouteLine.DrivingStep) step;
                    nodeLocation = drivingStep.getEntrance().getLocation();
                } else if (step instanceof WalkingRouteLine.WalkingStep) {
                    WalkingRouteLine.WalkingStep walkingStep = (WalkingRouteLine.WalkingStep) step;
                    nodeLocation = walkingStep.getEntrance().getLocation();
                } else if (step instanceof TransitRouteLine.TransitStep) {
                    TransitRouteLine.TransitStep transitStep = (TransitRouteLine.TransitStep) step;
                    nodeLocation = transitStep.getEntrance().getLocation();

                } else if (step instanceof BikingRouteLine.BikingStep) {
                    BikingRouteLine.BikingStep bikingStep = (BikingRouteLine.BikingStep) step;
                    nodeLocation = bikingStep.getEntrance().getLocation();

                } else if (step instanceof MassTransitRouteLine.TransitStep) {
                    MassTransitRouteLine.TransitStep massStep = (MassTransitRouteLine.TransitStep) step;
                    nodeLocation = massStep.getStartLocation();

                }
                // 移动节点至中心
                if (null != nodeLocation) {
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
                    //查询当前选中位置
                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(nodeLocation));
                }
            }
        });
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result != null && result.error == SearchResult.ERRORNO.NO_ERROR) {
            showInfoWindow(result.getAddress(), result.getLocation());
        }

    }

    // show popup
    private void showInfoWindow(String txt, LatLng latLng) {
        TextView popupText = new TextView(BDMapActivity.this);
        popupText.setPadding(5, 5, 5, 30);
        popupText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(txt);
        mBaiduMap.showInfoWindow(new InfoWindow(popupText, latLng, 0));
    }

    @Override
    public void onBackPressed() {
        if (mStepLayout.isShown()) {
            setMapBottomMargin(0);
            return;
        }
        super.onBackPressed();
    }
}
