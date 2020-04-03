package com.dhy.hotfix.uploader;

import androidx.annotation.NonNull;

import java.io.Serializable;

public interface IVersion extends Serializable {
    @NonNull
    String getUrl();

    int getVersionCode();
}
