package com.ecomm.orderservice.common.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Log before entering any controller method
    @Before("execution(* com.ecomm..controller..*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        logger.info("➡️ [CONTROLLER] Entering: {}", method);
        logger.info("   Args: {}", Arrays.toString(joinPoint.getArgs()));
    }

    // Log after a controller method returns
    @AfterReturning(pointcut = "execution(* com.ecomm..controller..*(..))", returning = "result")
    public void logAfterReturningController(JoinPoint joinPoint, Object result) {
        String method = joinPoint.getSignature().toShortString();
        logger.info("✅ [CONTROLLER] Exiting: {}", method);
        logger.info("   Response: {}", result);
    }

    // Log before any service method
    @Before("execution(* com.ecomm..service..*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        logger.info("🔄 [SERVICE] Calling: {}", method);
        logger.info("   Args: {}", Arrays.toString(joinPoint.getArgs()));
    }

    // Log after any service method returns
    @AfterReturning(pointcut = "execution(* com.ecomm..service..*(..))", returning = "result")
    public void logAfterReturningService(JoinPoint joinPoint, Object result) {
        String method = joinPoint.getSignature().toShortString();
        logger.info("✅ [SERVICE] Returned from: {}", method);
        logger.info("   Result: {}", result);
    }

    // Log exceptions thrown from any controller/service
    @AfterThrowing(pointcut = "execution(* com.ecomm..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().toShortString();
        logger.error("❌ [ERROR] Exception in: {}", method);
        logger.error("   Message: {}", ex.getMessage(), ex);
    }
}
