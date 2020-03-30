package com.dhy.hotfix;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DexOnly {
    @Test
    public void test() throws IOException {
        File file = new File("D:\\work\\Test\\app\\build\\outputs\\apk\\debug\\app-debug.apk");
        ZipFile apk = new ZipFile(file);
        File dexOnlyFile = new File(file.getParentFile(), "dexOnly.apk");
        if (dexOnlyFile.exists()) dexOnlyFile.delete();
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(dexOnlyFile));
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
        System.out.println("all finish");
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024 * 1024 * 10];
        int size;
        while (true) {
            size = inputStream.read(buffer);
            if (size > 0) outputStream.write(buffer, 0, size);
            else break;
        }
    }
}
