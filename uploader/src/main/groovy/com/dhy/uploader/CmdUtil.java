package com.dhy.uploader;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

public class CmdUtil {
    public static void excuteCMD(File postmanScript, BatParams params, Map<String, ?> extraEnvs) throws Exception {
        fixPostmanJson(postmanScript);
        runScript(postmanScript, params.toEnvironmentFilePath(extraEnvs));
    }

    private static void fixPostmanJson(File script) throws IOException, JSONException {
        String json = FileUtils.readFileToString(script, Charset.defaultCharset());
        JSONObject jsonObject = new JSONObject(json);
        JSONArray items = jsonObject.getJSONArray("item");
        boolean updated = false;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            try {
                JSONArray formdata = item.getJSONObject("request").getJSONObject("body").getJSONArray("formdata");
                updated = replaceFileItem(formdata) || updated;
            } catch (JSONException ignored) {

            }
        }
        if (updated) {
            FileUtils.writeStringToFile(script, jsonObject.toString(), Charset.defaultCharset());
        }
    }

    private static boolean replaceFileItem(JSONArray formdata) throws JSONException {
        boolean updated = false;
        Pattern evnReg = Pattern.compile("\\{\\{[^}]+}}");
        for (int i = 0; i < formdata.length(); i++) {
            JSONObject form = formdata.getJSONObject(i);
            String type = form.getString("type");
            if (type.equals("file")) {
                String description = form.getString("description");
                String src = form.getString("src");
                if (!src.equals(description) && evnReg.matcher(description).find()) {
                    System.out.println(form.toString());
                    form.put("src", description);
                    updated = true;
                }
            }
        }
        return updated;
    }

    public static void runScript(File script, String evn) throws IOException {
        File bats = getBatFolder(script);
        File bat = new File(bats, getBatName(script));
        if (!bat.exists()) createScript(bat, script);
        String cmd = String.format("cd /d %s && start %s %s", script.getParent(), bat.getAbsolutePath(), evn);
        excuteCMD(cmd);
    }

    private static void excuteCMD(String cmd) throws IOException {
        String path = System.getenv("path");
        String[] envp = new String[]{"path=" + path};

        String[] cmdarray = new String[]{"cmd", "/c", cmd};
        Runtime.getRuntime().exec(cmdarray, envp);
    }

    private static String getBatName(File script) {
        String end = String.format("-%d.bat", script.lastModified());
        return script.getName().replace(POSTMAN_COLLECTION_JSON, end);
    }

    private static void createScript(File bat, File json) throws IOException {
        deleteOldBatFile(json);
        String script = "newman run JSON_FILE_NAME --reporter-cli-no-summary --environment %1"
                .replace("JSON_FILE_NAME", json.getName());
        FileUtils.writeStringToFile(bat, script, Charset.defaultCharset());
    }

    private static final String POSTMAN_COLLECTION_JSON = ".postman_collection.json";

    private static File getBatFolder(File script) {
        return new File(script.getParent(), "bats");
    }

    private static void deleteOldBatFile(File script) {
        String start = script.getName().replace(POSTMAN_COLLECTION_JSON, "");
        File batFolder = getBatFolder(script);
        File[] files = batFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(start)) file.delete();
            }
        }
    }
}
