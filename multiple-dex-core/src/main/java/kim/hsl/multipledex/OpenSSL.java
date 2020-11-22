package kim.hsl.multipledex;

public class OpenSSL {
    static {
        System.loadLibrary("openssl");
    }

    public static native void decrypt(byte[] data, String path);
}
