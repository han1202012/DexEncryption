package kim.hsl.multipledex;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflexUtils {

    /**
     * 通过反射方法获取 instance 类中的 memberName 名称的成员
     * @param instance 成员所在对象
     * @param memberName 成员变量名称
     * @return 返回 Field 类型成员
     * @throws NoSuchFieldException
     */
    public static Field reflexField(Object instance, String memberName) throws NoSuchFieldException {

        // 获取字节码类
        Class clazz = instance.getClass();

        // 循环通过反射获取
        // 可能存在通过反射没有找到成员的情况 , 此时查找其父类是否有该成员
        // 循环次数就是其父类层级个数
        while (clazz != null) {
            try {
                // 获取成员
                Field memberField = clazz.getDeclaredField(memberName);

                // 如果不是 public , 无法访问 , 设置可访问
                if (!memberField.isAccessible()) {
                    memberField.setAccessible(true);
                }
                return memberField;
            } catch (NoSuchFieldException exception){
                // 如果找不到, 就到父类中查找
                clazz = clazz.getSuperclass();
            }
        }

        // 如果没有拿到成员 , 则直接中断程序 , 加载无法进行下去
        throw new NoSuchFieldException("没有在 " + clazz.getName() + " 类中找到 " + memberName +  "成员");
    }

    /**
     * 通过反射方法获取 instance 类中的 参数为 parameterTypes , 名称为 methodName 的成员方法
     * @param instance 成员方法所在对象
     * @param methodName 成员方法名称
     * @param parameterTypes 成员方法参数
     * @return
     * @throws NoSuchMethodException
     */
    public static Method reflexMethod(Object instance, String methodName, Class... parameterTypes)
            throws NoSuchMethodException {

        // 获取字节码类
        Class clazz = instance.getClass();

        // 循环通过反射获取
        // 可能存在通过反射没有找到成员方法的情况 , 此时查找其父类是否有该成员方法
        // 循环次数就是其父类层级个数
        while (clazz != null) {
            try {
                // 获取成员方法
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);

                // 如果不是 public , 无法访问 , 设置可访问
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                // 如果找不到, 就到父类中查找
                clazz = clazz.getSuperclass();
            }
        }

        // 如果没有拿到成员 , 则直接中断程序 , 加载无法进行下去
        throw new NoSuchMethodException("没有在 " + clazz.getName() + " 类中找到 " + methodName +  "成员方法");
    }

}
