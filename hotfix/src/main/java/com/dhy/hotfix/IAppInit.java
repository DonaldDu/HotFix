package com.dhy.hotfix;

import android.app.Application;

/**
 * 补丁包不能修复已经加载的Application类，所以把Application类的代码移动到AppInit类中以保证能修复到。
 */
public interface IAppInit {
    void onAppCreate(Application application);
}
