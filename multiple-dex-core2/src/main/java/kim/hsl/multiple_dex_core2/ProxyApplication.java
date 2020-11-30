package kim.hsl.multiple_dex_core2;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/22 0022.
 */
//解密与加载多个dex
//替换真实的Application
public class ProxyApplication extends Application {

    private static final String TAG = "ProxyApplication";
    private String app_name;
    private String app_version;

    /**
     * ActivityThread 创建Application之后调用的第一个函数
     *
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        getMetaData();
        //获得当前的apk文件
        File apkFile = new File(getApplicationInfo().sourceDir);
        //apk zip 解压到 appDir这个目录 /data/data/packagename/
        File versionDir = getDir(app_name + "_" + app_version, MODE_PRIVATE);

        //data/user/0/com.dongnao.proxyguard2/app_com.dongnao.proxyguard2.MyApplication_1.0
        Log.i(TAG, "获取路径 versionDir : " + versionDir.getAbsolutePath());

        File appDir = new File(versionDir, "app");
        //提取apk中 需要解密的所有dex放入到这个目录
        File dexDir = new File(appDir, "dexDir");
        //需要我们加载的dex
        List<File> dexFiles = new ArrayList<>();
        //需要解密 (MD5 文件校验)
        if (!dexDir.exists() || dexDir.list().length == 0) {
            //把apk解压 到 appDir
            Zip.unZip(apkFile, appDir);
            //获取目录下的所有文件
            File[] files = appDir.listFiles();
            for (File file : files) {
                String name = file.getName();
                //文件名是 .dex结尾， 并且不是主dex 放入 dexDir 目录
                if (name.endsWith(".dex") && !TextUtils.equals(name, "classes.dex")) {
                    try {
                        //从文件中读取 byte数组 加密后的dex数据
                        byte[] bytes = Utils.getBytes(file);
                        //将dex 文件 解密 并且写入 原文件file目录
                        Utils.decrypt(bytes, file.getAbsolutePath());
                        dexFiles.add(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            //已经解密过了
            for (File file : dexDir.listFiles()) {
                dexFiles.add(file);
            }
        }
        try {
            loadDex(dexFiles,versionDir);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "attachBaseContext 执行完毕");
    }

    /**
     * 加载dex文件集合
     *
     * @param dexFiles
     */
    private void loadDex(List<File> dexFiles, File optimizedDirectory) throws
            NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {

        Log.i(TAG, "loadDex");

        /**
         * 1.获得 系统 classloader中的dexElements数组
         */
        //1.1  获得classloader中的pathList => DexPathList
        Field pathListField = Utils.findField(getClassLoader(), "pathList");
        Object pathList = pathListField.get(getClassLoader());
        //1.2 获得pathList类中的 dexElements
        Field dexElementsField = Utils.findField(pathList, "dexElements");
        Object[] dexElements = (Object[]) dexElementsField.get(pathList);
        /**
         * 2.创建新的 element 数组 -- 解密后加载dex
         */
        //5.x 需要适配6.x 7.x
        Method makeDexElements= null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <
                Build.VERSION_CODES.M) {
             makeDexElements = Utils.findMethod(pathList, "makeDexElements", ArrayList.class,
                    File.class, ArrayList.class);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            makeDexElements = Utils.findMethod(pathList, "makePathElements", List.class,
                    File.class, List.class);
        }
        makeDexElements.setAccessible(true);
        ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
        Object[] addElements = (Object[]) makeDexElements.invoke(pathList, dexFiles,
                optimizedDirectory,
                suppressedExceptions);
        /**
         * 3.合并两个数组
         */
        //创建一个数组
        Object[] newElements = (Object[]) Array.newInstance(dexElements.getClass()
                .getComponentType(), dexElements.length +
                addElements.length);
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);
        System.arraycopy(addElements, 0, newElements, dexElements.length, addElements.length);
        /**
         * 4.替换classloader中的 element数组
         */
        dexElementsField.set(pathList, newElements);
    }

    public void getMetaData() {
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo
                    (getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            //是否设置app_name 与 app_version
            if (null != metaData) {
                //是否存在name为app_name的meta-data数据
                if (metaData.containsKey("app_name")) {
                    app_name = metaData.getString("app_name");
                }
                if (metaData.containsKey("app_version")) {
                    app_version = metaData.getString("app_version");
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
