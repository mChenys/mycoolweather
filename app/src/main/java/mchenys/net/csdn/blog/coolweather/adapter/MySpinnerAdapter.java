package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;
import mchenys.net.csdn.blog.coolweather.db.City;
import mchenys.net.csdn.blog.coolweather.db.Province;

public class MySpinnerAdapter<T> extends BaseAdapter {
    private Context mContext;
    private List<T> mData;

    public MySpinnerAdapter(Context ctx, List<T> data) {
        this.mData = data;
        this.mContext = ctx;
    }

    @Override
    public int getCount() {
        return null == mData ? 0 : mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_spinner_list, null);
            holder.infoTv = (TextView) convertView.findViewById(R.id.tv_spinner_info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object obj = getItem(position);
        if (obj instanceof Province) {
            Province p = (Province) obj;
            holder.infoTv.setText(p.getProvinceName());
        } else if (obj instanceof City) {
            City c = (City) obj;
            holder.infoTv.setText(c.getCityName());
        }
        return convertView;
    }

    private static class ViewHolder {
        private TextView infoTv;
    }
}