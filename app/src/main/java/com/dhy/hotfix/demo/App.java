package com.dhy.hotfix.demo;


import android.app.Application;

import java.lang.reflect.Constructor;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initHotFix();
    }

    /**
     * 通过反射的方式调用HotFix，这样也可以修复HotFix的代码问题。
     */
    private void initHotFix() {
        try {
            Class<?> hostFix = Class.forName("com.dhy.hotfix.HotFix");
            Constructor<?> constructor = hostFix.getConstructor(Application.class, String.class);
            constructor.newInstance(this, "com.dhy.hotfix.demo.AppInit");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
