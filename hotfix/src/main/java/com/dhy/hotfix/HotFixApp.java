package com.dhy.hotfix;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

public class HotFixApp extends Application {
    private final String appInitClassName;
    private IAppInit appInit;

    /**
     * 类名只能用字符串方式传参，不能用 AppInit.class.getName()，否则无法加载到补丁包中的类。
     * ClassLoader加载App类时，会自动把它import的类也加载了。
     */
    public HotFixApp(String appInitClassName) {
        this.appInitClassName = appInitClassName;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            HotFix.loadFile(this);
        } catch (Exception e) {
            Log.e("HotFix", "HotFix error");
            e.printStackTrace();
        }

        if (appInit == null) initApp(appInitClassName);
        appInit.onAppCreate(this);
    }

    private void initApp(String className) {
        try {
            Class<?> appInitClass = Class.forName(className);
            appInit = (IAppInit) appInitClass.newInstance();
        } catch (Exception e) {
            Log.e("HotFix", "initApp error");
            e.printStackTrace();
        }
    }

    @NonNull
    public IAppInit getAppInit() {
        return appInit;
    }
}
