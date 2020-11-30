#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <malloc.h>
#include <string.h>
#include <openssl/evp.h>
#include "logging_macros.h"

//密钥
static uint8_t *userkey = (uint8_t *) "abcdefghijklmnop";

extern "C"
JNIEXPORT void JNICALL
Java_kim_hsl_multipledex_OpenSSL_decrypt(JNIEnv *env, jclass clazz, jbyteArray encrypt_, jstring path_) {

    jbyte *src = (env)->GetByteArrayElements(encrypt_, NULL);
    const char *path = (env)->GetStringUTFChars(path_, 0);
    int src_len = (env)->GetArrayLength( encrypt_);

    //解密
    //加解密的 上下文
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    int outlen;
    unsigned char outbuf[1024];
    //初始化上下文 设置解码参数
    EVP_DecryptInit_ex(ctx, EVP_aes_128_ecb(), NULL, userkey, NULL);

    //密文比明文长，所以肯定能保存下所有的明文
    uint8_t *out = static_cast<uint8_t *>(malloc(src_len));
    //数据置空
    memset(out, 0, src_len);
    int len;
    //解密   abcdefg  z    z
    EVP_DecryptUpdate(ctx, out, &outlen, reinterpret_cast<const unsigned char *>(src), src_len);
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
    (env)->ReleaseByteArrayElements(encrypt_, src, 0);
    (env)->ReleaseStringUTFChars(path_, path);

}