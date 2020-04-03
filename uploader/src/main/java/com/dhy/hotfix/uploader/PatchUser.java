package com.dhy.hotfix.uploader;

import java.io.Serializable;
import java.util.List;

public class PatchUser implements Serializable {
    /**
     * 版本号可以用通配符 "*" 匹配所有，或者是完全匹配
     */
    public String version;
    /**
     * 用户为登录输入的用户名完全匹配。可以用@符号来加上注释：["123@Donald"]
     */
    public List<String> users;
    /**
     * 设备号为AndroidId。可以用@符号来加上注释： ["123@Donald"]
     */
    public List<String> uuids;
    /**
     * 显示更新提示
     */
    public boolean showTip;
}
