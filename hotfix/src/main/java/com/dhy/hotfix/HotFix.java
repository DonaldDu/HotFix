package com.dhy.hotfix;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class HotFix {
    private static final String name = "HotFix";

    /**
     * @param appInitClassName 可为null，类名只能用字符串方式传参，不能用 AppInit.class.getName()，否则无法加载到补丁包中的类。
     *                         ClassLoader加载App类时，会自动把它import的类也加载了。
     */
    public static void init(Application application, String appInitClassName) {
        try {
            HotFix.loadFile(application);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appInitClassName != null && !appInitClassName.isEmpty()) initApp(application, appInitClassName);
    }

    private static void initApp(Application application, String className) {
        try {
            Class<?> appInitClass = Class.forName(className);
            IAppInit appInit = (IAppInit) appInitClass.newInstance();
            appInit.onAppCreate(application);
        } catch (Exception e) {
            Log.e(name, "initApp error");
            e.printStackTrace();
        }
    }

    public static String formatPatchFileName(int versionCode) {
        return String.format("patch-vc%d.apk", versionCode);
    }

    /**
     * 检查补丁更新时，需要用这个参数
     */
    public static int getPatchVersion(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(name, 0);
    }

    /**
     * 补丁下载成功后更新以保存
     */
    public static void updatePatchVersion(Context context, int versionCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putInt(name, versionCode)
                .apply();
    }

    public static File getHotFixFolder(Context context) {
        return new File(context.getFilesDir(), name);
    }

    public static void clearBuffer(Context context) {
        File folder = getHotFixFolder(context);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) file.delete();
        }
    }

    static void loadFile(Context context) throws Exception {
        File patch = findPatch(context);
        if (patch != null && !patch.exists()) {
//            File folder = patch.getParentFile();
//            //noinspection ConstantConditions
//            if (!folder.exists()) folder.mkdirs();
//            patch.createNewFile();

//            InputStream inputStream = context.getAssets().open(name);
//            copyStream(inputStream, new FileOutputStream(patch));
        }
        if (patch != null && patch.exists() && patch.length() > 0) loadPatch(context, patch);
    }

    private static File findPatch(Context context) {
        int versionCode = getVersionCode(context);
        File folder = getHotFixFolder(context);
        File[] files = folder.listFiles();
        File patch = null;
        final String startKey = "-vc";
        if (files != null) {
            for (File file : files) {//patch-com.dhy.hotfix.demo-vc1.apk
                String name = file.getName();
                int start = name.lastIndexOf(startKey);
                int end = name.indexOf(".apk");
                if (start != -1 && end != -1) {
                    int vc = Integer.parseInt(name.substring(start + startKey.length(), end));
                    if (vc > versionCode) {
                        versionCode = vc;
                        patch = file;
                    }
                }
            }
        }
        return patch;
    }

    private static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static void loadPatch(Context context, File file) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Log.i(name, "loadPatch: " + file.getAbsolutePath());
        Field pathListField = getPathListField();
        ClassLoader parentLoader = context.getClassLoader();
        Object parentPathList = pathListField.get(parentLoader);

        ClassLoader extraLoader = createDexClassLoader(context, file, parentLoader);
        Object extraPathList = pathListField.get(extraLoader);

        @SuppressWarnings("ConstantConditions")
        Field dexElementsField = getDexElementsField(parentPathList);

        Object parentElements = dexElementsField.get(parentPathList);
        Object extraElements = dexElementsField.get(extraPathList);
        Object newElements = insertElements(parentElements, extraElements);

        dexElementsField.set(parentPathList, newElements);
    }

    /**
     * 将补丁插入系统DexElements[]最前端，生成一个新的DexElements[]
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    private static Object insertElements(Object parentElements, Object extraElements) {
        int pLen = Array.getLength(parentElements);
        int eLen = Array.getLength(extraElements);
        //noinspection ConstantConditions
        Object newElements = Array.newInstance(parentElements.getClass().getComponentType(), pLen + eLen);
        System.arraycopy(extraElements, 0, newElements, 0, eLen);
        System.arraycopy(parentElements, 0, newElements, eLen, pLen);
        return newElements;
    }

    private static Field getPathListField() throws ClassNotFoundException, NoSuchFieldException {
        Class c = Class.forName("dalvik.system.BaseDexClassLoader");
        @SuppressWarnings("JavaReflectionMemberAccess")
        Field pathList = c.getDeclaredField("pathList");
        pathList.setAccessible(true);
        return pathList;
    }

    private static ClassLoader createDexClassLoader(Context context, File file, ClassLoader parent) {
        String opt = context.getDir("opt", Context.MODE_PRIVATE).getAbsolutePath();
        return new DexClassLoader(file.getAbsolutePath(), opt, null, parent);
    }

    private static Field getDexElementsField(Object pathList) throws NoSuchFieldException {
        Field dexElements = pathList.getClass().getDeclaredField("dexElements");
        dexElements.setAccessible(true);
        return dexElements;
    }

    private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024 * 1024 * 10];
        int size;
        while (true) {
            size = inputStream.read(buffer);
            if (size > 0) outputStream.write(buffer, 0, size);
            else break;
        }
        inputStream.close();
        outputStream.close();
    }
}
