package com.portfolio.enterprise.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Audit logging aspect for tracking service method calls.
 * Demonstrates:
 * - Aspect-Oriented Programming (AOP)
 * - Cross-cutting concerns
 * - Method interception
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /**
     * Logs all service method calls.
     */
    @Around("execution(* com.portfolio.enterprise.service.*.*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        LocalDateTime startTime = LocalDateTime.now();
        String clientIp = getClientIp();

        auditLogger.info("AUDIT | START | {} | {}.{} | args={} | ip={}",
                startTime, className, methodName, sanitizeArgs(args), clientIp);

        try {
            Object result = joinPoint.proceed();

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            auditLogger.info("AUDIT | SUCCESS | {} | {}.{} | duration={}ms | ip={}",
                    endTime, className, methodName, duration, clientIp);

            return result;
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            auditLogger.error("AUDIT | FAILURE | {} | {}.{} | duration={}ms | error={} | ip={}",
                    endTime, className, methodName, duration, e.getMessage(), clientIp);

            throw e;
        }
    }

    /**
     * Gets client IP from request context.
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String forwarded = request.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isEmpty()) {
                    return forwarded.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Not in a web context
        }
        return "N/A";
    }

    /**
     * Sanitizes arguments to avoid logging sensitive data.
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null)
                        return "null";
                    String str = arg.toString();
                    // Mask potential passwords
                    if (str.toLowerCase().contains("password")) {
                        return "[REDACTED]";
                    }
                    // Truncate long strings
                    if (str.length() > 100) {
                        return str.substring(0, 100) + "...";
                    }
                    return str;
                })
                .toList()
                .toString();
    }
}
