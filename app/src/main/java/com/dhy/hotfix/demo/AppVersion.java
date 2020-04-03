package com.dhy.hotfix.demo;

import androidx.annotation.NonNull;

import com.dhy.hotfix.uploader.IVersion;

public class AppVersion implements IVersion {
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
