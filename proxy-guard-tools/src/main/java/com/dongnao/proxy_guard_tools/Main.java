package com.dongnao.proxy_guard_tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Administrator on 2018/1/22 0022.
 */

public class Main {

    public static void main(String[] args) throws Exception {
        /**
         * 1、制作只包含解密代码的dex 文件
         */
        //1.1 解压aar 获得classes.jar
        File aarFile = new File("multiple-dex-core/build/outputs/aar/multiple-dex-core-debug.aar");
        File aarTemp = new File("proxy-guard-tools/temp");
        Zip.unZip(aarFile, aarTemp);
        File classesJar = new File(aarTemp, "classes.jar");
        //1.2 执行dx命令 将jar变成dex文件
        File classesDex = new File(aarTemp, "classes.dex");
        Process process = Runtime.getRuntime().exec("cmd /c D:/001_Programs/001_Android/002_Sdk/Sdk/build-tools/30.0.2/dx.bat --dex --output " + classesDex
                .getAbsolutePath() + " " +
                classesJar.getAbsolutePath());
        process.waitFor();
        //失败
        if (process.exitValue() != 0) {
            throw new RuntimeException("dex error");
        }

        File apkFile = new File("app/build/outputs/apk/debug/app-debug.apk");
        File apkTemp = new File("app/build/outputs/apk/debug/temp");
        Zip.unZip(apkFile, apkTemp);
        File[] dexFiles = apkTemp.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".dex");
            }
        });
        AES.init(AES.DEFAULT_PWD);
        for (File dex : dexFiles) {
            byte[] bytes = getBytes(dex);
            byte[] encrypt = AES.encrypt(bytes);
            FileOutputStream fos = new FileOutputStream(new File(apkTemp, "secret-"
                    + dex.getName()));
            fos.write(encrypt);
            fos.flush();
            fos.close();
            dex.delete();
        }

        classesDex.renameTo(new File(apkTemp, "classes.dex"));
        File unSignedApk = new File("app/build/outputs/apk/debug/app-unsigned.apk");
        Zip.zip(apkTemp, unSignedApk);

        File alignedApk = new File("app/build/outputs/apk/debug/app-unsigned-aligned.apk");
        process = Runtime.getRuntime().exec("cmd /c D:/001_Programs/001_Android/002_Sdk/Sdk/build-tools/30.0.2/zipalign -f 4 " + unSignedApk
                .getAbsolutePath() + " " +
                alignedApk.getAbsolutePath());
        process.waitFor();
        //失败
        if (process.exitValue() != 0) {
            throw new RuntimeException("zipalign error");
        }

        File signedApk = new File("app/build/outputs/apk/debug/app-signed-aligned.apk");
        File jks = new File("proxy-guard-tools/proxy.jks");
        process = Runtime.getRuntime().exec("cmd /c D:/001_Programs/001_Android/002_Sdk/Sdk/build-tools/30.0.2/apksigner sign  --ks " + jks.getAbsolutePath
                () + " --ks-key-alias lance --ks-pass pass:p123456 --key-pass  pass:p654321 --out" +
                " " + signedApk.getAbsolutePath() + " " + alignedApk.getAbsolutePath());
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("apksigner error");
        }

    }

    public static byte[] getBytes(File file) throws Exception {
        RandomAccessFile r = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) r.length()];
        r.readFully(buffer);
        r.close();
        return buffer;
    }
}
