package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.busline.BusLineResult;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;

/**
 * 公交站
 * Created by mChenys on 2016/12/30.
 */
public class BusStationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<BusLineResult.BusStation> mBusStations;
    private Drawable mBusStart, mBusEnd, mBusNormal;

    public BusStationAdapter(Context ctx, List<BusLineResult.BusStation> busStations) {
        this.mContext = ctx;
        this.mBusStations = busStations;
        mBusStart = mContext.getResources().getDrawable(R.drawable.ic_bus_begin);
        mBusNormal = mContext.getResources().getDrawable(R.drawable.ic_bus_normal);
        mBusEnd = mContext.getResources().getDrawable(R.drawable.ic_bus_end);
        mBusStart.setBounds( 0, 0, mBusStart.getMinimumWidth(),mBusStart.getMinimumHeight());
        mBusNormal.setBounds( 0, 0, mBusNormal.getMinimumWidth(),mBusNormal.getMinimumHeight());
        mBusEnd.setBounds( 0, 0, mBusEnd.getMinimumWidth(),mBusEnd.getMinimumHeight());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_station_list, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder h = (MyViewHolder) holder;
        h.stationTv.setText(mBusStations.get(position).getTitle());
        if (position == 0) {
            h.stationTv.setCompoundDrawables(mBusStart, null, null, null);
        } else if (position == getItemCount()-1) {
            h.stationTv.setCompoundDrawables(mBusEnd, null, null, null);
        } else {
            h.stationTv.setCompoundDrawables(mBusNormal, null, null, null);
        }
    }

    @Override
    public int getItemCount() {
        return null == mBusStations ? 0 : mBusStations.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView stationTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            stationTv = (TextView) itemView.findViewById(R.id.tv_station);
        }
    }
}
