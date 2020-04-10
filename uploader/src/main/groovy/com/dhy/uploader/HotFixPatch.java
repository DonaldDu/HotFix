package com.dhy.uploader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class HotFixPatch {
    public static File genHotFixPatch(File apkFile, String pn, int vc) throws IOException {
        ZipFile apk = new ZipFile(apkFile);
        File patch = new File(apkFile.getParentFile(), String.format("%s-vc%d.hotfix.apk", pn, vc));
        if (patch.exists()) patch.delete();
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(patch));
        Enumeration<? extends ZipEntry> entries = apk.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".dex")) {
                outputStream.putNextEntry(new ZipEntry(entry.getName()));
                InputStream inputStream = apk.getInputStream(entry);
                copyStream(inputStream, outputStream);
                inputStream.close();
            }
        }
        outputStream.flush();
        outputStream.close();
        return patch;
    }

    private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024 * 1024 * 10];
        int size;
        while (true) {
            size = inputStream.read(buffer);
            if (size > 0) outputStream.write(buffer, 0, size);
            else break;
        }
    }
}
