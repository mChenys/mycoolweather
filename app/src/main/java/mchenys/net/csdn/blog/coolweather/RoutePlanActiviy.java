package mchenys.net.csdn.blog.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import mchenys.net.csdn.blog.coolweather.adapter.RouteLineAdapter;
import mchenys.net.csdn.blog.coolweather.adapter.RouteLineSuggestAdapter;
import mchenys.net.csdn.blog.coolweather.adapter.RouteType;
import mchenys.net.csdn.blog.coolweather.gson.SuggestAddressInfo;

/**
 * Created by mChenys on 2016/12/27.
 */
public class RoutePlanActiviy extends AppCompatActivity implements OnGetRoutePlanResultListener, OnGetGeoCoderResultListener {
    private static final String TAG = "RoutePlanActiviy";
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ListView mRoutePlanLv;
    private TextView mStartNodeTv, mEndNodeTv;
    private TextView mSearchTv;
    private TextView mSuggestTv;
    private ProgressDialog mProgressDialog;
    private String[] title = new String[]{"跨城交通", "驾车", "公交", "步行", "骑行"};
    private int nowSearchType = RouteType.MASS_TRANSIT_ROUTE; // 当前进行的检索，供判断浏览节点时结果使用。
    private String startCityName, startPlaceName;
    private String endCityName, endPlaceName;
    private RoutePlanSearch mSearch;
    private boolean isSameCity;//是否是同城,跨域交通用到
    //    private String myCityName, myPlaceName;
    private GeoCoder mGeoCoder;
    private LatLng mStartLatLng, mEndLatLng;
    private RouteLineSuggestAdapter mSuggestAdapter;
    //0:建议列表表示起始点地理编码查询,1:建议列表表示终点地理编码查询,2:表示非建议列表表示起始点地理编码查询,3:非建议列表表示终点地理编码查询
    private int mGeoCoderType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roue_plan);
        initSearch();
        initView();
        initListener();
        initData();
    }

    private void initSearch() {
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        // 初始化地理编码功能
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);
    }


    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mRoutePlanLv = (ListView) findViewById(R.id.lv_route);
        mStartNodeTv = (TextView) findViewById(R.id.tv_start);
        mEndNodeTv = (TextView) findViewById(R.id.tv_end);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mSearchTv = (TextView) findViewById(R.id.tv_search);
        mSuggestTv = (TextView) findViewById(R.id.tv_suggest_info);
        for (int i = 0; i < title.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(title[i]));
        }
    }

    private void initData() {
        mStartLatLng = getIntent().getParcelableExtra("startLatlng");
        mEndLatLng = getIntent().getParcelableExtra("endLatlng");
        startCityName = getIntent().getStringExtra("cityName");
        nowSearchType = getIntent().getIntExtra("nowSearchType", 0);
        boolean isAutoSearch = getIntent().getBooleanExtra("isAutoSearch", false);
        if (isAutoSearch) {
            String startNodeName = getIntent().getStringExtra("startNodeName");
            String endNodeName = getIntent().getStringExtra("endNodeName");
            mStartNodeTv.setText(startNodeName);
            mEndNodeTv.setText(endNodeName);
            searchRoute();
        }
    }


    private void initListener() {
        mStartNodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToFindCity(0);
            }
        });
        mEndNodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToFindCity(1);
            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                nowSearchType = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValue()) {
                    searchRoute();
                }
            }
        });
        mRoutePlanLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BaseAdapter adapter = (BaseAdapter) parent.getAdapter();
                if (adapter instanceof RouteLineAdapter) {
                    //显示在地图
                    RouteLine routeLine = (RouteLine) parent.getItemAtPosition(position);
                    showRouteLineByMap(routeLine, position + 1);
                } else if (adapter instanceof RouteLineSuggestAdapter) {
                    //选则了建议路线
                    mSuggestAdapter = (RouteLineSuggestAdapter) adapter;
                    SuggestAddressInfo sai = (SuggestAddressInfo) parent.getItemAtPosition(position);
                    if (sai.state == 1) {
                        startPlaceName = sai.pi.address + " " + sai.pi.city + " " + sai.pi.name;
                        startGeoCode(0);
                        mStartNodeTv.setText(startPlaceName);
                        showProgressDialog();

                    } else if (sai.state == 3) {
                        endPlaceName = sai.pi.address + " " + sai.pi.city + " " + sai.pi.name;
                        startGeoCode(1);
                        mEndNodeTv.setText(endPlaceName);
                        showProgressDialog();

                    }
                }
            }
        });

    }

    /**
     * 开始路线搜索
     */
    private void searchRoute() {
        showProgressDialog();
        PlanNode stNode = PlanNode.withLocation(mStartLatLng);
        PlanNode enNode = PlanNode.withLocation(mEndLatLng);

        Log.d(TAG, "#start search->startCityName:" + startCityName + " startPlaceName:" + startPlaceName + " endCityName:" + endCityName + " endPlaceName:" + endPlaceName);

        switch (nowSearchType) {
            case RouteType.MASS_TRANSIT_ROUTE:
                mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stNode).to(enNode));
                break;
            case RouteType.DRIVING_ROUTE:
                mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
                break;
            case RouteType.TRANSIT_ROUTE:
                mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city(startCityName).to(enNode));
                break;
            case RouteType.WALKING_ROUTE:
                mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
                break;
            case RouteType.BIKING_ROUTE:
                mSearch.bikingSearch((new BikingRoutePlanOption()).from(stNode).to(enNode));
                break;
        }
    }

    /**
     * 在地图上显示路线
     *
     * @param routeLine
     * @param linePosition
     */
    private void showRouteLineByMap(RouteLine routeLine, int linePosition) {
        Intent intent = getIntent();
        intent.putExtra("routeLine", routeLine);
        intent.putExtra("nowSearchType", nowSearchType);
        intent.putExtra("isSameCity", isSameCity);
        intent.putExtra("linePosition", linePosition);
        intent.putExtra("startLatlng", mStartLatLng);
        intent.putExtra("endLatlng", mEndLatLng);
        intent.putExtra("startCityName", startCityName);
        intent.putExtra("startNodeName", mStartNodeTv.getText().toString().trim());
        intent.putExtra("endNodeName", mEndNodeTv.getText().toString().trim());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 开始地理位置和坐标转换
     *
     * @param type
     */
    private void startGeoCode(int type) {
        mGeoCoderType = type;
        if (type == 0 || type == 2) {
            mGeoCoder.geocode(new GeoCodeOption().city(startCityName).address(startPlaceName));
        } else {
            mGeoCoder.geocode(new GeoCodeOption().city(endCityName).address(endPlaceName));
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        Log.d(TAG, "地理编码成功");
        switch (mGeoCoderType) {
            case 0://建议起点
                mStartLatLng = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
                mSuggestAdapter.resetData(SuggestAddressInfo.endNodeList);
                mRoutePlanLv.setVisibility(View.VISIBLE);
                Log.d(TAG, "#start search->起点维度:" + mStartLatLng.latitude + " 起点经度:" + mStartLatLng.longitude);

                break;
            case 1://建议终点
                mEndLatLng = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
                mRoutePlanLv.setVisibility(View.GONE);
                Log.d(TAG, "#start search->终点维度:" + mEndLatLng.latitude + " 终点经度:" + mEndLatLng.longitude);

                break;
            case 2://非建议起点
                mStartLatLng = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
                Log.d(TAG, "#start search->起点维度:" + mStartLatLng.latitude + " 起点经度:" + mStartLatLng.longitude);
                break;
            case 3://非建议终点
                mEndLatLng = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
                Log.d(TAG, "#start search->终点维度:" + mEndLatLng.latitude + " 终点经度:" + mEndLatLng.longitude);

                break;
        }

        closeProgressDialog();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void closeProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("正在搜索");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private boolean checkValue() {
        String end = mEndNodeTv.getText().toString().trim();
        if (TextUtils.isEmpty(end)) {
            Toast.makeText(RoutePlanActiviy.this, "请输入终点位置", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (null != mStartLatLng && null != mEndLatLng &&
                mStartLatLng.latitude == mEndLatLng.latitude &&
                mStartLatLng.longitude == mEndLatLng.longitude) {
            Toast.makeText(RoutePlanActiviy.this, "起点位置和终点位置相同或接近", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            closeProgressDialog();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点模糊，获取建议列表
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                RouteLineSuggestAdapter suggestAdapter = new RouteLineSuggestAdapter(this, SuggestAddressInfo.parseList(suggestAddrInfo));
                mRoutePlanLv.setAdapter(suggestAdapter);
                Log.d(TAG, "跨城公交搜索失败,显示建议列表");
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            closeProgressDialog();
            isSameCity = result.getOrigin().getCityId() == result.getDestination().getCityId();
            if (result.getRouteLines().size() > 1) {
                // 列表选择
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteType.MASS_TRANSIT_ROUTE);
                mRoutePlanLv.setAdapter(routeLineAdapter);
                mRoutePlanLv.setVisibility(View.VISIBLE);

            } else if (result.getRouteLines().size() == 1) {
                showRouteLineByMap(result.getRouteLines().get(0), 1);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }
        }

    }


    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            closeProgressDialog();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                RouteLineSuggestAdapter suggestAdapter = new RouteLineSuggestAdapter(this, SuggestAddressInfo.parseList(suggestAddrInfo));
                mRoutePlanLv.setAdapter(suggestAdapter);
                Log.d(TAG, "驾车搜索失败,显示建议列表");
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            closeProgressDialog();

            if (result.getRouteLines().size() > 1) {
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteType.DRIVING_ROUTE);

                mRoutePlanLv.setAdapter(routeLineAdapter);
                mRoutePlanLv.setVisibility(View.VISIBLE);

            } else if (result.getRouteLines().size() == 1) {
                showRouteLineByMap(result.getRouteLines().get(0), 1);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }


    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            closeProgressDialog();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                RouteLineSuggestAdapter suggestAdapter = new RouteLineSuggestAdapter(this, SuggestAddressInfo.parseList(suggestAddrInfo));
                mRoutePlanLv.setAdapter(suggestAdapter);
                Log.d(TAG, "公交搜索失败,显示建议列表");
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            closeProgressDialog();

            if (result.getRouteLines().size() > 1) {

                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteType.TRANSIT_ROUTE);

                mRoutePlanLv.setAdapter(routeLineAdapter);
                mRoutePlanLv.setVisibility(View.VISIBLE);

            } else if (result.getRouteLines().size() == 1) {
                showRouteLineByMap(result.getRouteLines().get(0), 1);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            closeProgressDialog();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                RouteLineSuggestAdapter suggestAdapter = new RouteLineSuggestAdapter(this, SuggestAddressInfo.parseList(suggestAddrInfo));
                mRoutePlanLv.setAdapter(suggestAdapter);
                Log.d(TAG, "步行搜索失败,显示建议列表");
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            closeProgressDialog();

            if (result.getRouteLines().size() > 1) {

                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteType.WALKING_ROUTE);

                mRoutePlanLv.setAdapter(routeLineAdapter);
                mRoutePlanLv.setVisibility(View.VISIBLE);
            } else if (result.getRouteLines().size() == 1) {
                showRouteLineByMap(result.getRouteLines().get(0), 1);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }

    }


    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            closeProgressDialog();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                RouteLineSuggestAdapter suggestAdapter = new RouteLineSuggestAdapter(this, SuggestAddressInfo.parseList(suggestAddrInfo));
                mRoutePlanLv.setAdapter(suggestAdapter);
                Log.d(TAG, "骑行搜索失败,显示建议列表");
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            closeProgressDialog();

            if (result.getRouteLines().size() > 1) {

                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteType.BIKING_ROUTE);
                mRoutePlanLv.setAdapter(routeLineAdapter);
                mRoutePlanLv.setVisibility(View.VISIBLE);

            } else if (result.getRouteLines().size() == 1) {
                showRouteLineByMap(result.getRouteLines().get(0), 1);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    private void jumpToFindCity(int reqCode) {
        startActivityForResult(new Intent(this, FindCityActivity.class), reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case 0:
                    startCityName = data.getStringExtra("cityName");
                    startPlaceName = data.getStringExtra("address");
                    if ("我的位置".equals(startCityName)) {
                        mStartNodeTv.setText("我的位置");
                    } else {
                        mStartNodeTv.setText(startCityName + " " + startPlaceName);
                        startGeoCode(2);
                    }
                    break;
                case 1:
                    endCityName = data.getStringExtra("cityName");
                    endPlaceName = data.getStringExtra("address");
                    if ("我的位置".equals(endCityName)) {
                        mEndNodeTv.setText("我的位置");
                    } else {
                        mEndNodeTv.setText(endCityName + " " + endPlaceName);
                        startGeoCode(3);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        mSearch.destroy();
        super.onDestroy();
    }
}
