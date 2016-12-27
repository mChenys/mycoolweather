package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;

/**
 * Created by mChenys on 2016/12/27.
 */
public class RouteLineSuggestAdapter extends BaseAdapter {
    private final List<PoiInfo> poiInfos;
    private LayoutInflater layoutInflater;

    public RouteLineSuggestAdapter(Context ctx, List<PoiInfo> poiInfos) {
        this.poiInfos = poiInfos;
        layoutInflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return null == poiInfos ? 0 : poiInfos.size();
    }

    @Override
    public PoiInfo getItem(int position) {
        return null == poiInfos ? null : poiInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_suggest_list, null);
            holder = new ViewHolder();
            holder.cityNameTv = (TextView) convertView.findViewById(R.id.tv_city_name);
            holder.addressTv = (TextView) convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PoiInfo pi = getItem(position);
        if (null == pi.city) pi.city = "";
        if (null == pi.name) pi.name = "";
        if (null == pi.address) pi.address = "";
        holder.cityNameTv.setText(pi.city + " " + pi.name);
        holder.addressTv.setText(pi.address);
        return convertView;
    }

    private class ViewHolder {
        private TextView cityNameTv;
        private TextView addressTv;
    }
}
