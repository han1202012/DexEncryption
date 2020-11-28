package kim.hsl.multiple_dex_tools

import java.io.*
import java.util.zip.*

/**
 * 此处配置 SDK 根目录绝对路径
 * D:/001_Programs/001_Android/002_Sdk/Sdk/
 * Y:/001_DevelopTools/002_Android_SDK/
 */
val sdkDirectory = "Y:/001_DevelopTools/002_Android_SDK/"

@ExperimentalStdlibApi
fun main() {
    /*
        1 . 生成 dex 文件 , 该 dex 文件中只包含解密 其它 dex 的功能

        编译工程
        会生成 Android 依赖库的 aar 文件
        生成目录是 module/build/outputs/aar/ 目录下

        前提是需要在 菜单栏 / File / Setting / Build, Execution, Deployment / Compiler
        设置界面中 , 勾选 Compile independent modules in parallel (may require larger )

        将 D:\002_Project\002_Android_Learn\DexEncryption\multiple-dex-core\build\outputs\aar
        路径下的 multiple-dex-core-debug.aar 文件后缀修改为 .zip
        解压上述文件
        拿到 classes.jar 文件即可 ;
     */

    // 获取 multiple-dex-core-debug.aar 文件对象
    var aarFile = File("multiple-dex-core/build/outputs/aar/multiple-dex-core-debug.aar")

    // 解压上述 multiple-dex-core-debug.aar 文件到 aarUnzip 目录中
    // 创建解压目录
    var aarUnzip = File("multiple-dex-tools/aarUnzip")
    // 解压操作
    unZip(aarFile, aarUnzip)

    // 拿到 multiple-dex-core-debug.aar 中解压出来的 classes.jar 文件
    var classesJarFile = File(aarUnzip, "classes.jar")

    // 创建转换后的 dex 目的文件, 下面会开始创建该 dex 文件
    var classesDexFile = File(aarUnzip, "classes.dex")

    // 打印要执行的命令
    println("cmd /c ${sdkDirectory}build-tools/30.0.2/dx.bat --dex --output ${classesDexFile.absolutePath} ${classesJarFile.absolutePath}")

    /*
        将 jar 包变成 dex 文件
        使用 dx 工具命令

        注意 : Windows 命令行命令之前需要加上 "cmd /c " 信息 , Linux 与 MAC 命令行不用添加
     */
    var process = Runtime.getRuntime().exec("cmd /c ${sdkDirectory}build-tools/30.0.2/dx.bat --dex --output ${classesDexFile.absolutePath} ${classesJarFile.absolutePath}")
    // 等待上述命令执行完毕
    process.waitFor()

    // 执行结果提示
    if(process.exitValue() == 0){
        println("生成 dex 操作 , 执行成功");
    } else {
        println("生成 dex 操作 , 执行失败");
    }


    /*
        2 . 加密 apk 中的 dex 文件
     */

    // 解压 apk 文件 , 获取所有的 dex 文件

    // 被解压的 apk 文件
    var apkFile = File("app/build/outputs/apk/debug/app-debug.apk")
    // 解压的目标文件夹
    var apkUnZipFile = File("app/build/outputs/apk/debug/unZipFile")

    // 解压文件
    unZip(apkFile, apkUnZipFile)

    // 从被解压的 apk 文件中找到所有的 dex 文件, 小项目只有 1 个, 大项目可能有多个
    // 使用文件过滤器获取后缀是 .dex 的文件
    var dexFiles : Array<File> = apkUnZipFile.listFiles({ file: File, s: String ->
        s.endsWith(".dex")
    })

    // 加密找到的 dex 文件
    var aes = AES(AES.DEFAULT_PWD)
    // 遍历 dex 文件
    for(dexFile: File in dexFiles){
        // 读取文件数据
        var bytes = getBytes(dexFile)
        // 加密文件数据
        var encryptedBytes = aes.encrypt(bytes)

        // 将加密后的数据写出到指定目录
        var outputFile = File(apkUnZipFile, "secret-${dexFile.name}")
        // 创建对应输出流
        var fileOutputStream = FileOutputStream(outputFile)

        // 将加密后的 dex 文件写出, 然后刷写 , 关闭该输出流
        fileOutputStream.write(encryptedBytes)
        fileOutputStream.flush()
        fileOutputStream.close()

        // 删除原来的文件
        dexFile.delete()
    }


    /*
        3 . 将代理 Application 中的 classes.dex 解压到上述
            app/build/outputs/apk/debug/unZipFile 目录中
     */
    // 拷贝文件到 app/build/outputs/apk/debug/unZipFile 目录中
    classesDexFile.renameTo(File(apkUnZipFile, "classes.dex"))

    // 压缩打包 , 该压缩包是未签名的压缩包
    var unSignedApk = File("app/build/outputs/apk/debug/app-unsigned.apk")
    // 压缩打包操作
    zip(apkUnZipFile, unSignedApk)


    /*
        4 . 对齐操作
     */
    // 对齐操作的输出结果, 将 app-unsigned.apk 对齐, 对齐后的文件输出到 app-unsigned-aligned.apk 中
    var unSignedAlignApk = File("app/build/outputs/apk/debug/app-unsigned-aligned.apk")

    // 打印要执行的命令
    println("cmd /c ${sdkDirectory}build-tools/30.0.2/zipalign -f 4 ${unSignedApk.absolutePath} ${unSignedAlignApk.absolutePath}")

    /*
        将 app-unsigned.apk 对齐
        使用 zipalign 工具命令

        注意 : Windows 命令行命令之前需要加上 "cmd /c " 信息 , Linux 与 MAC 命令行不用添加
     */
    process = Runtime.getRuntime().exec("cmd /c ${sdkDirectory}build-tools/30.0.2/zipalign -f 4 ${unSignedApk.absolutePath} ${unSignedAlignApk.absolutePath}")
    // 等待上述命令执行完毕
    process.waitFor()

    // 执行结果提示
    if(process.exitValue() == 0){
        println("对齐操作 执行成功");
    } else {
        println("对齐操作 执行失败");
    }



    /*
        5 . 签名操作
     */
    // 签名 apk 输出结果, 将 app-unsigned-aligned.apk 签名, 签名后的文件输出到 app-signed-aligned.apk 中
    var signedAlignApk = File("app/build/outputs/apk/debug/app-signed-aligned.apk")

    // 获取签名 jks 文件
    var jksFile = File("dex.jks")

    // 打印要执行的命令
    println("cmd /c ${sdkDirectory}build-tools/30.0.2/apksigner sign --ks ${jksFile.absolutePath} --ks-key-alias Key0 --ks-pass pass:000000 --key-pass pass:000000 --out ${signedAlignApk.absolutePath} ${unSignedAlignApk.absolutePath}")

    /*
        将 app-unsigned.apk 对齐
        使用 zipalign 工具命令

        注意 : Windows 命令行命令之前需要加上 "cmd /c " 信息 , Linux 与 MAC 命令行不用添加
     */
    process = Runtime.getRuntime().exec("cmd /c ${sdkDirectory}build-tools/30.0.2/apksigner sign --ks ${jksFile.absolutePath} --ks-key-alias Key0 --ks-pass pass:000000 --key-pass pass:000000 --out ${signedAlignApk.absolutePath} ${unSignedAlignApk.absolutePath}")

    // 打印错误日志
    var br = BufferedReader(InputStreamReader(process.errorStream))
    while ( true ){
        var line = br.readLine()
        if(line == null){
            break
        }else{
            println(line)
        }
    }
    br.close()

    // 等待上述命令执行完毕
    process.waitFor()

    // 执行结果提示
    if(process.exitValue() == 0){
        println("签名操作 执行成功");
    } else {
        println("签名操作 执行失败");
    }




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
fun unZip(zip: File, dir: File) {
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

fun zip(dir: File, zip: File) {
    // 如果目标压缩包存在 , 删除该压缩包
    zip.delete()

    // 对输出文件做 CRC32 校验
    val cos = CheckedOutputStream(FileOutputStream(
            zip), CRC32())
    val zos = ZipOutputStream(cos)

    // 压缩文件
    compress(dir, zos, "")
    zos.flush()
    zos.close()
}

private fun compress(srcFile: File, zos: ZipOutputStream, basePath: String) {
    if (srcFile.isDirectory) {
        val files = srcFile.listFiles()
        for (file in files) {
            // zip 递归添加目录中的文件
            compress(file, zos, basePath + srcFile.name + "/")
        }
    } else {
        compressFile(srcFile, zos, basePath)
    }
}


private fun compressFile(file: File, zos: ZipOutputStream, dir: String) {
    // 拼接完整的文件路径名称
    val fullName = dir + file.name

    // app/build/outputs/apk/debug/unZipFile 路径
    val fileNames = fullName.split("/").toTypedArray()

    // 正确的文件目录名
    val sb = StringBuffer()
    if (fileNames.size > 1) {
        for (i in 1 until fileNames.size) {
            sb.append("/")
            sb.append(fileNames[i])
        }
    } else {
        sb.append("/")
    }

    // 添加 zip 条目
    val entry = ZipEntry(sb.substring(1))
    zos.putNextEntry(entry)

    // 读取 zip 条目输出到文件中
    val fis = FileInputStream(file)
    var len: Int
    val data = ByteArray(2048)
    while (fis.read(data, 0, 2048).also { len = it } != -1) {
        zos.write(data, 0, len)
    }
    fis.close()
    zos.closeEntry()
}


/**
 * 读取文件到数组中
 */
fun getBytes(file: File): ByteArray {
    // 创建随机方位文件对象
    val randomAccessFile = RandomAccessFile(file, "r")
    // 获取文件大小 , 并创建同样大小的数据组
    val buffer = ByteArray(randomAccessFile.length().toInt())
    // 读取真个文件到数组中
    randomAccessFile.readFully(buffer)
    // 关闭文件
    randomAccessFile.close()
    return buffer
}