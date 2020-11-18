package kim.hsl.multiple_dex_tools

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AES {

    /**
     * 加密密钥, 16 字节
     */
    val DEFAULT_PWD = "kimhslmultiplede"

    /**
     * 加密解密算法类型
     */
    val algorithm = "AES/ECB/PKCS5Padding"

    /**
     * 加密算法, 目前本应用中只需要加密, 不需要解密
     */
    lateinit var encryptCipher: Cipher;

    /**
     * 解密算法
     */
    lateinit var decryptCipher: Cipher;

    @ExperimentalStdlibApi
    constructor(pwd: String){
        // 初始化加密算法
        encryptCipher = Cipher.getInstance(algorithm)
        // 初始化解密算法
        decryptCipher = Cipher.getInstance(algorithm)

        // 将密钥字符串转为字节数组
        var keyByte = pwd.toByteArray()
        // 创建密钥
        val key = SecretKeySpec(keyByte, "AES")

        // 设置算法类型, 及密钥
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        // 设置算法类型, 及密钥
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }

    /**
     * 加密操作
     */
    fun encrypt(contet : ByteArray) : ByteArray{
        var result : ByteArray = encryptCipher.doFinal(contet)
        return  result
    }

    /**
     * 解密操作
     */
    fun decrypt(contet : ByteArray) : ByteArray{
        var result : ByteArray = decryptCipher.doFinal(contet)
        return  result
    }

}


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
    unZipAar(aarFile, aarUnzip)

    // 拿到 multiple-dex-core-debug.aar 中解压出来的 classes.jar 文件
    var classesJarFile = File(aarUnzip, "classes.jar")

    /*
        将 jar 包变成 dex 文件 
        使用 dx 工具命令
        00:08:44
     */
    Runtime.getRuntime().exec("")





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
fun unZipAar(zip: File, dir: File) {
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