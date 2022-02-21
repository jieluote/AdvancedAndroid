package com.jieluote.plugin;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class ASMUtils {
    public static volatile ASMUtils sInstance;
    public static ASMUtils getInstance() {
        if (sInstance == null) {
            synchronized (ASMUtils.class) {
                if (sInstance == null) {
                    sInstance = new ASMUtils();
                }
            }
        }
        return sInstance;
    }

    private ASMUtils(){}

    public void modifyClassByIOSteam(File oldFile,File newFile) {
        String className = oldFile.getName();
        FileOutputStream out = null;
        try {
            byte[] sourceBytes = IOUtils.toByteArray(new FileInputStream(oldFile));
            byte[] modifiedBytes = modifyClass(sourceBytes);
            File modified = new File(newFile, className.replace(".", "") + "_modified.class");
            if (modified.exists()) {
                modified.delete();
            }
            modified.createNewFile();
            out = new FileOutputStream(modified);
            out.write(modifiedBytes);
        } catch (Exception e) {
            System.out.println("ASM Exception:" + e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] modifyClass(byte[] classBytes) {
        System.out.println("ASMUtils modifyClass start");
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ASMClassVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        System.out.println("ASMUtils modifyClass end");
        return classWriter.toByteArray();
    }
}
