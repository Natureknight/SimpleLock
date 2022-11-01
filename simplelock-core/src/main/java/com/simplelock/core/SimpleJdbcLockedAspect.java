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

package com.simplelock.core;

import com.simplelock.api.SimpleLock;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import static java.util.Objects.nonNull;

/**
 * This aspect will inject the behaviour of acquiring the lock before the annotated method and
 * then release the lock only if the annotated method managed to successfully acquire the lock.
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
@Aspect
@RequiredArgsConstructor
public class SimpleJdbcLockedAspect {

    private final SimpleLock simpleLock;

    @Around("@annotation(com.simplelock.core.SimpleJdbcLocked)")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SimpleJdbcLocked annotation = signature.getMethod().getAnnotation(SimpleJdbcLocked.class);
        String token = null;
        Object result;
        try {
            token = simpleLock.acquire(signature.getMethod().getName());
            result = joinPoint.proceed();
        } finally {
            if (nonNull(token)) {
                simpleLock.release(token, annotation.releaseAfter());
            }
        }

        return result;
    }
}
