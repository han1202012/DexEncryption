package kim.hsl.multipledex;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Lance
 * @date 2017/12/25
 */

public class Utils {

    static {
        System.loadLibrary("dn_ssl");
    }

    public static native void decrypt(byte[] data, String path);


    /**
     * 读取文件
     * @param file
     * @return
     * @throws Exception
     */
    public static byte[] getBytes(File file) {
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
     * 反射获得 指定对象(当前-》父类-》父类...)中的 成员属性
     * @param instance
     * @param name
     * @return
     * @throws NoSuchFieldException
     */
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();
        //反射获得
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                //如果无法访问 设置为可访问
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                //如果找不到往父类找
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }


    /**
     * 反射获得 指定对象(当前-》父类-》父类...)中的 函数
     * @param instance
     * @param name
     * @param parameterTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Method findMethod(Object instance, String name, Class... parameterTypes)
            throws NoSuchMethodException {
        Class clazz = instance.getClass();
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                //如果找不到往父类找
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList
                (parameterTypes) + " not found in " + instance.getClass());
    }
}
