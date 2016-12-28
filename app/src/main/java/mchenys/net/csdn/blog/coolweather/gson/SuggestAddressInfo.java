package mchenys.net.csdn.blog.coolweather.gson;

import android.util.Log;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.route.SuggestAddrInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 建议地址列表
 * Created by mChenys on 2016/12/28.
 */
public class SuggestAddressInfo {
    private static final String TAG = "SuggestAddressInfo";

    public int state;//0:startNode开始,1:startNode,2:endNode开始,3endNode

    public PoiInfo pi;//baidu建议地址

    public static List<SuggestAddressInfo> startNodeList = new ArrayList<>();
    public static List<SuggestAddressInfo> endNodeList = new ArrayList<>();

    public SuggestAddressInfo() {
    }

    public SuggestAddressInfo(int state, PoiInfo pi) {
        this.state = state;
        this.pi = pi;
    }

    public static List<SuggestAddressInfo> parseList(SuggestAddrInfo suggestAddrInfo) {
        List<SuggestAddressInfo> list = new ArrayList<>();
        List<SuggestAddressInfo> temp = new ArrayList<>();
        startNodeList.clear();
        endNodeList.clear();
        if (null != suggestAddrInfo) {
            if (suggestAddrInfo.getSuggestStartNode() != null) {
                temp.add(new SuggestAddressInfo(0, null));
                for (PoiInfo pi : suggestAddrInfo.getSuggestStartNode()) {
                    temp.add(new SuggestAddressInfo(1, pi));
                }
                startNodeList.addAll(temp);
            }
            if (suggestAddrInfo.getSuggestEndNode() != null) {
                temp.clear();
                temp.add(new SuggestAddressInfo(2, null));
                for (PoiInfo pi : suggestAddrInfo.getSuggestEndNode()) {
                    temp.add(new SuggestAddressInfo(3, pi));
                }
                endNodeList.addAll(temp);
            }
            Log.d(TAG, list.toString());
        }
        list.addAll(startNodeList);
        list.addAll(endNodeList);

        return list;
    }

    @Override
    public String toString() {
        return "SuggestAddressInfo{" +
                "state=" + state +
                ", address=" + pi.address + ",city=" + pi.city + ",name=" + pi.name +
                '}';
    }
}
