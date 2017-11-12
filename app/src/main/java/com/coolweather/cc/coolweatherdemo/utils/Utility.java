package com.coolweather.cc.coolweatherdemo.utils;

import android.text.TextUtils;

import com.coolweather.cc.coolweatherdemo.db.City;
import com.coolweather.cc.coolweatherdemo.db.County;
import com.coolweather.cc.coolweatherdemo.db.Province;
import com.coolweather.cc.coolweatherdemo.gson.WeatherBean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by CC on 2017/11/7.
 * 解析城市天气预报数据保存数据库工具类
 */

public class Utility {
        //解析json保存到数据库
    public static boolean handleProvinceResponse(String respone) {
        if (!TextUtils.isEmpty(respone)) {
            try {
                JSONArray jsonArray = new JSONArray(respone);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject provinceObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    //保存数据库
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析city
     * */
    public static boolean handleCityResponse(String response,int proviceId){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject cityObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(proviceId);
                    //保存数据库
                    city.save();

                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;

    }
    /**
     * 解析县级市数据
     * */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject countyObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    //这里获取到了对应城市的天气的id
                    county.setWeatherId(countyObject.getString("weather_id"));
                    //保存天气预报
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;

    }
    /**
     * 解析天气数据
     * */
    public static WeatherBean.HeWeatherBean handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherStr = jsonArray.getJSONObject(0).toString();
            WeatherBean.HeWeatherBean weatherBean = new Gson().fromJson(weatherStr, WeatherBean.HeWeatherBean.class);
            return weatherBean;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
