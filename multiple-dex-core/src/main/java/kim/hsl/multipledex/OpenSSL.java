package kim.hsl.multipledex;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OpenSSL {
    static {
        System.loadLibrary("openssl");
    }

    /**
     * 从文件中读取 Byte 数组
     * @param file
     * @return
     * @throws Exception
     */
    public static byte[] getBytes(File file) throws Exception {
        try {
            // 创建随机读取文件
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            // 获取文件字节数 , 创建保存文件数据的缓冲区
            byte[] buffer = new byte[(int) randomAccessFile.length()];
            // 读取整个文件数据
            randomAccessFile.readFully(buffer);
            // 关闭文件
            randomAccessFile.close();
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 调用 OpenSSL 解密 dex 文件
     * @param data
     * @param path
     */
    public static native void decrypt(byte[] data, String path);
}
