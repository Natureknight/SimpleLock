package com.simplelock.mongo.aspect;

import com.simplelock.api.SimpleLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class SimpleMongoLockedAspect {

    private static final String UNIQUE_KEY = "aop-unique-key";

    private final SimpleLock simpleLock;

    @Around("@annotation(com.simplelock.mongo.aspect.SimpleMongoLocked)")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SimpleMongoLocked annotation = signature.getMethod().getAnnotation(SimpleMongoLocked.class);

        log.debug("Intercepted method [{}] annotated with [{}]", signature.getMethod().getName(),
                SimpleMongoLocked.class.getSimpleName());

        // Try to acquire lock by using just the method name as unique key
        Optional<String> tokenOptional = simpleLock.acquireWithKeyPrefix(
                signature.getMethod().getName(), UNIQUE_KEY);

        if (tokenOptional.isPresent()) {
            // Proceed with execution if lock successfully acquired
            Object result = joinPoint.proceed();

            // Instantly release the lock after execution
            if (annotation.releaseImmediately() || annotation.releaseAfter() == 0L) {
                simpleLock.releaseImmediately(tokenOptional.get());
                return result;
            }

            // Or release the lock with the specified delay
            simpleLock.release(tokenOptional.get(), annotation.releaseAfter(), annotation.timeUnit());
            return result;
        }

        return null;
    }
}
