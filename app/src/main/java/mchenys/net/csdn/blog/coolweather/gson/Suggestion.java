package mchenys.net.csdn.blog.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 一些和天气相关的建议信息
 * Created by mChenys on 2016/12/23.
 */
public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort; //天气舒适,白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。

    @SerializedName("cw")
    public CarWash carwash; //天气较适宜,较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。

    public Sport sport;//较适宜,天气较好，但考虑气温较低，推荐您进行室内运动，若户外适当增减衣物并注意防晒。

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    public class CarWash{
        @SerializedName("txt")
        public String info;
    }

    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
