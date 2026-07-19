package com.huigrowth.babycare.aspect;

import com.huigrowth.babycare.aspect.AuditLogAnnotation;
import com.huigrowth.babycare.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 审计日志 AOP 切面
 * 自动拦截标注了 @AuditLog 注解的方法，记录操作日志。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Pointcut("@annotation(com.huigrowth.babycare.aspect.AuditLogAnnotation)")
    public void auditPointcut() {}

    @AfterReturning(pointcut = "auditPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AuditLogAnnotation auditLogAnno = method.getAnnotation(AuditLogAnnotation.class);
            if (auditLogAnno == null) return;

            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return;

            String username = auth.getName();
            Long userId = null;
            if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                // 尝试从 UserDetails 中获取 userId（这里通过自定义方式）
                // 简化处理：从 authentication 的 details 或 credentials 中获取
            }

            // 获取请求信息
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            // 构建详情
            Object[] args = joinPoint.getArgs();
            StringBuilder details = new StringBuilder();
            if (args != null && args.length > 0 && args[0] != null) {
                details.append("参数: ").append(args[0].getClass().getSimpleName());
                if (args[0] instanceof com.huigrowth.babycare.dto.RoleCreateRequest roleReq) {
                    details.append(" [name=").append(roleReq.getName()).append("]");
                } else if (args[0] instanceof com.huigrowth.babycare.dto.RoleAssignRequest assignReq) {
                    details.append(" [userId=").append(assignReq.getUserId()).append("]");
                } else if (args[0] instanceof Long id) {
                    details.append(" [id=").append(id).append("]");
                }
            }

            auditService.record(
                userId, username,
                auditLogAnno.action(), auditLogAnno.actionName(),
                auditLogAnno.targetType(), extractTargetId(args),
                details.toString(), ipAddress, userAgent
            );
        } catch (Exception e) {
            log.warn("审计日志记录异常: {}", e.getMessage());
        }
    }

    private Long extractTargetId(Object[] args) {
        if (args == null || args.length == 0) return null;
        // 第一个参数如果是 Long 类型，作为目标 ID
        if (args[0] instanceof Long) return (Long) args[0];
        // 尝试从第一个参数中获取 id 字段
        try {
            var field = args[0].getClass().getDeclaredField("id");
            field.setAccessible(true);
            Object val = field.get(args[0]);
            if (val instanceof Number) return ((Number) val).longValue();
        } catch (Exception ignored) {}
        // 尝试获取 xxxId 字段
        try {
            for (var field : args[0].getClass().getDeclaredFields()) {
                if (field.getName().endsWith("Id") && field.getType() == Long.class) {
                    field.setAccessible(true);
                    Object val = field.get(args[0]);
                    if (val instanceof Number) return ((Number) val).longValue();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
