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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
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

/**
 * Created by mChenys on 2016/12/27.
 */
public class RoutePlanActiviy extends AppCompatActivity implements OnGetRoutePlanResultListener {
    private static final String TAG = "RoutePlanActiviy";
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ListView mListView;
    private TextView mStartNodeTv, mEndNodeTv;
    private TextView mSearchTv;
    private ProgressDialog mProgressDialog;
    private String[] title = new String[]{"跨城交通", "驾车", "公交", "步行", "骑行"};
    private int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。
    private String startCityName, startPlaceName;
    private String endCityName, endPlaceName;
    private RoutePlanSearch mSearch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roue_plan);
        initSearch();
        initView();
        initData();
        initListener();
    }

    private void initSearch() {
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }


    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mListView = (ListView) findViewById(R.id.lv_route);
        mStartNodeTv = (TextView) findViewById(R.id.tv_start);
        mEndNodeTv = (TextView) findViewById(R.id.tv_end);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mSearchTv = (TextView) findViewById(R.id.tv_search);
    }

    private void initData() {
        for (int i = 0; i < title.length; i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(title[i]));
        }
        startCityName = getIntent().getStringExtra("cityName");
        startPlaceName = getIntent().getStringExtra("address");
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
                    showProgressDialog();
                    // 处理搜索按钮响应
                    // 设置起终点信息，对于tranist search 来说，城市名无意义
                    PlanNode stNode = PlanNode.withCityNameAndPlaceName(startCityName, startPlaceName);
                    PlanNode enNode = PlanNode.withCityNameAndPlaceName(endCityName, endPlaceName);
                    switch (nowSearchType) {
                        case 0:
                            PlanNode stMassNode = PlanNode.withCityNameAndPlaceName("北京", "天安门");
                            PlanNode enMassNode = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");
                            mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stMassNode).to(enMassNode));
                            break;
                        case 1:
                            mSearch.drivingSearch((new DrivingRoutePlanOption())
                                    .from(stNode).to(enNode));
                            break;
                        case 2:
                            mSearch.transitSearch((new TransitRoutePlanOption())
                                    .from(stNode).city(startCityName).to(enNode));
                            break;
                        case 3:
                            mSearch.walkingSearch((new WalkingRoutePlanOption())
                                    .from(stNode).to(enNode));
                            break;
                        case 4:
                            mSearch.bikingSearch((new BikingRoutePlanOption())
                                    .from(stNode).to(enNode));
                            break;
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void closeProvinceDialog() {
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
        return true;
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                if (suggestAddrInfo.getSuggestStartCity() != null)
                    for (CityInfo start : suggestAddrInfo.getSuggestStartCity()) {
                        Log.d(TAG, "StartCity->city:" + start.city + " num:" + start.num);
                    }
                if (suggestAddrInfo.getSuggestEndCity() != null)
                    for (CityInfo end : suggestAddrInfo.getSuggestEndCity()) {
                        Log.d(TAG, "EndCity->city:" + end.city + " num:" + end.num);
                    }
                if (suggestAddrInfo.getSuggestStartNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestStartNode()) {
                        Log.d(TAG, "StartNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
                if (suggestAddrInfo.getSuggestEndNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestEndNode()) {
                        Log.d(TAG, "EndNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 0) {
//                nowResultwalk = result;

                RouteLineAdapter mRouteLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.WALKING_ROUTE);

                mListView.setAdapter(mRouteLineAdapter);
                showProgressDialog();
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
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                if (suggestAddrInfo.getSuggestStartCity() != null)
                    for (CityInfo start : suggestAddrInfo.getSuggestStartCity()) {
                        Log.d(TAG, "StartCity->city:" + start.city + " num:" + start.num);
                    }
                if (suggestAddrInfo.getSuggestEndCity() != null)
                    for (CityInfo end : suggestAddrInfo.getSuggestEndCity()) {
                        Log.d(TAG, "EndCity->city:" + end.city + " num:" + end.num);
                    }
                if (suggestAddrInfo.getSuggestStartNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestStartNode()) {
                        Log.d(TAG, "StartNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
                if (suggestAddrInfo.getSuggestEndNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestEndNode()) {
                        Log.d(TAG, "EndNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);


            if (result.getRouteLines().size() > 0) {
//                nowResultransit = result;

                RouteLineAdapter mRouteLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.TRANSIT_ROUTE);

                mListView.setAdapter(mRouteLineAdapter);
                showProgressDialog();

            } else {
                Log.d("route result", "结果数<0");
                return;
            }


        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点模糊，获取建议列表
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nowResultmass = result;
//
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 0) {
                // 列表选择
                RouteLineAdapter mRouteLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.MASS_TRANSIT_ROUTE);
//                nowResultmass = result;
                mListView.setAdapter(mRouteLineAdapter);
                showProgressDialog();
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
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                if (suggestAddrInfo.getSuggestStartCity() != null)
                    for (CityInfo start : suggestAddrInfo.getSuggestStartCity()) {
                        Log.d(TAG, "StartCity->city:" + start.city + " num:" + start.num);
                    }
                if (suggestAddrInfo.getSuggestEndCity() != null)
                    for (CityInfo end : suggestAddrInfo.getSuggestEndCity()) {
                        Log.d(TAG, "EndCity->city:" + end.city + " num:" + end.num);
                    }
                if (suggestAddrInfo.getSuggestStartNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestStartNode()) {
                        Log.d(TAG, "StartNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
                if (suggestAddrInfo.getSuggestEndNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestEndNode()) {
                        Log.d(TAG, "EndNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;


            if (result.getRouteLines().size() > 0) {
//                nowResultdrive = result;

                RouteLineAdapter mRouteLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.DRIVING_ROUTE);

                mListView.setAdapter(mRouteLineAdapter);
                showProgressDialog();

            }  else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActiviy.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
            if (null != suggestAddrInfo) {
                if (suggestAddrInfo.getSuggestStartCity() != null)
                    for (CityInfo start : suggestAddrInfo.getSuggestStartCity()) {
                        Log.d(TAG, "StartCity->city:" + start.city + " num:" + start.num);
                    }
                if (suggestAddrInfo.getSuggestEndCity() != null)
                    for (CityInfo end : suggestAddrInfo.getSuggestEndCity()) {
                        Log.d(TAG, "EndCity->city:" + end.city + " num:" + end.num);
                    }
                if (suggestAddrInfo.getSuggestStartNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestStartNode()) {
                        Log.d(TAG, "StartNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
                if (suggestAddrInfo.getSuggestEndNode() != null)
                    for (PoiInfo pi : suggestAddrInfo.getSuggestEndNode()) {
                        Log.d(TAG, "EndNode->address:" + pi.address + " city:" + pi.city + " name:" + pi.name + " phoneNum:" + pi.phoneNum + " postCode:" + pi.postCode);
                    }
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 0) {
//                nowResultbike = result;

                RouteLineAdapter mRouteLineAdapter = new RouteLineAdapter(RoutePlanActiviy.this,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.DRIVING_ROUTE);
                mListView.setAdapter(mRouteLineAdapter);
                showProgressDialog();

            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
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
                    }
                    break;
                case 1:
                    endCityName = data.getStringExtra("cityName");
                    endPlaceName = data.getStringExtra("address");
                    if ("我的位置".equals(endCityName)) {
                        mEndNodeTv.setText("我的位置");
                    } else {
                        mEndNodeTv.setText(endCityName + " " + endPlaceName);
                    }
                    break;
            }
        }
    }
}
