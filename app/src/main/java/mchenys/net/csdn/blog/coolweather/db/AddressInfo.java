package mchenys.net.csdn.blog.coolweather.db;

import android.text.TextUtils;

import org.json.JSONArray;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mChenys on 2016/12/27.
 */
public class AddressInfo extends DataSupport {
    private int id;
    private String cityName;
    private String address;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "AddressInfo{" +
                "cityName='" + cityName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    public AddressInfo() {
    }

    public AddressInfo(String cityName, String address) {
        this.cityName = cityName;
        this.address = address;
    }

    public static List<AddressInfo> parseList(JSONArray array) {
        List<AddressInfo> list = new ArrayList<>();
        if (null != array) {
            for (int i = 0; i < array.length(); i++) {
                String str = array.optString(i);
                if (!TextUtils.isEmpty(str)) {
                    if (str.contains("$")) {
                        String[] arr = str.split("\\$+");
                        if (arr.length >= 3) {
                            AddressInfo addressInfo = new AddressInfo();
                            addressInfo.setCityName(arr[0]);
                            addressInfo.setAddress(arr[1] + arr[2]);
                            list.add(addressInfo);
                        }
                    }
                }
            }
        }
        list.add(0, new AddressInfo("我的位置", ""));
        return list;
    }

    public static List<AddressInfo> getHistoryList() {
        List<AddressInfo> list = new ArrayList<>();
        list.addAll(DataSupport.findAll(AddressInfo.class));
        return list;
    }
}
