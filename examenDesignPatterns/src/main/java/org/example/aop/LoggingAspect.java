package org.example.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import java.util.logging.Logger;

@Aspect
@DeclarePrecedence("org.example.aop.LoggingAspect, org.example.aop.SecurityAspect")
public class LoggingAspect {
    private static final Logger LOGGER = Logger.getLogger("org.example.audit");

    @Around("execution(* org.example..*(..)) && !within(org.example.aop..*)"
            + " && !within(org.example.security..*) && !within(org.example.*Test*)")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        String method = point.getSignature().toShortString();
        long start = System.nanoTime();
        LOGGER.info("ENTREE " + method);
        try {
            Object result = point.proceed();
            LOGGER.info("SUCCES " + method);
            return result;
        } catch (Throwable failure) {
            LOGGER.warning("ECHEC " + method + " : " + failure.getClass().getSimpleName());
            throw failure;
        } finally {
            LOGGER.info("DUREE " + method + " : " + (System.nanoTime() - start) + " ns");
        }
    }
}
