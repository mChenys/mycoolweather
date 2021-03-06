package mchenys.net.csdn.blog.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import mchenys.net.csdn.blog.coolweather.db.City;
import mchenys.net.csdn.blog.coolweather.db.County;
import mchenys.net.csdn.blog.coolweather.db.Province;
import mchenys.net.csdn.blog.coolweather.db.AddressInfo;
import mchenys.net.csdn.blog.coolweather.gson.Weather;

/**
 * Created by mChenys on 2016/12/16.
 */
public class Utility {
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.optJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.optString("name"));
                    province.setProvinceCode(provinceObject.optInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCitys = new JSONArray(response);
                for (int i = 0; i < allCitys.length(); i++) {
                    JSONObject cityObject = allCitys.optJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.optString("name"));
                    city.setCityCode(cityObject.optInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.optJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.optString("name"));
                    county.setWeatherId(countyObject.optString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.optJSONArray("HeWeather");
            String weatherContent = jsonArray.optJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<AddressInfo> handleAddressSearchResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (null != jsonObject) {
                JSONArray array = jsonObject.optJSONArray("s");
                return AddressInfo.parseList(array);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
