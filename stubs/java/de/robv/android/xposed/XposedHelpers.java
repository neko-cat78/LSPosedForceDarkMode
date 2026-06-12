package de.robv.android.xposed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class XposedHelpers {

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void findAndHookMethod(String className, ClassLoader classLoader,
                                          String methodName, Object... rest) {
        try {
            Class<?> clazz = findClass(className, classLoader);
            findAndHookMethod(clazz, methodName, rest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void findAndHookMethod(Class<?> clazz, String methodName,
                                          Object... rest) {
        try {
            int argCount = (rest.length - 1) / 2;
            Class<?>[] paramTypes = new Class<?>[argCount];
            for (int i = 0; i < argCount; i++) {
                paramTypes[i] = (Class<?>) rest[i * 2];
            }
            XC_MethodHook callback = (XC_MethodHook) rest[rest.length - 1];
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            XposedBridge.hookMethod(method, callback);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getStaticIntField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field.getInt(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
            }
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
