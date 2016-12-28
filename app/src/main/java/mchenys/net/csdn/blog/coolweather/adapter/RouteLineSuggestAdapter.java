package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;
import mchenys.net.csdn.blog.coolweather.gson.SuggestAddressInfo;

/**
 * Created by mChenys on 2016/12/27.
 */
public class RouteLineSuggestAdapter extends BaseAdapter {
    private List<SuggestAddressInfo> suggestAddresses;
    private LayoutInflater layoutInflater;

    public RouteLineSuggestAdapter(Context ctx, List<SuggestAddressInfo> suggestAddressInfos) {
        this.suggestAddresses = suggestAddressInfos;
        layoutInflater = LayoutInflater.from(ctx);
    }

    public void resetData(List<SuggestAddressInfo> sais) {
        this.suggestAddresses = sais;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return null == suggestAddresses ? 0 : suggestAddresses.size();
    }

    @Override
    public SuggestAddressInfo getItem(int position) {
        return null == suggestAddresses ? null : suggestAddresses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).state;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case 0:
                case 2:
                    convertView = layoutInflater.inflate(R.layout.item_suggest_info, null);
                    holder = new ViewHolder();
                    holder.suggestTv = (TextView) convertView.findViewById(R.id.tv_suggest_info);
                    convertView.setTag(holder);
                    break;
                case 1:
                case 3:
                    convertView = layoutInflater.inflate(R.layout.item_suggest_list, null);
                    holder = new ViewHolder();
                    holder.cityNameTv = (TextView) convertView.findViewById(R.id.tv_city_name);
                    holder.addressTv = (TextView) convertView.findViewById(R.id.tv_address);
                    convertView.setTag(holder);
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SuggestAddressInfo sai = getItem(position);
        if (sai.state == 0) {
            holder.suggestTv.setText("选择一个作为起点:");
        } else if (sai.state == 2) {
            holder.suggestTv.setText("选择一个作为终点:");
        } else {
            if(sai.pi.city ==null) sai.pi.city = "";
            if(sai.pi.name ==null) sai.pi.name = "";
            if(sai.pi.address==null) sai.pi.address = "";
            holder.cityNameTv.setText(sai.pi.city + " " + sai.pi.name);
            holder.addressTv.setText(sai.pi.address);
        }
        return convertView;
    }

    private class ViewHolder {
        private TextView cityNameTv;
        private TextView addressTv;
        private TextView suggestTv;
    }
}
