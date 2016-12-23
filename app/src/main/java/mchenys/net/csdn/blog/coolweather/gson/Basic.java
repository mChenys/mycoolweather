package mchenys.net.csdn.blog.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 城市信息
 * 由于接口中的字段不适用用来作为bean的字段,所以用 @SerializedName来做映射
 * Created by mChenys on 2016/12/23.
 */
public class Basic {
    @SerializedName("city")
    public String cityName; //城市

    @SerializedName("cnty")
    public String cnty;//国家

    @SerializedName("id")
    public String id;//城市id

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime; //天气更新时间
    }
}
