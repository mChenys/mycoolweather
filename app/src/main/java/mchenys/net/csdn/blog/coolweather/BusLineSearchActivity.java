package mchenys.net.csdn.blog.coolweather;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.adapter.MySpinnerAdapter;
import mchenys.net.csdn.blog.coolweather.db.City;
import mchenys.net.csdn.blog.coolweather.db.Province;
import mchenys.net.csdn.blog.coolweather.util.HttpUtil;
import mchenys.net.csdn.blog.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mChenys on 2016/12/29.
 */
public class BusLineSearchActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Spinner mProvinceSpinner, mCitySpinner;
    private EditText mBusLineEdt;
    private Button mSearchBtn;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private Province mSelectedProvince;
    private List<Province> mProvinceList = new ArrayList<>();
    private List<City> mCityList = new ArrayList<>();
    private MySpinnerAdapter mProvinceAdapter, mCityAdapter;
    private String mCityName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_search);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mProvinceSpinner = (Spinner) findViewById(R.id.sp_province);
        mCitySpinner = (Spinner) findViewById(R.id.sp_city);
        mBusLineEdt = (EditText) findViewById(R.id.edt_bus_line);
        mSearchBtn = (Button) findViewById(R.id.btn_search);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        mProvinceAdapter = new MySpinnerAdapter<>(this, mProvinceList);
        mCityAdapter = new MySpinnerAdapter<>(this, mCityList);
        mProvinceSpinner.setAdapter(mProvinceAdapter);
        mCitySpinner.setAdapter(mCityAdapter);
    }


    private void initData() {
        queryProvinces();
    }

    private void initListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mProvinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
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
                TextView tv = (TextView) view;
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
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
            closeProvinceDialog();
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
            closeProvinceDialog();
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
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProvinceDialog();
                        Toast.makeText(BusLineSearchActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
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

    private void closeProvinceDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }


}
