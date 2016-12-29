package mchenys.net.csdn.blog.coolweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import mchenys.net.csdn.blog.coolweather.gson.Forecast;
import mchenys.net.csdn.blog.coolweather.gson.Weather;
import mchenys.net.csdn.blog.coolweather.service.AutoUpdateService;
import mchenys.net.csdn.blog.coolweather.util.HttpUtil;
import mchenys.net.csdn.blog.coolweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 天气界面
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
    private Toolbar toolBar;
    private String weatherId;//天气id

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initView();
        initData();
        initListener();
        initAutoUpdateService();
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
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");
        toolBar.setNavigationIcon(R.drawable.ic_home);
        setSupportActionBar(toolBar);
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
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 0) {
                    showShareDialog();
                } else if (item.getItemId() == 1) {
                    showMyLocation();
                }
                return true;
            }


        });
    }


    private void initAutoUpdateService() {
        //开启自动更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        intent.putExtra("isFirst", true);
        startService(intent);
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
                maxText.setText(forecast.temperature.max + "℃");
                minText.setText(forecast.temperature.min + "℃");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "短信分享");
        menu.add(0, 1, 1, "出行助手");
        return true;
    }


    /**
     * 弹出分享列表
     */
    private void showShareDialog() {

        final EditText et = new EditText(this);

        new AlertDialog.Builder(this).setTitle("输入手机号码")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (TextUtils.isEmpty(input)) {
                            Toast.makeText(getApplicationContext(), "手机号码不能为空！", Toast.LENGTH_LONG).show();
                        } else {
                            sendSMS(input);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 发短信
     */
    private void sendSMS(String phone) {
        registerReceiver(mSmsSendBroadcastReceiver, new IntentFilter("SENT_SMS_ACTION"));
        StringBuilder sb = new StringBuilder();
        sb.append(titleUpdateTime.getText() + " ");
        sb.append(titleCity.getText());
        sb.append(degreeText.getText() + " ");
        sb.append(weatherInfoText.getText() + " ");
        sb.append("临近3天天气情况:");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);

        if (null != weatherString) {
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            for (Forecast forecast : weather.forecastList) {
                sb.append(forecast.date + " " + forecast.more.info + " 最低气温:" + forecast.temperature.min + "℃ 最高气温:" + forecast.temperature.max + "℃;");
            }
            sb.append(weather.suggestion.dress.info);
        }
        String message = sb.toString();
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent("SENT_SMS_ACTION"), 0);

        if (message.length() > 70) {
            ArrayList<String> msgs = smsManager.divideMessage(message);
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < msgs.size(); i++) {
                sentIntents.add(sentPI);
            }
            smsManager.sendMultipartTextMessage(phone, null, msgs, sentIntents, null);
        } else {
            smsManager.sendTextMessage(phone, null, message, sentPI, null);
        }
    }

    private BroadcastReceiver mSmsSendBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "信息已发出", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "未指定失败 \n 信息未发出，请重试", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "无线连接关闭 \n 信息未发出，请重试", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "PDU失败 \n 信息未发出，请重试", Toast.LENGTH_LONG).show();
                    break;
            }
            WeatherActivity.this.unregisterReceiver(this);
        }
    };

    private void showMyLocation() {
        startActivity(new Intent(this, BDMapActivity.class));
    }
}


