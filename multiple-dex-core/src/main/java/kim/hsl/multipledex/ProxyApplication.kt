package kim.hsl.multipledex

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import java.io.File

class ProxyApplication : Application() {

    /**
     * 应用真实的 Application 全类名
     */
    lateinit var app_name : String

    /**
     * DEX 解密之后的目录名称
     */
    lateinit var app_directory : String

    /**
     * 在 Application 在 ActivityThread 中被创建之后,
     * 一个调用的方法是 attachBaseContext 函数.
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
                2.
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
            // 检查是否存在 app_directory 元数据
            if(metaData.containsKey("app_directory")){
                app_directory = metaData.getString("app_directory").toString()
            }
        }

        // 创建用户的私有目录 , 将 apk 文件解压到该目录中
        var privateDir : File = getDir("${app_name}_${app_directory}", MODE_PRIVATE)

        // 在上述目录下创建 app 目录
        // 创建该目录的目的是存放解压后的 apk 文件的
        var appDir : File = File(privateDir, "app")

        // app 中存放的是解压后的所有的 apk 文件
        // app 下创建 dexDir 目录 , 将所有的 dex 目录移动到该 deDir 目录中
        var dexDir : File = File(appDir, "dexDir")
    }

}