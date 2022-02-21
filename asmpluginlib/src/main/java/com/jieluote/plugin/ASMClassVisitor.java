package com.jieluote.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ASMClassVisitor extends ClassVisitor {
    public ASMClassVisitor(int api) {
        super(api);
    }

    public ASMClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor);
    }

    public ASMClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    /**
     * 读取方法
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        //System.out.println("visitMethod,name:" + name);
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        return new ASMMethodVisitor(methodVisitor, access, name, desc);
    }
}
