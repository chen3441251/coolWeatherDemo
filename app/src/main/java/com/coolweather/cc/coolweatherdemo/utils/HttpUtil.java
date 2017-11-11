package com.coolweather.cc.coolweatherdemo.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by CC on 2017/11/6.
 */

public class HttpUtil {
    //okhttp工具类，get请求
    public static void sendOkHttpRequest(String url, Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
