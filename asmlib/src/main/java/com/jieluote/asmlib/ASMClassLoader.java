package com.jieluote.asmlib;

import java.io.FileInputStream;
import java.io.IOException;

public class ASMClassLoader extends ClassLoader {
    public String classPath;

    public ASMClassLoader() {}

    public ASMClassLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name == null || name.length() == 0) {
            return null;
        }
        try {
            byte[] data = loadByte(name);
            //注意此方法在JVM中可以使用，但在DVM中不能使用，直接抛出异常(can't load this type of class file)
            //DVM的classLoader并不支持读取class文件,只支持加载dex文件
            return defineClass(name, data, 0, data.length);
        } catch (IOException e) {
            throw new ClassNotFoundException();
        }
    }

    public byte[] loadByte(String name) throws IOException {
        if (name == null || name.length() == 0) {
            return new byte[1024];
        }
        String filePath = classPath + "/" + name + ".class";
        FileInputStream fis = new FileInputStream(filePath);
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;
    }
}
