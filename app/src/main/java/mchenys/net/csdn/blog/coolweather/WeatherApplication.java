package mchenys.net.csdn.blog.coolweather;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import org.litepal.LitePalApplication;

/**
 * Created by mChenys on 2016/12/26.
 */
public class WeatherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //litepal初始化
        LitePalApplication.initialize(this);
    }
}
