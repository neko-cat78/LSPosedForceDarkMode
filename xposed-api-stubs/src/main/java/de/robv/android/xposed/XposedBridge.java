package de.robv.android.xposed;

import java.lang.reflect.Member;

public class XposedBridge {
    public static void log(String text) {}
    public static void hookMethod(Member hookMethod, XC_MethodHook callback) {}
    public static void hookAllMethods(Class<?> clazz, String methodName, XC_MethodHook callback) {}
}
