package com.dhy.hotfix.demo;


import android.app.Application;

import com.dhy.hotfix.HotFix;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initHotFix();
    }

    private void initHotFix() {
        HotFix.init(this, "com.dhy.hotfix.demo.AppInit");
    }
}
