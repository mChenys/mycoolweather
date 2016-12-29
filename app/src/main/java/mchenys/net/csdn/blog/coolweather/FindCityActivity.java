package mchenys.net.csdn.blog.coolweather;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import mchenys.net.csdn.blog.coolweather.adapter.AddressAdapter;
import mchenys.net.csdn.blog.coolweather.db.AddressInfo;
import mchenys.net.csdn.blog.coolweather.util.HttpUtil;
import mchenys.net.csdn.blog.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mChenys on 2016/12/27.
 */
public class FindCityActivity extends AppCompatActivity {
    private static final String TAG = "FindCityActivity";
    private TextView mBackTv, mOkBtn;
    private EditText mInputEdt;
    private ListView mResultAddressLv, mHistoryAddressLv;
    private List<AddressInfo> mSearchResult = new ArrayList<>();
    private List<AddressInfo> mSearchHistory = new ArrayList<>();
    private AddressAdapter mResultAdapter;
    private AddressAdapter mHistoryAdapter;
    private boolean isHistoryShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_city);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        mBackTv = (TextView) findViewById(R.id.tv_back);
        mOkBtn = (TextView) findViewById(R.id.tv_ok);
        mInputEdt = (EditText) findViewById(R.id.edt_input);
        mResultAddressLv = (ListView) findViewById(R.id.lv_result);
        mHistoryAddressLv = (ListView) findViewById(R.id.lv_history_result);
        mResultAdapter = new AddressAdapter(this, mSearchResult,0);
        mHistoryAdapter = new AddressAdapter(this, mSearchHistory,1);
        mResultAddressLv.setAdapter(mResultAdapter);
        mHistoryAddressLv.setAdapter(mHistoryAdapter);
        changeContentShow(true);
    }

    private void changeContentShow(boolean isHistoryShow) {
        this.isHistoryShow = isHistoryShow;
        mHistoryAddressLv.setVisibility(isHistoryShow ? View.VISIBLE : View.GONE);
        mResultAddressLv.setVisibility(isHistoryShow ? View.GONE : View.VISIBLE);
    }

    private void initData() {
        mSearchHistory.clear();
        mSearchHistory.addAll(AddressInfo.getHistoryList());
        mHistoryAdapter.notifyDataSetChanged();
    }

    private void initListener() {
        mBackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputTxt = mInputEdt.getText().toString().trim();
                if (TextUtils.isEmpty(inputTxt)) {
                    Toast.makeText(FindCityActivity.this, "搜索地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                AddressInfo info = new AddressInfo(inputTxt, inputTxt);
                info.save();
                Intent intent = getIntent();
                intent.putExtra("cityName", inputTxt);
                intent.putExtra("address", inputTxt);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mResultAddressLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddressInfo info = mSearchResult.get(position);
                Intent intent = getIntent();
                intent.putExtra("cityName", info.getCityName());
                intent.putExtra("address", info.getAddress());
                info.save();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mHistoryAddressLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddressInfo info = mSearchHistory.get(position);
                Intent intent = getIntent();
                intent.putExtra("cityName", info.getCityName());
                intent.putExtra("address", info.getAddress());
                info.save();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mInputEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String search = s.toString().trim();
                if (TextUtils.isEmpty(search)) {
                    mResultAddressLv.setVisibility(View.GONE);
                } else {
                    mResultAddressLv.setVisibility(View.VISIBLE);
                    loadData(search);
                }
            }
        });

        mInputEdt.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = mInputEdt.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > mInputEdt.getWidth() - mInputEdt.getPaddingRight() - drawable.getIntrinsicWidth()) {
                    mInputEdt.setText("");
                }
                return false;
            }
        });
    }

    private void loadData(String keyWord) {
        try {
            keyWord = URLEncoder.encode(keyWord, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String url = "http://map.baidu.com/su?wd=" + keyWord + "&cid=0&type=0";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final List<AddressInfo> data = Utility.handleAddressSearchResponse(response.body().string());
                if (data != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSearchResult.clear();
                            mSearchResult.addAll(data);
                            Log.d(TAG, "mSearchResult->" + mSearchResult.toString());
                            mResultAdapter.notifyDataSetChanged();
                            changeContentShow(false);
                        }
                    });
                }
            }
        });
    }


}
