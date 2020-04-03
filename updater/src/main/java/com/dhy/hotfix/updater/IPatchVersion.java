package com.dhy.hotfix.updater;

import java.io.Serializable;

public interface IPatchVersion extends Serializable {

    String getUrl();

    int getVersionCode();
}
