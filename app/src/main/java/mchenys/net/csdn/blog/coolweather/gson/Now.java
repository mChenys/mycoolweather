package mchenys.net.csdn.blog.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 当前天气信息
 * Created by mChenys on 2016/12/23.
 */
public class Now {
    @SerializedName("tmp")
    public String temperature; //温度

    @SerializedName("cond")
    public More more; //云的状态bean

    public class More{
        @SerializedName("txt")
        public String info; //云的状态信息
    }
}
