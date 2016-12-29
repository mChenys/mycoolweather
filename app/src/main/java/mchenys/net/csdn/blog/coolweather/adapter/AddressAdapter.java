package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;
import mchenys.net.csdn.blog.coolweather.db.AddressInfo;

public class AddressAdapter extends BaseAdapter {
    private List<AddressInfo> mAddressInfos;
    private Context mContext;
    private int type;//0:表示在线搜索,1:表示历史搜索

    public AddressAdapter(Context ctx, List<AddressInfo> infos, int type) {
        this.mContext = ctx;
        this.mAddressInfos = infos;
        this.type = type;
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
            holder.clearLl = (LinearLayout) convertView.findViewById(R.id.ll_clear);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AddressInfo info = getItem(position);
        holder.addressTv.setVisibility("我的位置".equals(info.getCityName()) ? View.GONE : View.VISIBLE);
        holder.addressTv.setText(info.getAddress());
        holder.cityTv.setText(info.getCityName());
        boolean isShowClear = type == 1 && position == getCount() - 1;
        holder.clearLl.setVisibility(isShowClear ? View.VISIBLE : View.GONE);
        holder.clearLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSupport.deleteAll(AddressInfo.class);
                mAddressInfos.clear();
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private TextView cityTv;
        private TextView addressTv;
        private LinearLayout clearLl;
    }
}