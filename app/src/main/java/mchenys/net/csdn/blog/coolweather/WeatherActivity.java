package mchenys.net.csdn.blog.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import mchenys.net.csdn.blog.coolweather.gson.Forecast;
import mchenys.net.csdn.blog.coolweather.gson.Weather;
import mchenys.net.csdn.blog.coolweather.service.AutoUpdateService;
import mchenys.net.csdn.blog.coolweather.util.HttpUtil;
import mchenys.net.csdn.blog.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mChenys on 2016/12/23.
 */
public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private TextView healthText;
    private TextView dressText;
    private ImageView bingPicImg;
    private SwipeRefreshLayout refreshLayout;
    private DrawerLayout drawerLayout;
    private Button navButton;

    private String weatherId;//天气id

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        setContentView(R.layout.activity_weather);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.sv_weather);
        titleCity = (TextView) findViewById(R.id.tv_title);
        titleUpdateTime = (TextView) findViewById(R.id.tv_update_time);
        degreeText = (TextView) findViewById(R.id.tv_degree);
        weatherInfoText = (TextView) findViewById(R.id.tv_weather_info);
        forecastLayout = (LinearLayout) findViewById(R.id.ll_forecast);
        aqiText = (TextView) findViewById(R.id.tv_aqi);
        pm25Text = (TextView) findViewById(R.id.tv_pm25);
        comfortText = (TextView) findViewById(R.id.tv_comfort);
        carWashText = (TextView) findViewById(R.id.tv_car_wash);
        sportText = (TextView) findViewById(R.id.tv_sport_text);
        healthText = (TextView) findViewById(R.id.tv_health_text);
        dressText = (TextView) findViewById(R.id.tv_dress_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.btn_nav);

    }

    private void initData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);

        if (null != weatherString) {
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather();
        }
        String bingPic = prefs.getString("bing_pic", null);
        if (null != bingPic) {
            //有缓存则加载缓存
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    private void initListener() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather();
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                //保存缓存
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 根据城市Id请求城市天气信息
     */
    private void requestWeather() {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=b7957d1187704a53a3f21dfdb33a7458";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != weather && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });

            }
        });
        loadBingPic();
    }

    /**
     * 处理并展示数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        if (null != weather && "ok".equals(weather.status)) {
            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            String degree = weather.now.temperature + "℃";
            String weatherInfo = weather.now.more.info;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);

            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.forecastList) {
                View view = View.inflate(this, R.layout.forecast_item, null);
                TextView dateText = (TextView) view.findViewById(R.id.tv_date);
                TextView infoText = (TextView) view.findViewById(R.id.tv_info);
                TextView maxText = (TextView) view.findViewById(R.id.tv_max);
                TextView minText = (TextView) view.findViewById(R.id.tv_min);
                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max+"℃");
                minText.setText(forecast.temperature.min+"℃");
                forecastLayout.addView(view);
            }

            if (weather.aqi != null) {
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }

            String comfort = "舒适度:" + weather.suggestion.comfort.info;
            String carWash = "洗车指数:" + weather.suggestion.carwash.info;
            String sport = "运动建议:" + weather.suggestion.sport.info;
            String health = "健康卫士:" + weather.suggestion.health.info;
            String dress = "衣着建议:" + weather.suggestion.dress.info;

            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            healthText.setText(health);
            dressText.setText(dress);

            weatherLayout.setVisibility(View.VISIBLE);

            //开启自动更新服务
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }

    }

    public void closeDrawerLayout() {
        drawerLayout.closeDrawers();
        refreshLayout.setRefreshing(true);
        requestWeather();
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }


}


