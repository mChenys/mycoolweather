package mchenys.net.csdn.blog.coolweather.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.PoiSearchActivity;
import mchenys.net.csdn.blog.coolweather.R;

/**
 * Created by mChenys on 2016/12/30.
 */
public class HaveFunSearchFragment extends Fragment {
    private AutoCompleteTextView keyWorldsView = null;
    private PoiSearchActivity mParent;
    private ListView mCityNameLv;
    private TextView mCityListTv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_have_fun_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mParent = (PoiSearchActivity) getActivity();
        keyWorldsView = (AutoCompleteTextView) view.findViewById(R.id.searchkey);
        mCityNameLv = (ListView) view.findViewById(R.id.lv_city);
        mCityListTv = (TextView) view.findViewById(R.id.tv_city_list);
        mCityListTv.setVisibility(View.GONE);
        keyWorldsView.setThreshold(1);

        keyWorldsView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                mCityListTv.setVisibility(View.GONE);
                //使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                mParent.requestSuggestion(cs.toString());
            }
        });

        keyWorldsView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = keyWorldsView.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > keyWorldsView.getWidth() - keyWorldsView.getPaddingRight() - drawable.getIntrinsicWidth()) {
                    keyWorldsView.setText("");
                }
                return false;
            }
        });
    }

    public String getKeyword() {
        return keyWorldsView.getText().toString().trim();
    }

    /**
     * 显示建议信息
     *
     * @param list
     */
    public void showSuggestList(List<String> list) {
        ArrayAdapter sugAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, list);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    /**
     * 显示匹配结果的城市名称
     *
     * @param cityList
     */
    public void showFindedCityList(List<String> cityList) {
        if (null != cityList && cityList.size() > 0) {
            mCityListTv.setVisibility(View.VISIBLE);
            ArrayAdapter cityAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, cityList);
            mCityNameLv.setAdapter(cityAdapter);
            mCityNameLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String cityName = (String) parent.getItemAtPosition(position);
                    mParent.searchPoiByCityName(cityName);
                }
            });
        }
    }

}
