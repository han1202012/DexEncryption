package kim.hsl.multipledex

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.zip.ZipFile
import kotlin.collections.ArrayList


class ProxyApplication : Application() {

    /**
     * 应用真实的 Application 全类名
     */
    lateinit var app_name : String

    /**
     * DEX 解密之后的目录名称
     */
    lateinit var app_version : String

    /**
     * 在 Application 在 ActivityThread 中被创建之后,
     * 第一个调用的方法是 attachBaseContext 函数.
     * 该函数是 Application 中最先执行的函数.
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

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
        val apkFile = File(applicationInfo.sourceDir)

        // 获取在 app Module 下的 AndroidManifest.xml 中配置的元数据,
        // 应用真实的 Application 全类名
        // 解密后的 dex 文件存放目录
        var applicationInfo : ApplicationInfo = packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
        );
        var metaData : Bundle = applicationInfo.metaData
        if(metaData != null){
            // 检查是否存在 app_name 元数据
            if(metaData.containsKey("app_name")){
                app_name = metaData.getString("app_name").toString()
            }
            // 检查是否存在 app_version 元数据
            if(metaData.containsKey("app_version")){
                app_version = metaData.getString("app_version").toString()
            }
        }

        // 创建用户的私有目录 , 将 apk 文件解压到该目录中
        var privateDir : File = getDir("${app_name}_${app_version}", MODE_PRIVATE)

        // 在上述目录下创建 app 目录
        // 创建该目录的目的是存放解压后的 apk 文件的
        var appDir : File = File(privateDir, "app")

        // app 中存放的是解压后的所有的 apk 文件
        // app 下创建 dexDir 目录 , 将所有的 dex 目录移动到该 deDir 目录中
        // dexDir 目录存放应用的所有 dex 文件
        // 这些 dex 文件都需要进行解密
        var dexDir : File = File(appDir, "dexDir")

        // 遍历解压后的 apk 文件 , 将需要加载的 dex 放入如下集合中
        var dexFiles : ArrayList<File> = ArrayList<File>()

        // 如果该 dexDir 存在 , 并且该目录不为空 , 并进行 MD5 文件校验
        if( !dexDir.exists() || dexDir.list().size == 0){
            // 将 apk 中的文件解压到了 appDir 目录
            unZipApk(apkFile, appDir)

            // 获取 appDir 目录下的所有文件
            var files = appDir.listFiles()

            // 遍历文件名称集合
            for(i in files.indices){
                var file = files[i]
                // 如果文件后缀是 .dex , 并且不是 主 dex 文件 classes.dex
                // 符合上述两个条件的 dex 文件放入到 dexDir 中
                if(file.name.endsWith(".dex") &&
                    TextUtils.equals(file.name, "classes.dex")){
                    // 筛选出来的 dex 文件都是需要解密的
                    // 解密需要使用 OpenSSL 进行解密

                    // 获取该文件的二进制 Byte 数据
                    // 这些 Byte 数组就是加密后的 dex 数据
                    var bytes = Utils.getBytes(file)

                    // 解密该二进制数据, 并替换原来的加密 dex, 直接覆盖原来的文件即可
                    Utils.decrypt(bytes, file.absolutePath)

                    // 将解密完毕的 dex 文件放在需要加载的 dex 集合中
                    dexFiles.add(file)

                }// 判定是否是需要解密的 dex 文件
            }// 遍历 apk 解压后的文件

        }else{
            // 已经解密完成, 此时不需要解密, 直接获取 dexDir 中的文件即可
            for (file in dexDir.listFiles()) {
                dexFiles.add(file)
            }
        }

        // 截止到此处 , 已经拿到了解密完毕 , 需要加载的 dex 文件
        // 加载自己解密的 dex 文件
        loadDex(dexFiles)
    }

    /**
     * 加载 dex 文件集合
     * 这些 dex 文件已经解密
     * 参考博客 : https://hanshuliang.blog.csdn.net/article/details/109608605
     *
     * 创建自己的 Element[] dexElements 数组
     * ( libcore/dalvik/src/main/java/dalvik/system/DexPathList.java )
     * 然后将 系统加载的 Element[] dexElements 数组 与 我们自己的 Element[] dexElements 数组进行合并操作
     * 00:17:07
     */
    fun loadDex(dexList: ArrayList<File>) : Unit{

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
        var pathListField = reflexField(classLoader, "DexPathList");
        // 获取 classLoader 对象对应的 DexPathList pathList 成员
        var pathList = pathListField.get(classLoader)

        //阶段三 : 获取封装在 DexPathList 类中的 Element[] dexElements 数组
        var dexElementsField = reflexField(pathList, "dexElements")
        // 获取 pathList 对象对应的 Element[] dexElements 数组成员
        var dexElements : Array<Any> = dexElementsField.get(pathList) as Array<Any>



        /*
            2 . 在本应用中创建 Element[] dexElements 数组 , 用于存放解密后的 dex 文件
                不同的 Android 版本中 , 创建 Element[] dexElements 数组的方法不同 , 这里需要做兼容

         */
        //00:27:32



    }


