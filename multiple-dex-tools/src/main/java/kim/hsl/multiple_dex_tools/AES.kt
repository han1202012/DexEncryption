package kim.hsl.multiple_dex_tools

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

}