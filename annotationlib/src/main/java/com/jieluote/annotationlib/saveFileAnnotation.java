package com.jieluote.annotationlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)  //编译时注解
@Target(ElementType.METHOD)        //注解对象范围-方法
public @interface saveFileAnnotation {  //生成java文件
}