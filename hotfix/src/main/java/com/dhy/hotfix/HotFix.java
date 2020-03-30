package com.dhy.hotfix;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class HotFix {
    static void loadFile(Context context) throws Exception {
        String name = "patch/dexOnly.apk";
        File dexOnlyApk = new File(context.getFilesDir(), name);
        if (!dexOnlyApk.exists()) {
            File folder = dexOnlyApk.getParentFile();
            //noinspection ConstantConditions
            if (!folder.exists()) folder.mkdirs();
            dexOnlyApk.createNewFile();

            InputStream inputStream = context.getAssets().open(name);
            copyStream(inputStream, new FileOutputStream(dexOnlyApk));
        }
        loadPatch(context, dexOnlyApk);
    }

    private static void loadPatch(Context context, File file) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
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
