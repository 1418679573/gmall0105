package com.pigxia.gmall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by absen on 2020/6/5 11:20
 */
//  注解的使用范围  方法（METHOD）还是类上
@Target(ElementType.METHOD)
// 注解的运行时，是在编译期SOURCE，还是运行期 RUNTIME
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    boolean loginSuccess() default true;
}
