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
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This aspect will inject the behaviour of acquiring the lock before the annotated method and
 * then release the lock only if the annotated method managed to successfully acquire the lock.
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class SimpleJdbcLockedAspect {

    private final SimpleLock simpleLock;

    @Around("@annotation(com.simplelock.core.SimpleJdbcLocked)")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SimpleJdbcLocked annotation = signature.getMethod().getAnnotation(SimpleJdbcLocked.class);

        log.debug("Intercepted method [{}] annotated with [{}]", signature.getMethod().getName(),
                SimpleJdbcLocked.class.getSimpleName());

        if (log.isWarnEnabled()) {
            long minutes = TimeUnit.MINUTES.convert(annotation.releaseAfter(), annotation.timeUnit());
            // Print a log message here in case client wants to hold the lock for too long. This might end up
            // having the lock stuck in DB and never released (or released just after service restart only if
            // configured so), as there will be a scheduled thread spawned to execute the release in the
            // service node which acquired the lock initially
            if (minutes >= 1L) {
                log.warn("Holding a lock for too long might end up having your lock record stuck in database "
                        + "and never released after e.g. service restart or crash. Currently you're about to "
                        + "hold the lock for {} minute(s) or more.", minutes);
            }
        }

        Optional<String> tokenOptional = Optional.empty();
        Object result = null;
        try {
            tokenOptional = simpleLock.acquire(signature.getMethod().getName());
            if (tokenOptional.isPresent()) {
                result = joinPoint.proceed();
            }
        } finally {
            tokenOptional.ifPresent(token -> simpleLock.release(token,
                    annotation.releaseAfter(),
                    annotation.timeUnit()));
        }

        return result;
    }
}
