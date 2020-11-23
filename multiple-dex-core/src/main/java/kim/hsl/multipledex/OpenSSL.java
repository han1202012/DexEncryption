package kim.hsl.multipledex;

import java.io.File;
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
        RandomAccessFile r = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) r.length()];
        r.readFully(buffer);
        r.close();
        return buffer;
    }

    public static native void decrypt(byte[] data, String path);
}
