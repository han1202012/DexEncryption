package kim.hsl.multipledex;

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

public class ProxyApplication extends Application {
    public static final String TAG = "ProxyApplication";

    /**
     * 应用真实的 Application 全类名
     */
    String app_name;

    /**
     * DEX 解密之后的目录名称
     */
    String app_version;

    /**
     * 在 Application 在 ActivityThread 中被创建之后,
     * 第一个调用的方法是 attachBaseContext 函数.
     * 该函数是 Application 中最先执行的函数.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            Log.i(TAG, "attachBaseContext");

            /*
                在该 Application 中主要进行两个操作 :
                1 . 解密并加载多个 DEX 文件
                2 . 将真实的 Application 替换成应用的主 Application
             */


            /*
                I . 解密与加载多 DEX 文件
                    先进行解密, 然后再加载解密之后的 DEX 文件

                    1. 先获取当前的 APK 文件
                    2. 然后解压该 APK 文件
             */

            // 获取当前的 APK 文件, 下面的 getApplicationInfo().sourceDir 就是本应用 APK 安装文件的全路径
            File apkFile = new File(getApplicationInfo().sourceDir);

            // 获取在 app Module 下的 AndroidManifest.xml 中配置的元数据,
            // 应用真实的 Application 全类名
            // 解密后的 dex 文件存放目录
            ApplicationInfo applicationInfo = null;

