package com.h928.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * 使用注解标识Activity的哪些方法可以被注入.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewAnno {
    /** 
     * 用于查找和注入的视图ID
     */  
    public int value();  
}  