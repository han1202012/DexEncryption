#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <malloc.h>
#include <string.h>
#include <openssl/evp.h>
#include "logging_macros.h"

//密钥
static uint8_t *userkey = "kimhslmultiplede";

JNIEXPORT void JNICALL
Java_kim_hsl_multipledex_OpenSSL_decrypt1(JNIEnv *env, jobject instance, jbyteArray data, jstring path) {

    // 将 Java Byte 数组转为 C 数组
    jbyte *src = (*env)->GetByteArrayElements(env, data, NULL);
    // 将 Java String 字符串转为 C char* 字符串
    const char *filePath = (*env)->GetStringUTFChars(env, path, 0);
    // 获取 Java Byte 数组长度
    int srcLen = (*env)->GetArrayLength(env, data);

    /*
     * 下面的代码是从 OpenSSL 源码跟目录下 demos/evp/aesccm.c 中拷贝并修改
     */

    // 加密解密的上下文
    EVP_CIPHER_CTX *ctx;
    int outlen;
    // 创建加密解密上下文
    ctx = EVP_CIPHER_CTX_new();

    /* Select cipher 配置上下文解码参数
     * 配置加密模式 :
     * Java 中的加密算法类型 "AES/ECB/PKCS5Padding" , 使用 ecb 模式
     * EVP_aes_192_ecb() 配置 ecb 模式
     * AES 有五种加密模式 : CBC、ECB、CTR、OCF、CFB
     * 配置密钥 :
     * Java 中定义的密钥是 "kimhslmultiplede"
     */
    EVP_DecryptInit_ex(ctx, EVP_aes_128_ecb(), NULL, "kimhslmultiplede", NULL);


    // 申请解密输出数据内存, 申请内存长度与密文长度一样即可
    // AES 加密密文比明文要长
    uint8_t *out = malloc(srcLen);
    // 将申请的内存设置为 0
    memset(out, 0, srcLen);

    // 记录解密总长度
    int totalLen = 0;

    /*
     * 解密操作
     * int EVP_DecryptUpdate(EVP_CIPHER_CTX *ctx, unsigned char *out,
                                 int *outl, const unsigned char *in, int inl);
     * 解密 inl 长度的 in , 解密为 outl 长度的 out
     * 解密的输入数据是 src, 长度为 srcLen 字节, 注意该长度是 int 类型
     * 解密的输出数据是 out, 长度为 srcLen 字节, 注意该长度是 int* 指针类型
     */
    EVP_DecryptUpdate(ctx, out, &outlen, src, srcLen);
    totalLen += outlen; //更新总长度

    /*
     * int EVP_DecryptFinal_ex(EVP_CIPHER_CTX *ctx, unsigned char *outm,
                                   int *outl);
     * 解密时, 每次解密 16 字节, 如果超过了 16 字节 , 就会剩余一部分无法解密,
     * 之前的 out 指针已经解密了 outlen 长度, 此时接着后续解密, 指针需要进行改变 out + outlen
     * 此时需要调用该函数 , 解密剩余内容
     */
    EVP_DecryptFinal_ex(ctx, out + outlen, &outlen);
    totalLen += outlen; //更新总长度, 此时 totalLen 就是总长度

    // 解密完成, 释放上下文对象
    EVP_CIPHER_CTX_free(ctx);

    // 将解密出的明文, 写出到给定的 Java 文件中
    FILE *file = fopen(filePath, "wb");
    // 写出 out 指针指向的数据 , 写出个数 totalLen * 1 , 写出到 file 文件中
    fwrite(out, totalLen, 1, file);
    // 关闭文件
    fclose(file);
    // 释放解密出的密文内存
    free(out);

    // 释放 Java 引用
    (*env)->ReleaseByteArrayElements(env, data, src, 0);
    (*env)->ReleaseStringUTFChars(env, path, filePath);

}

JNIEXPORT void JNICALL
Java_kim_hsl_multipledex_OpenSSL_decrypt(JNIEnv *env, jobject instance, jbyteArray encrypt_, jstring path_) {
    jbyte *src = (*env)->GetByteArrayElements(env, encrypt_, NULL);
    const char *path = (*env)->GetStringUTFChars(env, path_, 0);
    int src_len = (*env)->GetArrayLength(env, encrypt_);

    //解密
    //加解密的 上下文
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    int outlen;
    unsigned char outbuf[1024];
    //初始化上下文 设置解码参数
    EVP_DecryptInit_ex(ctx, EVP_aes_128_ecb(), NULL, userkey, NULL);

    //密文比明文长，所以肯定能保存下所有的明文
    uint8_t *out = malloc(src_len);
    //数据置空
    memset(out, 0, src_len);
    int len;
    //解密   abcdefg  z    z
    EVP_DecryptUpdate(ctx, out, &outlen, src, src_len);
    len = outlen;
    //解密剩余的所有数据 校验
    EVP_DecryptFinal_ex(ctx, out + outlen, &outlen);
    len += outlen;
    EVP_CIPHER_CTX_free(ctx);

    //写文件 以二进制形式写出
    FILE *f = fopen(path, "wb");
    fwrite(out, len, 1, f);
    fclose(f);
    free(out);
    (*env)->ReleaseByteArrayElements(env, encrypt_, src, 0);
    (*env)->ReleaseStringUTFChars(env, path_, path);
}
