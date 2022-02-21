package com.jieluote.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 拦截方法，这里可以增加注入逻辑(修改后,要重新uploadArchives)
 * 我们这里在方法进入和退出时各打印了一行日志
 */
public class ASMMethodVisitor extends AdviceAdapter {
    private static final String ANNOTATION_TRACK_METHOD = "Lcom/jieluote/asmlib/TrackMethod;";
    private boolean needInject;
    private String parameter;
    private final MethodVisitor methodVisitor;

    protected ASMMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(Opcodes.ASM6, methodVisitor, access, name, descriptor);
        //System.out.println("ASMMethodVisitor init");
        this.methodVisitor = methodVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);
        //System.out.println("visitAnnotation,desc:" + desc);
        if (desc.equals(ANNOTATION_TRACK_METHOD)) {
            needInject = true;
            return new AnnotationVisitor(Opcodes.ASM6, annotationVisitor) {
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, value);
                    if (name.equals("parameter")) {
                        //取得注解的值
                        parameter = (String) value;
                        System.out.println("visitAnnotation,parameter:" + parameter);
                    }
                }
            };
        }
        return annotationVisitor;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        if (needInject) {
            System.out.println("onMethodEnter");
            //注入以下代码:  Log.d("asmtest","aopMethod start parameter is :"+parameter);
            methodVisitor.visitLdcInsn("asmtest");
            methodVisitor.visitLdcInsn("aopMethod start, parameter is :" + parameter);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            methodVisitor.visitInsn(POP);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        if (needInject) {
            System.out.println("onMethodExit");
            //注入以下代码:  Log.d("asmtest","aopMethod end");
            methodVisitor.visitLdcInsn("asmtest");
            methodVisitor.visitLdcInsn("aopMethod end");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            methodVisitor.visitInsn(POP);
        }
    }
}
