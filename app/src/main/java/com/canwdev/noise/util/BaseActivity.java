package com.canwdev.noise.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by CAN on 2017/11/10.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 知晓当前是在哪个活动
        // BaseActivity extends AppCompatActivity
        // FirstActivity extends BaseActivity
        Log.d("BaseActivity", getClass().getSimpleName());

        //随时随地优雅的退出，需要结合ActivityCollector使用
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //随时随地优雅的退出，需要结合ActivityCollector使用
        ActivityCollector.removeActivity(this);
    }
}
