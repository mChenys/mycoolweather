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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.PoiSearchActivity;
import mchenys.net.csdn.blog.coolweather.R;

/**
 * Created by mChenys on 2016/12/30.
 */
public class HaveFunSearchFragment extends Fragment {
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private PoiSearchActivity mParent;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_have_fun_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mParent = (PoiSearchActivity) getActivity();
        keyWorldsView = (AutoCompleteTextView) view.findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
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
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
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

    public void showSuggestList(List<String> list) {
        sugAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, list);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }
}
