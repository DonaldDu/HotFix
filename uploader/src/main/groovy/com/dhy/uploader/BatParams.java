package com.dhy.uploader;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class BatParams {
    private String appId;
    private int versionCode;
    private String versionName;
    private boolean isDebug;

    public String updateLog;
    public String apkFilePath;

    public BatParams(String appId, String versionName, int versionCode, boolean isDebug) {
        this.appId = appId;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.isDebug = isDebug;
    }

    String toEnvironmentFilePath(Map<String, ?> extraEnvs) throws IllegalAccessException, IOException {
        File apk = new File(apkFilePath);
        File envJsonFile = new File(apk.getParentFile(), "env.json");
        JSONArray jsonArray = new JSONArray();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", field.getName());
            jsonObject.put("value", field.get(this));
            jsonArray.add(jsonObject);
        }

        if (extraEnvs != null && !extraEnvs.isEmpty()) {
            for (String key : extraEnvs.keySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", key);
                jsonObject.put("value", extraEnvs.get(key));
                jsonArray.add(jsonObject);
            }
        }

        if (envJsonFile.exists()) envJsonFile.delete();
        envJsonFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(envJsonFile);
        outputStream.write(jsonArray.toJSONString().getBytes());
        outputStream.flush();
        outputStream.close();
        return envJsonFile.getAbsolutePath();
    }
}
