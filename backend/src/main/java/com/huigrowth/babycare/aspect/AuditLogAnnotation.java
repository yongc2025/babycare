package com.huigrowth.babycare.aspect;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 标注在需要记录审计日志的 Controller 方法上。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLogAnnotation {

    /** 操作标识，如 CREATE_ROLE、ASSIGN_ROLE */
    String action();

    /** 操作中文名，如 "创建角色"、"分配用户角色" */
    String actionName();

    /** 目标类型，如 "Role"、"User"、"Menu" */
    String targetType() default "";
}
