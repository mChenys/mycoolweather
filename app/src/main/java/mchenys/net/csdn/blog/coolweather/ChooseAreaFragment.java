package mchenys.net.csdn.blog.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.db.City;
import mchenys.net.csdn.blog.coolweather.db.County;
import mchenys.net.csdn.blog.coolweather.db.Province;
import mchenys.net.csdn.blog.coolweather.util.HttpUtil;
import mchenys.net.csdn.blog.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mChenys on 2016/12/19.
 */
public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static int LEVEL_PROVINCE = 0;
    public static int LEVEL_CITY = 1;
    public static int LEVEL_COUNTY = 2;
    private ProgressDialog mProgressDialog;
    private TextView mTitleTv;
    private Button mBackBtn;
    private ListView mListView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> mProvinceList;//省列表
    private List<City> mCityList;//市列表
    private List<County> mCountyList;//现列表
    private Province mSelectedProvince;//选中的省
    private City mSelectedCity;//选中的市
    private int currentLevel; //当前选中的级别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleTv = (TextView) view.findViewById(R.id.tv_title);
        mBackBtn = (Button) view.findViewById(R.id.btn_back);
        mListView = (ListView) view.findViewById(R.id.lv);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = mProvinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    mSelectedCity = mCityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = mCountyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        //当前切换城市是在MainActivity中
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        //当前切换城市是在WeatherActivity的侧滑菜单中
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.setWeatherId(weatherId);
                        activity.closeDrawerLayout();
                    }

                }
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //查询所有省
    private void queryProvinces() {
        mTitleTv.setText("中国");
        mBackBtn.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province province : mProvinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            asyncQueryFromServer(address, "province");
        }
    }

    //查询选中省内的所有市
    private void queryCities() {
        mTitleTv.setText(mSelectedProvince.getProvinceName());
        mBackBtn.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid=?", String.valueOf(mSelectedProvince.getId())).find(City.class);
        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city : mCityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            asyncQueryFromServer(address, "city");
        }
    }

    //查询选中市内的所有县
    private void queryCounties() {
        mTitleTv.setText(mSelectedCity.getCityName());
        mBackBtn.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid=?", String.valueOf(mSelectedCity.getId())).find(County.class);
        if (mCountyList.size() > 0) {
            dataList.clear();
            for (County county : mCountyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            asyncQueryFromServer(address, "county");
        }
    }

    /**
     * 根据类型查找接口
     *
     * @param address
     * @param type
     */
    private void asyncQueryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProvinceDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (isHandleSuccess(responseText, type)) {
                    doQueryByType(type);
                }
            }
        });
    }

    private void doQueryByType(final String type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProvinceDialog();
                switch (type) {
                    case "province":
                        queryProvinces();
                        break;
                    case "city":
                        queryCities();
                        break;
                    case "county":
                        queryCounties();
                        break;
                }
            }
        });
    }

    private boolean isHandleSuccess(String responseText, String type) {
        boolean result = false;
        switch (type) {
            case "province":
                result = Utility.handleProvinceResponse(responseText);
                break;
            case "city":
                int provinceCode = mSelectedProvince == null ? this.provinceCode : mSelectedProvince.getId();
                result = Utility.handleCityResponse(responseText, provinceCode);
                break;
            case "county":
                int cityCode = mSelectedCity == null ? this.cityCode : mSelectedCity.getId();
                result = Utility.handleCountyResponse(responseText, cityCode);
                break;
        }
        return result;
    }

    private void closeProvinceDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private int provinceCode, cityCode;
    private String weatherId;
    private String provinceName, cityName, countyName;

    public void showWeatherByPosition(String province, String city, String county) {
        showProgressDialog();
        Log.d(TAG, "showWeatherByPosition->province:" + province + " city:" + city + " county:" + county);
        if (TextUtils.isEmpty(province) || TextUtils.isEmpty(city) || TextUtils.isEmpty(county)) {
            Toast.makeText(getActivity(), "定位信息获取失败", Toast.LENGTH_SHORT).show();
            closeProvinceDialog();
            return;
        }
        this.provinceName = province.replace("省", "");
        this.cityName = city.replace("市", "");
        this.countyName = county.replace("区", "").replace("县", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    findProvinceCode();
                    if (0 != provinceCode) {
                        findCityCode();
                    }
                    if (0 != cityCode) {
                        findWeatherId();
                    }

                    Log.d(TAG, "showWeatherByPosition->provinceCode:" + provinceCode + " cityCode:" + cityCode + " weatherId:" + weatherId);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(weatherId)) {
                                closeProvinceDialog();
                                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                                intent.putExtra("weather_id", weatherId);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "定位信息获取失败", Toast.LENGTH_SHORT).show();
                            closeProvinceDialog();
                        }
                    });
                }

            }
        }).start();


    }

    private void findWeatherId() {
        if (!TextUtils.isEmpty(countyName)) {
            List<County> countyList = DataSupport.select("weatherId").where("countyName=?", countyName).find(County.class);
            if (null != countyList && countyList.size() > 0) {
                weatherId = countyList.get(0).getWeatherId();
                return;
            }
            //进来说明找不到对应的county,那就用cityName查找,例如 广东/深圳/福田,而接口又没有福田,只能查上一级作为直接的县区了
            countyList = DataSupport.select("weatherId").where("countyName=?", cityName).find(County.class);
            if (null != countyList && countyList.size() > 0) {
                weatherId = countyList.get(0).getWeatherId();
                return;
            } else {
                String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
                executeQueryFromServer(address, "county");
            }
        }
    }

    private void findCityCode() {
        if (!TextUtils.isEmpty(cityName)) {
            List<City> cityList = DataSupport.select("cityCode").where("cityName=?", cityName).find(City.class);
            if (null != cityList && cityList.size() > 0) {
                cityCode = cityList.get(0).getCityCode();
            } else {
                String address = "http://guolin.tech/api/china/" + provinceCode;
                executeQueryFromServer(address, "city");
            }
        }
    }

    public void findProvinceCode() {
        if (!TextUtils.isEmpty(provinceName)) {
            List<Province> provinceList = DataSupport.select("provinceCode")
                    .where("provinceName =?", provinceName).find(Province.class);
            if (null != provinceList && provinceList.size() > 0) {
                provinceCode = provinceList.get(0).getProvinceCode();
            } else {
                String address = "http://guolin.tech/api/china";
                executeQueryFromServer(address, "province");
            }
        }
    }

    private void executeQueryFromServer(String address, String type) {
        String responseText = HttpUtil.sendOkHttpRequest(address);
        if (isHandleSuccess(responseText, type)) {
            switch (type) {
                case "province":
                    findProvinceCode();
                    break;
                case "city":
                    findCityCode();
                    break;
                case "county":
                    findWeatherId();
                    break;
            }
        }
    }
}
