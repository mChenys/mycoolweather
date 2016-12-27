package mchenys.net.csdn.blog.coolweather.gson;

import android.text.TextUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mChenys on 2016/12/27.
 */
public class AddressInfo {
    public String cityName;
    public String address;

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
                            list.add(new AddressInfo(arr[0], arr[1] + arr[2]));
                        }
                    }
                }
            }
        }
        list.add(0, new AddressInfo("我的位置", ""));
        return list;
    }
}