            applicationInfo = getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.GET_META_DATA
            );

            Bundle metaData = applicationInfo.metaData;
            if (metaData != null) {
                // 检查是否存在 app_name 元数据
                if (metaData.containsKey("app_name")) {
                    app_name = metaData.getString("app_name").toString();
                }
                // 检查是否存在 app_version 元数据
                if (metaData.containsKey("app_version")) {
                    app_version = metaData.getString("app_version").toString();
                }
            }

            // 创建用户的私有目录 , 将 apk 文件解压到该目录中
            File privateDir = getDir(app_name + "_" + app_version, MODE_PRIVATE);

            Log.i(TAG, "attachBaseContext 创建用户的私有目录 : " + privateDir.getAbsolutePath());

            // 在上述目录下创建 app 目录
            // 创建该目录的目的是存放解压后的 apk 文件的
            File appDir = new File(privateDir, "app");

            // app 中存放的是解压后的所有的 apk 文件
            // app 下创建 dexDir 目录 , 将所有的 dex 目录移动到该 dexDir 目录中
            // dexDir 目录存放应用的所有 dex 文件
            // 这些 dex 文件都需要进行解密
            File dexDir = new File(appDir, "dexDir");

            // 遍历解压后的 apk 文件 , 将需要加载的 dex 放入如下集合中
            ArrayList<File> dexFiles = new ArrayList<File>();

            // 如果该 dexDir 不存在 , 或者该目录为空 , 并进行 MD5 文件校验
            if (!dexDir.exists() || dexDir.list().length == 0) {
                // 将 apk 中的文件解压到了 appDir 目录
                ZipUtils.unZipApk(apkFile, appDir);


                // 获取 appDir 目录下的所有文件
                File[] files = appDir.listFiles();

                Log.i(TAG, "attachBaseContext appDir 目录路径 : " + appDir.getAbsolutePath());
                Log.i(TAG, "attachBaseContext appDir 目录内容 : " + files);

                // 遍历文件名称集合
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];

                    Log.i(TAG, "attachBaseContext 遍历 " + i + " . " + file);

                    // 如果文件后缀是 .dex , 并且不是 主 dex 文件 classes.dex
                    // 符合上述两个条件的 dex 文件放入到 dexDir 中
                    if (file.getName().endsWith(".dex") &&
                            !TextUtils.equals(file.getName(), "classes.dex")) {
                        // 筛选出来的 dex 文件都是需要解密的
                        // 解密需要使用 OpenSSL 进行解密

                        // 获取该文件的二进制 Byte 数据
                        // 这些 Byte 数组就是加密后的 dex 数据
                        byte[] bytes = OpenSSL.getBytes(file);

                        // 解密该二进制数据, 并替换原来的加密 dex, 直接覆盖原来的文件即可
                        OpenSSL.decrypt(bytes, file.getAbsolutePath());

                        // 将解密完毕的 dex 文件放在需要加载的 dex 集合中
                        dexFiles.add(file);

                        // 拷贝到 dexDir 中

                        Log.i(TAG, "attachBaseContext 解密完成 被解密文件是 : " + file);

                    }// 判定是否是需要解密的 dex 文件
                }// 遍历 apk 解压后的文件

            } else {
                // 已经解密完成, 此时不需要解密, 直接获取 dexDir 中的文件即可
                for (File file : dexDir.listFiles()) {
                    dexFiles.add(file);
                }
            }

            Log.i(TAG, "attachBaseContext 解密完成 dexFiles : " + dexFiles);

            for (int i = 0; i < dexFiles.size(); i++) {
                Log.i(TAG, i + " . " + dexFiles.get(i).getAbsolutePath());
            }

            // 截止到此处 , 已经拿到了解密完毕 , 需要加载的 dex 文件
            // 加载自己解密的 dex 文件
            loadDex(dexFiles, privateDir);

            Log.i(TAG, "attachBaseContext 完成");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载 dex 文件集合
     * 这些 dex 文件已经解密
     * 参考博客 : https://hanshuliang.blog.csdn.net/article/details/109608605
     * <p>
     * 创建自己的 Element[] dexElements 数组
     * ( libcore/dalvik/src/main/java/dalvik/system/DexPathList.java )
     * 然后将 系统加载的 Element[] dexElements 数组 与 我们自己的 Element[] dexElements 数组进行合并操作
     */
    void loadDex(ArrayList<File> dexFiles, File optimizedDirectory)
            throws
            IllegalAccessException,
            InvocationTargetException,
            NoSuchFieldException,
            NoSuchMethodException {
        Log.i(TAG, "loadDex");
        /*
            需要执行的步骤
            1 . 获得系统 DexPathList 中的 Element[] dexElements 数组
                ( libcore/dalvik/src/main/java/dalvik/system/DexPathList.java )
            2 . 在本应用中创建 Element[] dexElements 数组 , 用于存放解密后的 dex 文件
            3 . 将 系统加载的 Element[] dexElements 数组
                与 我们自己的 Element[] dexElements 数组进行合并操作
            4 . 替换 ClassLoader 加载过程中的 Element[] dexElements 数组 ( 封装在 DexPathList 中 )
         */


        /*
            1 . 获得系统 DexPathList 中的 Element[] dexElements 数组

            第一阶段 : 在 Context 中调用 getClassLoader() 方法 , 可以拿到 PathClassLoader ;

            第二阶段 : 从 PathClassLoader 父类 BaseDexClassLoader 中找到 DexPathList ;

            第三阶段 : 获取封装在 DexPathList 类中的 Element[] dexElements 数组 ;

            上述的 DexPathList 对象 是 BaseDexClassLoader 的私有成员
            Element[] dexElements 数组 也是 DexPathList 的私有成员
            因此只能使用反射获取 Element[] dexElements 数组
         */

        // 阶段一二 : 调用 getClassLoader() 方法可以获取 PathClassLoader 对象
        // 从 PathClassLoader 对象中获取 private final DexPathList pathList 成员
        Field pathListField = ReflexUtils.reflexField(getClassLoader(), "pathList");
        // 获取 classLoader 对象对应的 DexPathList pathList 成员
        Object pathList = pathListField.get(getClassLoader());

        //阶段三 : 获取封装在 DexPathList 类中的 Element[] dexElements 数组
        Field dexElementsField = ReflexUtils.reflexField(pathList, "dexElements");
        // 获取 pathList 对象对应的 Element[] dexElements 数组成员
        Object[] dexElements = (Object[]) dexElementsField.get(pathList);



        /*
            2 . 在本应用中创建 Element[] dexElements 数组 , 用于存放解密后的 dex 文件
                不同的 Android 版本中 , 创建 Element[] dexElements 数组的方法不同 , 这里需要做兼容

         */
        Method makeDexElements;
        Object[] addElements = null;

        if (Build.VERSION.SDK_INT <=
                Build.VERSION_CODES.M) { // 5.0, 5.1  makeDexElements

            // 反射 5.0, 5.1, 6.0 版本的 DexPathList 中的 makeDexElements 方法
            makeDexElements = ReflexUtils.reflexMethod(
                    pathList, "makeDexElements",
                    ArrayList.class, File.class, ArrayList.class);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            addElements = (Object[]) makeDexElements.invoke(pathList, dexFiles,
                    optimizedDirectory,
                    suppressedExceptions);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 7.0 以上版本 makePathElements

            // 反射 7.0 以上版本的 DexPathList 中的 makeDexElements 方法
            makeDexElements = ReflexUtils.reflexMethod(pathList, "makePathElements",
                    List.class, File.class, List.class);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            addElements = (Object[]) makeDexElements.invoke(pathList, dexFiles,
                    optimizedDirectory,
                    suppressedExceptions);

        }

        /*
            3 . 将 系统加载的 Element[] dexElements 数组
                与 我们自己的 Element[] dexElements 数组进行合并操作

            首先创建数组 , 数组类型与 dexElements 数组类型相同
            将 dexElements 数组中的元素拷贝到 newElements 前半部分, 拷贝元素个数是 dexElements.size
            将 addElements 数组中的元素拷贝到 newElements 后半部分, 拷贝元素个数是 dexElements.size
         */
        Object[] newElements = (Object[]) Array.newInstance(
                dexElements.getClass().getComponentType(),
                dexElements.length + addElements.length);

        // 将 dexElements 数组中的元素拷贝到 newElements 前半部分, 拷贝元素个数是 dexElements.size
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);

        // 将 addElements 数组中的元素拷贝到 newElements 后半部分, 拷贝元素个数是 dexElements.size
        System.arraycopy(addElements, 0, newElements, dexElements.length, addElements.length);


        /*
            4 . 替换 ClassLoader 加载过程中的 Element[] dexElements 数组 ( 封装在 DexPathList 中 )
         */
        dexElementsField.set(pathList, newElements);

        Log.i(TAG, "loadDex 完成");

    }


    @Override
    public void onCreate() {
        super.onCreate();

        try {

        /*
            在此处进行 Application 替换
         */

            // 先判断是否有配置 Application ,
            // 那么在 Manifest.xml 中的 meta-data 元数据 app_name 不为空
            // 如果开发者没有自定义 Application , 没有配置元数据 , 直接退出
            if (TextUtils.isEmpty(app_name)) {
                return;
            }

            // 获取上下文对象 , 保存下来 , 之后要使用
            Context baseContext = getBaseContext();

            // 通过反射获取 Application , 系统也是进行的反射操作
            Class<?> delegateClass = Class.forName(app_name);

            // 创建用户真实配置的 Application
            Application delegate = (Application) delegateClass.newInstance();

            // 调用 Application 的 attach 函数
            // 该函数无法直接调用 , 也需要通过反射调用
            // 这里先通过反射获取 Application 的 attach 函数
            Method attach = Application.class.getDeclaredMethod("attach", Context.class);
            // attach 方法是私有的 , 设置 attach 方法允许访问
            attach.setAccessible(true);

            // 获取上下文对象 ,
            // 该 Context 是通过调用 Application 的 attachBaseContext 方法传入的 ContextImpl
            // 将该上下文对象传入 Application 的 attach 方法中
            attach.invoke(delegate, baseContext);


            /*
                截止到此处, Application 创建完毕 , 下面开始逐个替换下面的 Application

                ① ContextImpl 的 private Context mOuterContext
                    成员是 kim.hsl.multipledex.ProxyApplication 对象 ;

                ② ActivityThread 中的 ArrayList<Application> mAllApplications
                    集合中添加了 kim.hsl.multipledex.ProxyApplication 对象 ;

                ③ LoadedApk 中的 mApplication 成员是 kim.hsl.multipledex.ProxyApplication 对象 ;

                ④ ActivityThread 中的 Application mInitialApplication
                    成员是 kim.hsl.multipledex.ProxyApplication 对象 ;
             */

            // I . 替换 ① ContextImpl 的 private Context mOuterContext
            //  成员是 kim.hsl.multipledex.ProxyApplication 对象
            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
            // 获取 ContextImpl 中的 mOuterContext 成员
            Field mOuterContextField = contextImplClass.getDeclaredField("mOuterContext");
            // mOuterContext 成员是私有的 , 设置可访问性
            mOuterContextField.setAccessible(true);
            // ContextImpl 就是应用的 Context , 直接通过 getBaseContext() 获取即可
            mOuterContextField.set(baseContext, delegate);




            // II . 替换 ④ ActivityThread 中的 Application mInitialApplication
            //                    成员是 kim.hsl.multipledex.ProxyApplication 对象 ;
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            // 获取 ActivityThread 中的 mInitialApplication 成员
            Field mInitialApplicationField =
                    activityThreadClass.getDeclaredField("mInitialApplication");
            // mInitialApplication 成员是私有的 , 设置可访问性
            mInitialApplicationField.setAccessible(true);

            // 从 ContextImpl 对象中获取其 ActivityThread mMainThread 成员变量
            Field mMainThreadField = contextImplClass.getDeclaredField("mMainThread");
            mMainThreadField.setAccessible(true);
            // ContextImpl 就是本应用的上下文对象 , 调用 getBaseContext 方法获得
            Object mMainThread = mMainThreadField.get(baseContext);

            // ContextImpl 就是应用的 Context , 直接通过 getBaseContext() 获取即可
            mInitialApplicationField.set(mMainThread, delegate);



            // III . 替换 ② ActivityThread 中的 ArrayList<Application> mAllApplications
            //                    集合中添加了 kim.hsl.multipledex.ProxyApplication 对象 ;

            // 获取 ActivityThread 中的 mAllApplications 成员
            Field mAllApplicationsField =
                    activityThreadClass.getDeclaredField("mAllApplications");
            // mAllApplications 成员是私有的 , 设置可访问性
            mAllApplicationsField.setAccessible(true);

            // 获取 ActivityThread 中的 ArrayList<Application> mAllApplications 队列
            ArrayList<Application> mAllApplications =
                    (ArrayList<Application>) mAllApplicationsField.get(mMainThread);
            // 将真实的 Application 添加到上述队列中
            mAllApplications.add(delegate);



            // IV . 替换 ③ LoadedApk 中的 mApplication
            //          成员是 kim.hsl.multipledex.ProxyApplication 对象

            // 1. 先获取 LoadedApk 对象
            // LoadedApk 是 ContextImpl 中的 LoadedApk mPackageInfo 成员变量
            // 从 ContextImpl 对象中获取其 LoadedApk mPackageInfo 成员变量
            Field mPackageInfoField = contextImplClass.getDeclaredField("mPackageInfo");
            mPackageInfoField.setAccessible(true);
            // ContextImpl 就是本应用的上下文对象 , 调用 getBaseContext 方法获得
            Object mPackageInfo = mPackageInfoField.get(baseContext);

            // 2. 获取 LoadedApk 对象中的 mApplication 成员
            Class<?> loadedApkClass = Class.forName("android.app.LoadedApk");
            // 获取 ActivityThread 中的 mInitialApplication 成员
            Field mApplicationField =
                    loadedApkClass.getDeclaredField("mApplication");
            // LoadedApk 中的 mApplication 成员是私有的 , 设置可访问性
            mApplicationField.setAccessible(true);

            // 3. 将 Application 设置给 LoadedApk 中的 mApplication 成员
            mApplicationField.set(mPackageInfo, delegate);



            // 再次调用 onCreate 方法
            delegate.onCreate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }


    }
}
