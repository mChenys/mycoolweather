package mchenys.net.csdn.blog.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 未来几天的天气信息
 * Created by mChenys on 2016/12/23.
 */
public class Forecast {
    public String date; //"2016-12-23"
    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{
        public String max;
        public String min;
    }

    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
