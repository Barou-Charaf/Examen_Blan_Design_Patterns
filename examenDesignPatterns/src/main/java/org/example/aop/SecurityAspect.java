package org.example.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.security.RequiresRole;
import org.example.security.SecurityContext;

@Aspect
public class SecurityAspect {
    @Before(value = "execution(* *(..)) && @annotation(permission)", argNames = "permission")
    public void checkAccess(RequiresRole permission) {
        SecurityContext.require(permission.value());
    }
}
