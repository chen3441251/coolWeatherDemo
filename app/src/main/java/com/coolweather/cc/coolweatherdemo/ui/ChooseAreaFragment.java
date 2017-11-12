package com.coolweather.cc.coolweatherdemo.ui;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.cc.coolweatherdemo.R;
import com.coolweather.cc.coolweatherdemo.db.City;
import com.coolweather.cc.coolweatherdemo.db.County;
import com.coolweather.cc.coolweatherdemo.db.Province;
import com.coolweather.cc.coolweatherdemo.utils.HttpUtil;
import com.coolweather.cc.coolweatherdemo.utils.Utility;

import org.litepal.crud.ClusterQuery;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ChooseAreaFragment extends Fragment {


    private TextView mTitle;
    private TextView mBack;
    private ListView mListView;
    ArrayList<String> dataList = new ArrayList<>();
    //定义地区层级类别
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    //省份列表
    public List<Province> provinceList;
    //城市列表
    public List<City> cityList;
    //县级列表
    public List<County> countyList;
    //选中的省份
    public Province selectProvince;
    //选中的城市
    public City selectCity;
    //当前选中的级别
    public int currentLevel = -1;
    private ArrayAdapter<String> mAreaAdapter;
    private ProgressDialog mProgressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //加载布局文件
        View view = inflater.inflate(R.layout.choose_area, container, false);
        //初始化控件(标题，返回键,列表)
        mTitle = (TextView) view.findViewById(R.id.tv_title);
        mBack = (TextView) view.findViewById(R.id.btn_back);
        mListView = (ListView) view.findViewById(R.id.list_view);
        //创建适配器
        mAreaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAreaAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //默认初始化查询本地数据库的省份列表(首次进来是没有的)
        queryProvince();
        //处理点击事件
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            switch (currentLevel) {
                // 省份条目
                case LEVEL_PROVINCE:
                    //默认是第一个
                    selectProvince = provinceList.get(position);
                    //去查city列表
                    queryCities();
                    break;
                //城市条目
                case LEVEL_CITY:
                    selectCity = cityList.get(position);
                    //去查县级市列表
                    queryCounties();
                    break;
                case LEVEL_COUNTY:
                    //点击县级市跳转天气详情
                    //获取当前点击县级市的天气id
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        // 如果碎片属于首页
                        Intent intent = new Intent();
                        intent.setClass(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        //如果碎片属于天气界面的侧滑菜单
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        //关闭侧滑菜单
                        activity.mDrawerLayout.closeDrawers();
                        //同时去查询所选城市的天气
                        activity.getWeatherFromServer(weatherId);
                        //刷新按钮
                        activity.mSwipeRefreshLayout.setRefreshing(true);
                        activity.weatherId=weatherId;
                    }

                    break;
                default:
                    break;
            }
        });



        mBack.setOnClickListener(v -> {
            switch (currentLevel) {
                case LEVEL_COUNTY:
                    //当前是县级市，返回上一页则去查询城市列表
                    queryCities();
                    break;
                case LEVEL_CITY:
                    //当前是城市，则去查询省份
                    queryProvince();
                    break;
                default:
                    break;
            }
        });


    }

    protected void queryProvince() {
        //初始化界面
        mTitle.setText("中国");
        mBack.setVisibility(View.GONE);
        //查询本地数据库
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0) {
            //创建一个临时集合来存储列表省份
            dataList.clear();
            //本地数据库有数据
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            //更新列表数据
            mAreaAdapter.notifyDataSetChanged();
            //赋值当前选中的级别是省份
            currentLevel=LEVEL_PROVINCE;
            //默认选中第一条数据
            mListView.setSelection(0);
        }else {
            //本地数据库无数据,需要联网获取
            String url="http://guolin.tech/api/china";
            getDataFromServer(url,LEVEL_PROVINCE);
        }
    }



    private void queryCounties() {
        //初始化界面标题栏和返回按钮
        mTitle.setText(selectCity.getCityName());
        mBack.setVisibility(View.VISIBLE);
        //查询数据库
        countyList = DataSupport.where("cityId=?", String.valueOf(selectCity.getCityCode())).find(County.class);
        if (countyList.size()>0) {
            //如果该城市下的县级市本地数据有则查本地
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            //当前级别是县级市
            currentLevel=LEVEL_COUNTY;
            mAreaAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
        }else {
            //网络获取县级市
            String url="http://guolin.tech/api/china/"+selectProvince.getProvinceCode()+"/"+selectCity.getCityCode();
            getDataFromServer(url,LEVEL_COUNTY);
        }
    }

    protected void queryCities() {
        //城市页面
        mTitle.setText(selectProvince.getProvinceName());
        mBack.setVisibility(View.VISIBLE);
        //查询当前城市数据库,根据选中城市条件查询
        cityList = DataSupport.where("provinceId=?",String.valueOf(selectProvince.getProvinceCode())).find(City.class);
        if (cityList.size()>0) {
            dataList.clear();
            //查询本地数据库数据
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            //当前层级是城市
            currentLevel=LEVEL_CITY;
            //刷新listview数据
            mAreaAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
        }else {
            //网络获取当前城市数据
            int provinceCode = selectProvince.getProvinceCode();
            String url="http://guolin.tech/api/china/"+provinceCode;
            getDataFromServer(url,LEVEL_CITY);
        }
    }
    private void getDataFromServer(String url, int level) {
        //显示网络请求loading
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(()->{
                    closeProgressDialog();
                    Toast.makeText(getContext(),"数据加载失败",Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                closeProgressDialog();
                //如果成功获取数据则根据type解析列表数据并存入到数据库
                String responseStr = response.body().string();
                Log.d("xxx","responseStr=="+responseStr);
                boolean isSuccess=false;
                switch (level) {
                    case LEVEL_PROVINCE:
                        //解析并存入到数据库,返回值是存储数据是否成功
                         isSuccess = Utility.handleProvinceResponse(responseStr);
                        break;
                    case LEVEL_CITY:
                        isSuccess=Utility.handleCityResponse(responseStr,selectProvince.getProvinceCode());
                        break;
                    case LEVEL_COUNTY:
                        isSuccess=Utility.handleCountyResponse(responseStr,selectCity.getCityCode());
                        break;
                    default:
                        break;
                }
                if (isSuccess) {
                    //如果存储本地数据库成功则再次去读取数据
                    getActivity().runOnUiThread(()->{
                        switch (level) {
                            case LEVEL_PROVINCE:
                                queryProvince();
                                break;
                            case LEVEL_CITY:
                                queryCities();
                                break;
                            case LEVEL_COUNTY:
                                queryCounties();
                                break;
                            default:
                                break;
                        }
                    });

                }
            }
        });
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();

    }
    protected int getCurrentLevel(){
        return currentLevel;
    }

}
