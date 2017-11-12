package com.coolweather.cc.coolweatherdemo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.cc.coolweatherdemo.R;
import com.coolweather.cc.coolweatherdemo.gson.WeatherBean;
import com.coolweather.cc.coolweatherdemo.service.AutoUpdataService;
import com.coolweather.cc.coolweatherdemo.utils.HttpUtil;
import com.coolweather.cc.coolweatherdemo.utils.Utility;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 展示天气的界面
 */
public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.tv_titile)
    TextView mTvTitile;
    @BindView(R.id.tv_updatetime)
    TextView mTvUpdatetime;
    @BindView(R.id.tv_temp)
    TextView mTvTemp;
    @BindView(R.id.tv_weatherinfo)
    TextView mTvWeatherinfo;
    @BindView(R.id.tv_des)
    TextView mTvDes;
    @BindView(R.id.ll_forecast_layout)
    LinearLayout mLlForecastLayout;
    @BindView(R.id.tv_aqi_titile)
    TextView mTvAqiTitile;
    @BindView(R.id.tv_aqi)
    TextView mTvAqi;
    @BindView(R.id.tv_pm25)
    TextView mTvPm25;
    @BindView(R.id.tv_comf)
    TextView mTvComf;
    @BindView(R.id.tv_wash)
    TextView mTvWash;
    @BindView(R.id.tv_sport)
    TextView mTvSport;
    @BindView(R.id.scrollView)
    ScrollView mScrollView;
    @BindView(R.id.iv_background)
    ImageView mIvBackground;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.tv_home)
    TextView mTvHome;
    @BindView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;
    String weatherId ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果当前系统是5.0以上,设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //设置状态栏颜色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weather = sp.getString("weather", null);
        String image_background = sp.getString("image_background", null);
        if (image_background != null) {
            Glide.with(this).load(image_background).into(mIvBackground);
        } else {
            //网络获取图片
            getImageBackgroudFromServer();
        }
        if (weather != null) {
            // 从本地读取天气数据
            WeatherBean.HeWeatherBean weatherBean = Utility.handleWeatherResponse(weather);
            showWeatherInfo(weatherBean);
            weatherId = weatherBean.getBasic().getId();
        } else {
            //无本地缓存区网络请求获取上一个界面跳转传递过来的weather_id
            weatherId = getIntent().getStringExtra("weather_id");
            mScrollView.setVisibility(View.INVISIBLE);
            //网络获取天气数据
            getWeatherFromServer(weatherId);
        }
        //手动下拉刷新监听
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            //网络获取天气数据
            getWeatherFromServer(weatherId);
        });
        //侧滑菜单切换城市
        mTvHome.setOnClickListener((v)->{
            mDrawerLayout.openDrawer(GravityCompat.START);
        });
    }

    private void getImageBackgroudFromServer() {
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                edit.putString("image_background", string);
                edit.apply();
                //展示图片
                runOnUiThread(() -> {
                    Glide.with(WeatherActivity.this).load(string).into(mIvBackground);
                });
            }
        });
    }

    protected void getWeatherFromServer(String weather_id) {
        String url = "http://guolin.tech/api/weather?cityid=" + weather_id + "&key=d1543e4f3fd24c4b8bb4aa69b9541c1c";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取天气数据失败", Toast.LENGTH_SHORT).show();
                    //关闭下拉刷新状态
                    mSwipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String weatherStr = response.body().string();
                Log.d("xxx","respones="+weatherStr);
                WeatherBean.HeWeatherBean weatherBean = Utility.handleWeatherResponse(weatherStr);
                runOnUiThread(() -> {
                    if (weatherBean != null && "ok".equals(weatherBean.getStatus())) {
                        //把获取到时候数据保存在sp
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        edit.putString("weather", weatherStr);
                        edit.apply();
                        //把天气数据展示出来
                        showWeatherInfo(weatherBean);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气数据失败", Toast.LENGTH_SHORT).show();
                    }
                    //关闭下拉刷新状态
                    mSwipeRefreshLayout.setRefreshing(false);
                });
            }
        });
        //获取天气背景图片
        getImageBackgroudFromServer();
    }

    private void showWeatherInfo(WeatherBean.HeWeatherBean weatherBean) {
        mScrollView.setVisibility(View.VISIBLE);
        mTvTitile.setText(weatherBean.getBasic().getCity());
        mTvUpdatetime.setText("更新时间\t" + weatherBean.getBasic().getUpdate().getLoc().split(" ")[1]);
        mTvTemp.setText(weatherBean.getNow().getTmp() + "℃");
        mTvWeatherinfo.setText(weatherBean.getNow().getCond().getTxt());
        //未来几天天气
        List<WeatherBean.HeWeatherBean.DailyForecastBean> dailyForecast = weatherBean.getDaily_forecast();
        if (mLlForecastLayout.getChildCount()>0) {
            mLlForecastLayout.removeAllViews();
        }
        for (WeatherBean.HeWeatherBean.DailyForecastBean dailyForecastBean : dailyForecast) {
            View inflate = LayoutInflater.from(this).inflate(R.layout.forecast_item_layout, mLlForecastLayout, false);
            TextView tv_date = (TextView) inflate.findViewById(R.id.tv_date);
            TextView tv_info = (TextView) inflate.findViewById(R.id.tv_info);
            TextView tv_max_temp = (TextView) inflate.findViewById(R.id.tv_max_temp);
            TextView tv_min_temp = (TextView) inflate.findViewById(R.id.tv_min_temp);
            tv_date.setText(dailyForecastBean.getDate());
            tv_info.setText(dailyForecastBean.getCond().getTxt_d());
            tv_max_temp.setText(dailyForecastBean.getTmp().getMax());
            tv_min_temp.setText(dailyForecastBean.getTmp().getMin());
            //把布局加入到容器
            mLlForecastLayout.addView(inflate);
        }
        //空气质量
        if (weatherBean != null) {
            mTvAqi.setText(weatherBean.getAqi().getCity().getAqi());
            mTvPm25.setText(weatherBean.getAqi().getCity().getPm25());
        }
        //出行建议
        mTvComf.setText("舒适度：" + weatherBean.getSuggestion().getComf().getTxt());
        mTvWash.setText("洗车指数：" + weatherBean.getSuggestion().getCw().getTxt());
        mTvSport.setText("运动建议：" + weatherBean.getSuggestion().getSport().getTxt());
        //设置定时任务
        if (weatherBean != null&&"ok".equals(weatherBean.getStatus())) {
            Intent intent = new Intent(this, AutoUpdataService.class);
            startService(intent);
        }else {
            Toast.makeText(this,"更新数据失败",Toast.LENGTH_SHORT).show();
        }
    }
}
