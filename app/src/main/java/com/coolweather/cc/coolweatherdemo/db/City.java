package com.coolweather.cc.coolweatherdemo.db;

import org.litepal.crud.DataSupport;

/**
 * Created by CC on 2017/11/5.
 * 城市
 */

public class City extends DataSupport {
    //自增长id
    private int id;
    //城市名称
    private String cityName;
    //城市编号
    private int cityCode;
    //所属省份
    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
