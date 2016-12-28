package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;
import mchenys.net.csdn.blog.coolweather.db.AddressInfo;

public class AddressAdapter extends BaseAdapter {
    private List<AddressInfo> mAddressInfos;
    private Context mContext;

    public AddressAdapter(Context ctx, List<AddressInfo> infos) {
        this.mContext = ctx;
        this.mAddressInfos = infos;
    }

    @Override
    public int getCount() {
        return null == mAddressInfos ? 0 : mAddressInfos.size();
    }

    @Override
    public AddressInfo getItem(int position) {
        return null == mAddressInfos ? null : mAddressInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_search_list, null);
            holder = new ViewHolder();
            holder.cityTv = (TextView) convertView.findViewById(R.id.tv_city);
            holder.addressTv = (TextView) convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AddressInfo info = getItem(position);
        holder.addressTv.setVisibility(TextUtils.isEmpty(info.getAddress()) ? View.GONE : View.VISIBLE);
        holder.addressTv.setText(info.getAddress());
        holder.cityTv.setText(info.getCityName());
        return convertView;
    }

    private class ViewHolder {
        private TextView cityTv;
        private TextView addressTv;
    }
}