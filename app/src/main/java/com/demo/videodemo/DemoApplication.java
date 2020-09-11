package com.demo.videodemo;

import android.app.Application;

import com.demo.videodemo.utils.AppUtils;
import com.demo.videodemo.utils.ThreadPool;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThreadPool.start();
        AppUtils.setApp(this);
    }
}
