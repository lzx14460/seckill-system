package com.example.MiaoShaSystem.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {
    String action();
    String targetType() default "";
    String detail() default "";
}