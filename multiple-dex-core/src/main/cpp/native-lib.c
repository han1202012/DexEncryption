#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <malloc.h>
#include <string.h>
#include <openssl/evp.h>

//密钥
static uint8_t *userkey = "abcdefghijklmnop";

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "NDK", __VA_ARGS__)

JNIEXPORT void JNICALL
Java_kim_hsl_multipledex_OpenSSL_decrypt(JNIEnv *env, jclass clazz, jbyteArray data, jstring path) {

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
    int outlen, tmplen, rv;
    unsigned char outbuf[1024];

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
    EVP_DecryptInit_ex(ctx, EVP_aes_192_ecb(), NULL, "kimhslmultiplede", NULL);


    /* Set ciphertext length: only needed if we have AAD */
    /*
     * 解密操作
     * int EVP_DecryptUpdate(EVP_CIPHER_CTX *ctx, unsigned char *out,
                                 int *outl, const unsigned char *in, int inl);
     * 解密 inl 长度的 in , 解密为 outl 长度的 out
     * 01:00:51
     */
    EVP_DecryptUpdate(ctx, NULL, &outlen, NULL, sizeof(ccm_ct));
    /* Zero or one call to specify any AAD */
    EVP_DecryptUpdate(ctx, NULL, &outlen, ccm_adata, sizeof(ccm_adata));
    /* Decrypt plaintext, verify tag: can only be called once */
    rv = EVP_DecryptUpdate(ctx, outbuf, &outlen, ccm_ct, sizeof(ccm_ct));
    /* Output decrypted block: if tag verify failed we get nothing */
    if (rv > 0) {
        printf("Plaintext:\n");
        BIO_dump_fp(stdout, outbuf, outlen);
    } else
        printf("Plaintext not available: tag verify failed.\n");
    EVP_CIPHER_CTX_free(ctx);


    // 释放 Java 引用
    (*env)->ReleaseByteArrayElements(env, data, src, 0);
    (*env)->ReleaseStringUTFChars(env, path, path);

}

/*
JNIEXPORT void JNICALL
Java_com_dongnao_proxy_guard_core_Utils_decrypt(JNIEnv *env, jobject instance,
                                                jbyteArray encrypt_, jstring path_) {
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
}*/
