package com.dhy.hotfix.demo;

import androidx.annotation.NonNull;

import com.dhy.hotfix.updater.IPatchVersion;

public class AppVersion implements IPatchVersion {
    public String url;

    @NonNull
    @Override
    public String getUrl() {
        return url;
    }

    public int versionCode;

    @Override
    public int getVersionCode() {
        return versionCode;
    }
}
