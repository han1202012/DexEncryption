package kim.hsl.multipledex;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OpenSSL {
    static {
        Log.i("ProxyApplication", "System.loadLibrary Start");
        System.loadLibrary("openssl");
        Log.i("ProxyApplication", "System.loadLibrary Over");
    }

    /**
     * 从文件中读取 Byte 数组
     * @param file
     * @return
     * @throws Exception
     */
    public static byte[] getBytes(File file) throws Exception {
        /*RandomAccessFile r = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) r.length()];
        r.readFully(buffer);
        r.close();
        return buffer;*/

        Log.i("ProxyApplication", "getBytes");
        try {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            Log.i("ProxyApplication", "RandomAccessFile Over");
            byte[] buffer = new byte[(int) r.length()];
            r.readFully(buffer);
            r.close();
            Log.i("ProxyApplication", "正常返回");
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("ProxyApplication", "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("ProxyApplication", "IOException");
        }
        Log.i("ProxyApplication", "null");
        return null;
    }

    /**
     * 调用 OpenSSL 解密 dex 文件
     * @param data
     * @param path
     */
    public static native void decrypt(byte[] data, String path);
}
