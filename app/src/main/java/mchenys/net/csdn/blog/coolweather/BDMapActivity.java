package mchenys.net.csdn.blog.coolweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.RouteStep;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.DrivingRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.MassTransitRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.TransitRouteOverlay;
import mchenys.net.csdn.blog.coolweather.mapapi.overlayutil.WalkingRouteOverlay;

/**
 * Created by mChenys on 2016/12/24.
 */
public class BDMapActivity extends AppCompatActivity implements OnGetGeoCoderResultListener {
    private static final String TAG = "BDMapActivity";
    private MapView mMapView;
    private Toolbar mToolbar;
    private boolean isFirstLoc;
    private MenuItem mTrafficItem, mHeatItem;
    private Button mChangeMarkerBtn;
    private ImageButton mRouteIb;
    private String startCityName, startPlaceName;
    private LinearLayout mStepLayout;//显示所有路线步骤的布局
    private TextView mStepDescTv;
    private ListView mRouteStepLv;//显示节点的ListView
    private int mContentHeight;
    private FrameLayout mContentFl;
    private ImageView mStepBackIv;
    private int m100dp;
    //指针的类型,普通,跟随,罗盘
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private LocationClient mLocationClient;
    private BaiduMap mBaiduMap;
    private GeoCoder mGeoCoder;

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
        mContentFl = (FrameLayout) findViewById(R.id.fl_content);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mChangeMarkerBtn = (Button) findViewById(R.id.btn_chg_marker);
        mChangeMarkerBtn.setOnClickListener(mChangeMarkerListener);
        mStepLayout = (LinearLayout) findViewById(R.id.ll_step);
        mStepDescTv = (TextView) findViewById(R.id.tv_step_desc);
        mRouteStepLv = (ListView) findViewById(R.id.lv_node_step);
        mStepLayout.setVisibility(View.GONE);
        mRouteIb = (ImageButton) findViewById(R.id.ib_route);
        mStepBackIv = (ImageView) findViewById(R.id.iv_step_back);
        mStepBackIv.setVisibility(View.GONE);
        m100dp = (int) getResources().getDimension(R.dimen.start_route_bottom_margin);
        setStartRouteBottomMargin(m100dp);

    }

    private void setStartRouteBottomMargin(int value) {
        if (value == m100dp) {
            ((FrameLayout.LayoutParams) mRouteIb.getLayoutParams()).bottomMargin = m100dp;
            mBaiduMap.setViewPadding(0, 0, 0, 0);
        } else {
            ((FrameLayout.LayoutParams) mRouteIb.getLayoutParams()).bottomMargin = value + m100dp;
            mBaiduMap.setViewPadding(0, 0, 0, value);
        }

    }

    /**
     * 获取定位信息
     */
    private void requestLocation() {
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

        // 初始化地理编码功能
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);

    }

    private boolean isOpen;

    private void initListener() {
        // 地图点击事件处理
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();//清楚地图弹出物
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        //点击marker移动并弹出显示位置
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng ll = marker.getPosition();
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(ll));
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
                return true;
            }
        });
        mStepBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏节点详情
                mStepLayout.setVisibility(View.GONE);
                mStepBackIv.setVisibility(View.GONE);
                setStartRouteBottomMargin(m100dp);
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
                Intent intent = new Intent(BDMapActivity.this, RoutePlanActiviy.class);
                intent.putExtra("cityName", startCityName);
                intent.putExtra("address", startPlaceName);
                startActivityForResult(intent, 100, null);
            }
        });
        mStepLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) mStepLayout.getLayoutParams();
                flp.gravity = Gravity.BOTTOM;
                if (!isOpen) {
                    isOpen = !isOpen;
                    flp.height = mContentHeight * 1 / 2;//打开

                } else {
                    isOpen = !isOpen;
                    flp.height = mContentHeight * 1 / 4;//关闭
                }
                mStepDescTv.setSelected(isOpen);//打开:右边箭头向下,关闭:右边箭头向上
                mStepLayout.setLayoutParams(flp);
                setStartRouteBottomMargin(flp.height);

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
            int linePosition = data.getIntExtra("linePosition", 1);
            boolean isSameCity = data.getBooleanExtra("isSameCity", false);//是否是同城,跨域交通用到
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
            setStartRouteBottomMargin(mContentHeight * 1 / 4);
            RouteStepAdapter stepAdapter = new RouteStepAdapter(this, steps);
            mRouteStepLv.setAdapter(stepAdapter);
            mStepLayout.setVisibility(View.VISIBLE);
            mStepBackIv.setVisibility(View.VISIBLE);
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
            // show popup
            TextView popupText = new TextView(BDMapActivity.this);
            popupText.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            popupText.setBackgroundResource(R.drawable.popup);
            popupText.setTextColor(0xFF000000);
            popupText.setText(result.getAddress());
            mBaiduMap.showInfoWindow(new InfoWindow(popupText, result.getLocation(), 0));
        }

    }
}
