package com.coolweather.cc.coolweatherdemo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.coolweather.cc.coolweatherdemo.gson.WeatherBean;
import com.coolweather.cc.coolweatherdemo.utils.HttpUtil;
import com.coolweather.cc.coolweatherdemo.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 自动定时更新天气数据
 * */
public class AutoUpdataService extends Service {
    public AutoUpdataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //更新天气数据
        updataWeatherData();
        //更新背景图片
        updataBackGround();
        //设定定时任务
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // 设置更新间隔时间
        int limitTime=2*60*60*1000;
        long l = SystemClock.elapsedRealtime() + limitTime;
        Intent i = new Intent(this, AutoUpdataService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,l,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updataBackGround() {
        //更新背景图片
        String url="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取新图片的地址
                String url = response.body().string();
                //缓存新数据到本地
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(AutoUpdataService.this).edit();
                edit.putString("image_background",url);
                edit.apply();
            }
        });
    }

    private void updataWeatherData() {
        String weather = PreferenceManager.getDefaultSharedPreferences(this).getString("weather", null);
        if (weather != null) {
            //如果本地有天气数据，则取出里面的weatherid再去请求一次当前的地区的天气数据
            WeatherBean.HeWeatherBean weatherBean = Utility.handleWeatherResponse(weather);
            String weatherId = weatherBean.getBasic().getId();
            String url="http://guolin.tech/api/weather?cityid="+weatherId+"&key=d1543e4f3fd24c4b8bb4aa69b9541c1c";
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherStr = response.body().string();
                    WeatherBean.HeWeatherBean weatherBean = Utility.handleWeatherResponse(weatherStr);
                    if (weatherBean != null&&"ok".equals(weatherBean.getStatus())) {
                        //缓存数据到本地，覆盖旧数据
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(AutoUpdataService.this).edit();
                        edit.putString("weather",weatherStr);
                        edit.apply();
                    }

                }
            });
        }
    }
}
