package mchenys.net.csdn.blog.coolweather.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.PoiSearchActivity;
import mchenys.net.csdn.blog.coolweather.R;
import mchenys.net.csdn.blog.coolweather.adapter.MySpinnerAdapter;
import mchenys.net.csdn.blog.coolweather.db.City;
import mchenys.net.csdn.blog.coolweather.db.Province;
import mchenys.net.csdn.blog.coolweather.util.HttpUtil;
import mchenys.net.csdn.blog.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mChenys on 2016/12/30.
 */
public class BusLineSearchFragment extends Fragment {
    private Spinner mProvinceSpinner, mCitySpinner;
    private EditText mBusLineEdt;
    private Province mSelectedProvince;
    private List<Province> mProvinceList = new ArrayList<>();
    private List<City> mCityList = new ArrayList<>();
    private MySpinnerAdapter mProvinceAdapter, mCityAdapter;
    private PoiSearchActivity mParent;
    private String mCityName;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_busline_search_header, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mParent = (PoiSearchActivity) getActivity();
        initView(view);
        initData();
        initListener();
    }

    private void initView(View view) {
        mProvinceSpinner = (Spinner) view.findViewById(R.id.sp_province);
        mCitySpinner = (Spinner) view.findViewById(R.id.sp_city);
        mBusLineEdt = (EditText) view.findViewById(R.id.edt_bus_line);
        mProvinceAdapter = new MySpinnerAdapter<>(getActivity(), mProvinceList);
        mCityAdapter = new MySpinnerAdapter<>(getActivity(), mCityList);
        mProvinceSpinner.setAdapter(mProvinceAdapter);
        mCitySpinner.setAdapter(mCityAdapter);
    }


    private void initData() {
        queryProvinces();
    }

    private void initListener() {
        mProvinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (null != view) {
                    TextView tv = (TextView) view;
                    tv.setTextColor(Color.WHITE);
                    tv.setGravity(Gravity.CENTER);
                }
                mSelectedProvince = (Province) parent.getItemAtPosition(position);
                queryCities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (null != view) {
                    TextView tv = (TextView) view;
                    tv.setTextColor(Color.WHITE);
                    tv.setGravity(Gravity.CENTER);
                }
                mCityName = mCityList.get(position).getCityName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    //查询所有省
    private void queryProvinces() {
        List<Province> temp = DataSupport.findAll(Province.class);
        if (temp.size() > 0) {
            mProvinceList.clear();
            mProvinceList.addAll(temp);
            mProvinceAdapter.notifyDataSetChanged();
            mProvinceSpinner.setSelection(0);
            mSelectedProvince = mProvinceList.get(0);
            mParent.closeProgressDialog();
        } else {
            String address = "http://guolin.tech/api/china";
            asyncQueryFromServer(address, "province");
        }
    }

    //查询选中省内的所有市
    private void queryCities() {
        List<City> temp = DataSupport.where("provinceid=?", String.valueOf(mSelectedProvince.getId())).find(City.class);
        if (temp.size() > 0) {
            mCityList.clear();
            mCityList.addAll(temp);
            mCityAdapter.notifyDataSetChanged();
            mCitySpinner.setSelection(0);
            mCityName = mCityList.get(0).getCityName();
            mParent.closeProgressDialog();
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            asyncQueryFromServer(address, "city");
        }
    }

    /**
     * 根据类型查找接口
     *
     * @param address
     * @param type
     */
    private void asyncQueryFromServer(String address, final String type) {
        mParent.showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mParent.closeProgressDialog();
                        Toast.makeText(mParent, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseText = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isHandleSuccess(responseText, type)) {
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            }
                        }
                    }
                });

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
                result = Utility.handleCityResponse(responseText, mSelectedProvince.getId());
                break;
        }
        return result;
    }

    public String getCityName() {
        return mCityName;
    }

    public String getBusLine() {
        return mBusLineEdt.getText().toString().trim();
    }
}
