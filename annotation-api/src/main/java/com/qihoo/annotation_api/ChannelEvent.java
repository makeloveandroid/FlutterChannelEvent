package com.qihoo.annotation_api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解处理器需要用到的源注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ChannelEvent {
    /**
     * 最终要调用的methodName
     */
    String methodName();
}
