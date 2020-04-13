package com.dhy.hotfix.demo.net;

import androidx.annotation.NonNull;

import com.dhy.hotfix.updater.IPatchVersion;
import com.google.gson.annotations.SerializedName;

public class AppVersion implements IPatchVersion {
    public String url;
    public String message;
    @SerializedName("versiontype")
    public int versionType;

    @NonNull
    @Override
    public String getUrl() {
        return url;
    }

    @SerializedName("versioncode")
    public int versionCode;

    @Override
    public int getVersionCode() {
        return versionCode;
    }
}
