package mchenys.net.csdn.blog.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.adapter.BusStationAdapter;
import mchenys.net.csdn.blog.coolweather.adapter.MyItemDecoration;
import mchenys.net.csdn.blog.coolweather.fragment.BusLineSearchFragment;
import mchenys.net.csdn.blog.coolweather.fragment.HaveFunSearchFragment;

/**
 * 公交/周边搜索
 * Created by mChenys on 2016/12/29.
 */
public class PoiSearchActivity extends AppCompatActivity {
    private TextView mSearchTv;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private LinearLayout mRightSettingLayout;
    private ImageView mSwitchIv;
    private TextView mNextLineTv;
    private TextView mShowMapTv;
    private List<BusLineResult.BusStation> mBusStations = new ArrayList<>();
    private BusStationAdapter mStationAdapter;
    private FrameLayout mContentFl;
    private RadioGroup mPoiSwitchRg;
    public static final int TYPE_BUS_LINE = 0;
    public static final int TYPE_HAVE_FUN = 1;
    private int mType;
    private List<Fragment> mFragmentList = new ArrayList<>();
    // 搜索相关
    private PoiSearch mPoiSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private BusLineSearch mBusLineSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private BusLineResult mBusLineResult = null;
    private List<String> busLineIDList = new ArrayList<>();//公交站id
    private List<String> suggest;//poi搜索建议
    private int busLineIndex; //公车线路个数,可能有相同的路线名称

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_search);
        initSearch();
        initView();
        initListener();
        switchPoiSearch(TYPE_BUS_LINE);
    }


    private void initView() {
        mSearchTv = (TextView) findViewById(R.id.btn_search);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyItemDecoration());
        mRecyclerView.setAdapter(mStationAdapter = new BusStationAdapter(this, mBusStations));

        mRightSettingLayout = (LinearLayout) findViewById(R.id.ll_right_setting);
        mRightSettingLayout.setVisibility(View.GONE);
        mSwitchIv = (ImageView) findViewById(R.id.iv_switch_bus);
        mNextLineTv = (TextView) findViewById(R.id.tv_next);
        mShowMapTv = (TextView) findViewById(R.id.tv_show_map);
        mContentFl = (FrameLayout) findViewById(R.id.fl_content);
        mPoiSwitchRg = (RadioGroup) findViewById(R.id.rg_switch_poi);

        mFragmentList.add(new BusLineSearchFragment());
        mFragmentList.add(new HaveFunSearchFragment());
    }

    private void initSearch() {
        mPoiSearch = PoiSearch.newInstance();
        mBusLineSearch = BusLineSearch.newInstance();

        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        mBusLineSearch.setOnGetBusLineSearchResultListener(busListener);
        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestListener);
    }


    private void initListener() {

        findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPoiSwitchRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_bus_line:
                        mType = TYPE_BUS_LINE;
                        break;
                    case R.id.rb_have_fun:
                        mType = TYPE_HAVE_FUN;
                        break;
                }
                switchPoiSearch(mType);
            }
        });

        mSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBusStations.isEmpty()) {
                    mRightSettingLayout.setVisibility(View.GONE);
                }
                if (mType == TYPE_BUS_LINE) {
                    String busLine = getBusSearchFragment().getBusLine();
                    if (TextUtils.isEmpty(busLine)) {
                        Toast.makeText(PoiSearchActivity.this, "请输入线路名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String cityName = getBusSearchFragment().getCityName();
                    showProgressDialog();
                    // 发起poi检索，从得到所有poi中找到公交线路类型的poi，再使用该poi的uid进行公交详情搜索
                    mPoiSearch.searchInCity(new PoiCitySearchOption().city(cityName).keyword(busLine));
                } else if (mType == TYPE_HAVE_FUN) {
                    String keystr = getFunSearchFragment().getKeyword();
                    if (TextUtils.isEmpty(keystr)) {
                        Toast.makeText(PoiSearchActivity.this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mPoiSearch.searchInCity((new PoiCitySearchOption()).city("").keyword(keystr).pageNum(0));
                }

            }
        });

        mSwitchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.reverse(mBusStations);
                mStationAdapter.notifyDataSetChanged();
            }
        });
        mNextLineTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                searchBusLine();
            }
        });
        mShowMapTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("type", mType);
                intent.putExtra("busLineResult", mBusLineResult);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    private void switchPoiSearch(int type) {
        Fragment fragment = mFragmentList.get(type);
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.fl_content, fragment).
                commitAllowingStateLoss();
        if (type == TYPE_BUS_LINE) {
            mPoiSwitchRg.check(R.id.rb_bus_line);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else if (type == TYPE_HAVE_FUN) {
            mPoiSwitchRg.check(R.id.rb_have_fun);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {

        public void onGetPoiResult(PoiResult result) {
            //获取POI检索结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                closeProgressDialog();
                Toast.makeText(PoiSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            if (mType == TYPE_BUS_LINE) {
                // 遍历所有poi，找到类型为公交线路的poi
                busLineIDList.clear();
                for (PoiInfo poi : result.getAllPoi()) {
                    if (poi.type == PoiInfo.POITYPE.BUS_LINE || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                        busLineIDList.add(poi.uid);
                    }
                }
                //公交查询
                if (mType == TYPE_BUS_LINE) {
                    searchBusLine();
                }
            } else if (mType == TYPE_HAVE_FUN) {
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    Intent intent = getIntent();
                    intent.putExtra("type", mType);
                    intent.putExtra("poiResult", result);
                    setResult(RESULT_OK, intent);
                    return;
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
                    // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                    String strInfo = "在";
                    for (CityInfo cityInfo : result.getSuggestCityList()) {
                        strInfo += cityInfo.city;
                        strInfo += ",";
                    }
                    strInfo += "找到结果";
                    Toast.makeText(PoiSearchActivity.this, strInfo, Toast.LENGTH_LONG).show();
                }
            }
        }

        public void onGetPoiDetailResult(PoiDetailResult result) {
            if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(PoiSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PoiSearchActivity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    };

    //搜索公交
    private void searchBusLine() {
        BusLineSearchFragment fragment = (BusLineSearchFragment) mFragmentList.get(mType);
        String cityName = fragment.getCityName();
        if (busLineIndex >= busLineIDList.size()) {
            busLineIndex = 0;
        }
        if (busLineIndex >= 0 && busLineIndex < busLineIDList.size() && busLineIDList.size() > 0) {
            mBusLineSearch.searchBusLine((new BusLineSearchOption().city(cityName).uid(busLineIDList.get(busLineIndex))));
            busLineIndex++;
        }
    }

    //公交查询结果
    private OnGetBusLineSearchResultListener busListener = new OnGetBusLineSearchResultListener() {

        @Override
        public void onGetBusLineResult(BusLineResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                closeProgressDialog();
                Toast.makeText(PoiSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            mBusLineResult = result;
            mBusStations.clear();
            mBusStations.addAll(result.getStations());
            mStationAdapter.notifyDataSetChanged();
            closeProgressDialog();
            if (result.getStations().size() > 0) {
                mRightSettingLayout.setVisibility(View.VISIBLE);
            }
        }
    };

    //搜索建议
    public void requestSuggestion(String key) {
        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(key).city(""));
    }

    //建议查询结果
    private OnGetSuggestionResultListener suggestListener = new OnGetSuggestionResultListener() {
        /**
         * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
         *
         * @param res
         */
        @Override
        public void onGetSuggestionResult(SuggestionResult res) {
            if (res == null || res.getAllSuggestions() == null) {
                return;
            }
            suggest = new ArrayList<String>();
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                if (info.key != null) {
                    suggest.add(info.key);
                }
            }
            if (suggest.size() > 0) {
                getFunSearchFragment().showSuggestList(suggest);
            }
        }
    };

    public HaveFunSearchFragment getFunSearchFragment() {
        return (HaveFunSearchFragment) mFragmentList.get(1);
    }

    public BusLineSearchFragment getBusSearchFragment() {
       return  (BusLineSearchFragment) mFragmentList.get(0);
    }

    public void closeProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    public void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        mBusLineSearch.destroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }


}
