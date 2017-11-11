package com.coolweather.cc.coolweatherdemo.base;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by CC on 2017/11/5.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化litepal
        LitePal.initialize(this);
    }

}
