package mchenys.net.csdn.blog.coolweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.RouteStep;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.R;

/**
 * Created by mChenys on 2016/12/28.
 */
public class RouteStepAdapter extends BaseAdapter {
    private List<? extends RouteStep> mRouteSteps;
    private LayoutInflater layoutInflater;

    public RouteStepAdapter(Context ctx, List<RouteStep> steps) {
        layoutInflater = LayoutInflater.from(ctx);
        mRouteSteps = steps;
    }

    @Override
    public int getCount() {
        return null == mRouteSteps ? 0 : mRouteSteps.size();
    }

    @Override
    public RouteStep getItem(int position) {
        return null == mRouteSteps ? null : mRouteSteps.get(position);
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
            convertView = layoutInflater.inflate(R.layout.item_route_step_list, null);
            holder.infoTv = (TextView) convertView.findViewById(R.id.tv_step_info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RouteStep step = getItem(position);
        String nodeTitle = "";
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        } else if (step instanceof BikingRouteLine.BikingStep) {
            nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
        } else if (step instanceof MassTransitRouteLine.TransitStep) {
            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
        }
        holder.infoTv.setText((position + 1) + ": " + nodeTitle.trim());
        return convertView;
    }

    private static class ViewHolder {
        private TextView infoTv;
    }
}
