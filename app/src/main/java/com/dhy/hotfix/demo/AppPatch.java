package com.dhy.hotfix.demo;

import androidx.multidex.MultiDexApplication;

import com.dhy.hotfix.HotFix;

public class AppPatch extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        initHotFix();
    }

    private void initHotFix() {
        HotFix.init(this, "com.dhy.hotfix.demo.App");
    }
}
