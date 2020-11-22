package kim.hsl.multipledex

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * 通过反射方法获取 instance 类中的 memberName 名称的成员
 */
fun reflexField(instance: Any, memberName: String): Field {

    // 获取字节码类
    var clazz: Class<*>? = instance.javaClass

    // 循环通过反射获取
    // 可能存在通过反射没有找到成员的情况 , 此时查找其父类是否有该成员
    // 循环次数就是其父类层级个数
    while (clazz != null) {
        try {
            // 获取成员
            val memberField = clazz.getDeclaredField(memberName)

            // 如果不是 public , 无法访问 , 设置可访问
            if (!memberField.isAccessible) {
                memberField.isAccessible = true
            }
            return memberField
        } catch (e: NoSuchFieldException) {
            // 如果找不到, 就到父类中查找
            clazz = clazz.superclass
        }
    }

    // 如果没有拿到成员 , 则直接中断程序 , 加载无法进行下去
    throw NoSuchFieldException("没有在 ${instance.javaClass} 类中找到 $memberName 成员")
}


/**
 * 通过反射方法获取 instance 类中的 参数为 parameterTypes , 名称为 methodName 的成员方法
 */
fun reflexMethod(instance: Any, methodName: String, vararg parameterTypes: Class<*>?): Method {

    // 获取字节码类
    var clazz: Class<*>? = instance.javaClass

    // 循环通过反射获取
    // 可能存在通过反射没有找到成员方法的情况 , 此时查找其父类是否有该成员方法
    // 循环次数就是其父类层级个数
    while (clazz != null) {
        try {
            // 获取成员方法
            val method = clazz.getDeclaredMethod(methodName, *parameterTypes)

            // 如果不是 public , 无法访问 , 设置可访问
            if (!method.isAccessible) {
                method.isAccessible = true
            }
            return method
        } catch (e: NoSuchMethodException) {
            // 如果找不到, 就到父类中查找
            clazz = clazz.superclass
        }
    }

    // 如果没有拿到成员 , 则直接中断程序 , 加载无法进行下去
    throw NoSuchMethodException("没有在 ${instance.javaClass} 类中找到 $methodName 成员方法")
}