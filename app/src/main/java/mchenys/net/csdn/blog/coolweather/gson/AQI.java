package mchenys.net.csdn.blog.coolweather.gson;

/**
 * 空气质量信息
 * Created by mChenys on 2016/12/23.
 */
public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
