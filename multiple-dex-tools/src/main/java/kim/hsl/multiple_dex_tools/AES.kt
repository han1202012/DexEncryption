package kim.hsl.multiple_dex_tools

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.zip.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AES {

    // Kotlin 类中的静态变量
    companion object{
        /**
         * 加密密钥, 16 字节
         */
        val DEFAULT_PWD = "kimhslmultiplede"
    }


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
    fun encrypt(contet: ByteArray) : ByteArray{
        var result : ByteArray = encryptCipher.doFinal(contet)
        return  result
    }

    /**
     * 解密操作
     */
    fun decrypt(contet: ByteArray) : ByteArray{
        var result : ByteArray = decryptCipher.doFinal(contet)
        return  result
    }

}