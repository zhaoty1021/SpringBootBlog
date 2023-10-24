package com.wip.aop;

import java.lang.annotation.*;

/**
 * 系统日志注解
 * @author yingxiu.zty
 * @createTime on 2023/10/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BlogLog {
    String value() default "";
}
