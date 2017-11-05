package com.coolweather.cc.coolweatherdemo;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by CC on 2017/11/5.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }

}
