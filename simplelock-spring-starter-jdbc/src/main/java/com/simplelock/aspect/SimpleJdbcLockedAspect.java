/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Stanislav Dabov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.simplelock.aspect;

import com.simplelock.api.SimpleLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;

/**
 * This aspect will inject the behaviour of acquiring the lock before the annotated method and
 * then release the lock only if the annotated method managed to successfully acquire the lock.
 *
 * @author Stanislav Dabov
 * @see SimpleJdbcLocked
 * @since 1.0.0
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class SimpleJdbcLockedAspect {

    private final SimpleLock simpleLock;

    @Around("@annotation(com.simplelock.aspect.SimpleJdbcLocked)")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SimpleJdbcLocked annotation = signature.getMethod().getAnnotation(SimpleJdbcLocked.class);

        log.debug("Intercepted method [{}] annotated with [{}]", signature.getMethod().getName(),
                SimpleJdbcLocked.class.getSimpleName());

        // Try to acquire lock by using just the method name as unique key
        Optional<String> tokenOptional = simpleLock.acquireWithKeyPrefix(
                signature.getMethod().getName(), "");

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
