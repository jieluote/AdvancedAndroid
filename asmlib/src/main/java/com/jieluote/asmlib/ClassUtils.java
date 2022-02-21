package com.jieluote.asmlib;

import java.lang.reflect.Method;

public class ClassUtils {
    public static volatile ClassUtils sInstance;
    public static ClassUtils getInstance() {
        if (sInstance == null) {
            synchronized (ClassUtils.class) {
                if (sInstance == null) {
                    sInstance = new ClassUtils();
                }
            }
        }
        return sInstance;
    }

    private ClassUtils(){}

    public void loadClass(String path, String name) {
        ASMClassLoader loader = new ASMClassLoader(path);
        try {
            Class<?> loaderClass = loader.findClass(name);
            if(loaderClass == null){
                return;
            }
            Object instance = loaderClass.newInstance();
            Method main = loaderClass.getDeclaredMethod("println");
            main.invoke(instance, (Object) null);
        } catch (Exception e) {
            System.out.println("ClassUtils loadClass Exception:" + e);
        }
    }
}
