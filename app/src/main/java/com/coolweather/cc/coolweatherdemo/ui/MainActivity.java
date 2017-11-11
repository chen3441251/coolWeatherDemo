package com.coolweather.cc.coolweatherdemo.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.coolweather.cc.coolweatherdemo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ChooseAreaFragment chooseFragment = (ChooseAreaFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_chooseArea);
        int currentLevel = chooseFragment.getCurrentLevel();
        if(keyCode==KeyEvent.KEYCODE_BACK){
            switch (currentLevel) {
                //县级市
                case 2:
                    chooseFragment.queryCities();
                    break;
                    //城市
                case 1:
                    chooseFragment.queryProvince();
                    break;
                    //默认首页
                default:
                    finish();
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
