package mchenys.net.csdn.blog.coolweather.mapapi.overlayutil;

import android.graphics.Color;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.RouteStep;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示一条公交详情结果的Overlay
 */
public class BusLineOverlay extends OverlayManager {

    private BusLineResult mBusLineResult = null;

    /**
     * 构造函数
     *
     * @param baiduMap 该BusLineOverlay所引用的 BaiduMap 对象
     */
    public BusLineOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置公交线数据
     *
     * @param result 公交线路结果数据
     */
    public void setData(BusLineResult result) {
        this.mBusLineResult = result;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {

        if (mBusLineResult == null || mBusLineResult.getStations() == null) {
            return null;
        }
        List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();

        for (Object obj : mBusLineResult.getStations()) {
            LatLng latLng = null;
            if (obj instanceof BusLineResult.BusStation) {
                BusLineResult.BusStation station = (BusLineResult.BusStation) obj;
                latLng = station.getLocation();

            } else if (obj instanceof RouteNode) {
                RouteNode routeNode = (RouteNode) obj;
                latLng = routeNode.getLocation();
            }
            if (null != latLng) {
                overlayOptionses.add(new MarkerOptions()
                        .position(latLng)
                        .zIndex(10)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory
                                .fromAssetWithDpi("Icon_bus_station.png")));
            }

        }

        List<LatLng> points = new ArrayList<LatLng>();
        for (Object obj : mBusLineResult.getSteps()) {
            List<LatLng> latLngs = null;
            if (obj instanceof BusLineResult.BusStep) {
                BusLineResult.BusStep step = (BusLineResult.BusStep) obj;
                latLngs = step.getWayPoints();
            } else if (obj instanceof RouteStep) {
                RouteStep step = (RouteStep) obj;
                latLngs = step.getWayPoints();
            }
            if (latLngs != null) {
                points.addAll(latLngs);
            }
        }
        if (points.size() > 0) {
            overlayOptionses
                    .add(new PolylineOptions().width(10)
                            .color(Color.argb(178, 0, 78, 255)).zIndex(0)
                            .points(points));
        }
        return overlayOptionses;
    }

    /**
     * 覆写此方法以改变默认点击行为
     *
     * @param index 被点击的站点在
     *              {@link com.baidu.mapapi.search.busline.BusLineResult#getStations()}
     *              中的索引
     * @return 是否处理了该点击事件
     */
    public boolean onBusStationClick(int index) {
        if (mBusLineResult.getStations() != null
                && mBusLineResult.getStations().get(index) != null) {
            Object obj = mBusLineResult.getStations();
            String title = "";
            if (obj instanceof BusLineResult.BusStation) {
                BusLineResult.BusStation station = (BusLineResult.BusStation) obj;
                title = station.getTitle();
            } else if (obj instanceof RouteNode) {
                RouteNode routeNode = (RouteNode) obj;
                title = routeNode.getTitle();
            }
            Log.i("baidumapsdk", "BusLineOverlay onBusStationClick title:"+title);
        }
        return false;
    }

    public final boolean onMarkerClick(Marker marker) {
        if (mOverlayList != null && mOverlayList.contains(marker)) {
            return onBusStationClick(mOverlayList.indexOf(marker));
        } else {
            return false;
        }

    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        // TODO Auto-generated method stub
        return false;
    }
}
