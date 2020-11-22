#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <malloc.h>
#include <string.h>
#include <openssl/evp.h>

//密钥
static uint8_t *userkey = "abcdefghijklmnop";

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "NDK", __VA_ARGS__)


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
