package com.jieluote.annotationProcessorlib;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;
import com.jieluote.annotationlib.*;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

//AutoService important, Through the interface find implement class
//build/classes/java/main/META-INF/services/javax.annotation.processing.Processor
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        processingEnv = processingEnvironment;
        println("init AnnotationProcessor");   //show at build window
        System.out.println("init Processor");  //show at logcat
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(saveFileAnnotation.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(saveFileAnnotation.class);
        for (Element element : elements) {
            //type check
            if (element.getKind() == ElementKind.METHOD) {
                TypeElement classElement = ((TypeElement) element.getEnclosingElement());
                PackageElement packageElement = ((PackageElement) classElement.getEnclosingElement());
                String packageName = packageElement.getQualifiedName().toString();
                saveJavaFile(element,packageName);
            }
        }
        return true;
    }

    /**
     * two ways to generate Java file 1.StringBuilder 2.javapoet
     * file path: \app\build\generated\ap_generated_sources\debug\out\com\jieluote\advancedandroid
     */
    private void saveJavaFile(Element element,String packageName){
        saveFileByStringBuilder(element,packageName);
        saveFileByJavapoet(element,packageName);
    }

    /**
     *
     * @param element
     * @param packageName
     */
    public void saveFileByJavapoet(Element element,String packageName) {
        String fileName = "annotationProcessor_javapoet_" + element.getSimpleName();
        String className = fileName;
        TypeSpec bindingClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethods2())
                .build();
        String path = packageName + "." + className;
        JavaFile javaFile = JavaFile.builder(packageName, bindingClass).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            println("saveFileByJavapoet Exception:" + e);
        }
        println("saveFileByJavapoet success, path = " + path);
    }

    private void saveFileByStringBuilder(Element element,String packageName) {
        try {
            String fileName = "annotationProcessor_stringBuilder_" + element.getSimpleName();
            String className = fileName;
            StringBuilder builder = new StringBuilder();
            builder.append("package ").append(packageName).append(";\n\n");
            builder.append("public class " + className);
            builder.append(" {\n");
            generateMethods(builder);
            builder.append('\n');
            builder.append("}\n");
            String path = packageName + "." + fileName;
            JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(
                    path, element);
            Writer writer = fileObject.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
            println("saveFileByStringBuilder success, path = "+path);
        } catch (IOException e) {
            println("saveFileByStringBuilder:" + e);
        }
    }

    private void generateMethods(StringBuilder builder) {
        builder.append("public void test() {\n");
        builder.append("  }\n");
    }

    private MethodSpec generateMethods2() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("test")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        return methodBuilder.build();
    }

    private void println(String msg){
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, msg);
    }
}