    /**
     * 通过反射方法获取 instance 类中的 memberName 名称的成员
     */
    fun reflexField(instance: Any, memberName: String): Field {

        // 获取字节码类
        var clazz: Class<*>? = instance.javaClass

        // 循环通过反射获取
        // 可能存在通过反射没有找到成员的情况 , 此时查找其父类是否有该成员
        // 循环次数就是其父类层级个数
        while (clazz != null) {
            try {
                // 获取成员
                val memberField = clazz.getDeclaredField(memberName)

                // 如果不是 public , 无法访问 , 设置可访问
                if (!memberField.isAccessible) {
                    memberField.isAccessible = true
                }
                return memberField
            } catch (e: NoSuchFieldException) {
                // 如果找不到, 就到父类中查找
                clazz = clazz.superclass
            }
        }

        // 如果没有拿到成员 , 则直接中断程序 , 加载无法进行下去
        throw NoSuchFieldException("没有在 ${instance.javaClass} 类中找到 $memberName 成员")
    }


    /**
     * 通过反射方法获取 instance 类中的 参数为 parameterTypes , 名称为 methodName 的成员方法
     */
    fun reflexMethod(instance: Any, methodName: String, vararg parameterTypes: Class<*>?): Method {

        // 获取字节码类
        var clazz: Class<*>? = instance.javaClass

        // 循环通过反射获取
        // 可能存在通过反射没有找到成员方法的情况 , 此时查找其父类是否有该成员方法
        // 循环次数就是其父类层级个数
        while (clazz != null) {
            try {
                // 获取成员方法
                val method = clazz.getDeclaredMethod(methodName, *parameterTypes)

                // 如果不是 public , 无法访问 , 设置可访问
                if (!method.isAccessible) {
                    method.isAccessible = true
                }
                return method
            } catch (e: NoSuchMethodException) {
                // 如果找不到, 就到父类中查找
                clazz = clazz.superclass
            }
        }

        // 如果没有拿到成员 , 则直接中断程序 , 加载无法进行下去
        throw NoSuchMethodException("没有在 ${instance.javaClass} 类中找到 $methodName 成员方法")
    }


    /**
     * 删除文件, 如果有目录, 则递归删除
     */
    private fun deleteFile(file: File) {
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                deleteFile(f)
            }
        } else {
            file.delete()
        }
    }

    /**
     * 解压文件
     * @param zip 被解压的压缩包文件
     * @param dir 解压后的文件存放目录
     */
    fun unZipApk(zip: File, dir: File) {
        try {
            // 如果存放文件目录存在, 删除该目录
            deleteFile(dir)
            // 获取 zip 压缩包文件
            val zipFile = ZipFile(zip)
            // 获取 zip 压缩包中每一个文件条目
            val entries = zipFile.entries()
            // 遍历压缩包中的文件
            while (entries.hasMoreElements()) {
                val zipEntry = entries.nextElement()
                // zip 压缩包中的文件名称 或 目录名称
                val name = zipEntry.name
                // 如果 apk 压缩包中含有以下文件 , 这些文件是 V1 签名文件保存目录 , 不需要解压 , 跳过即可
                if (name == "META-INF/CERT.RSA" || name == "META-INF/CERT.SF" || (name
                            == "META-INF/MANIFEST.MF")
                ) {
                    continue
                }
                // 如果该文件条目 , 不是目录 , 说明就是文件
                if (!zipEntry.isDirectory) {
                    val file = File(dir, name)
                    // 创建目录
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    // 向刚才创建的目录中写出文件
                    val fileOutputStream = FileOutputStream(file)
                    val inputStream = zipFile.getInputStream(zipEntry)
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        fileOutputStream.write(buffer, 0, len)
                    }
                    inputStream.close()
                    fileOutputStream.close()
                }
            }

            // 关闭 zip 文件
            zipFile.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}