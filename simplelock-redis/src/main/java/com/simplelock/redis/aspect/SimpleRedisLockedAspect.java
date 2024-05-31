/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Stanislav Dabov
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

package com.simplelock.redis.aspect;

import com.simplelock.api.ReleaseStrategy;
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
public class SimpleRedisLockedAspect {

    private static final String UNIQUE_KEY = "aop-unique-key";

    private final SimpleLock simpleLock;
    private final ReleaseStrategy releaseStrategy;

    @Around("@annotation(com.simplelock.redis.aspect.SimpleRedisLocked)")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SimpleRedisLocked annotation = signature.getMethod().getAnnotation(SimpleRedisLocked.class);

        log.debug("Intercepted method [{}] annotated with [{}]", signature.getMethod().getName(),
                SimpleRedisLocked.class.getSimpleName());

        String lockKey = signature.getMethod().getName() + "-" + UNIQUE_KEY;

        // Try to acquire lock by using just the method name as unique key
        Optional<String> tokenOptional = simpleLock.acquire(lockKey);

        if (tokenOptional.isPresent()) {
            // Proceed with execution if lock successfully acquired
            Object result = joinPoint.proceed();

            if (releaseStrategy != ReleaseStrategy.WITH_DELAY
                    && annotation.releaseStrategy() != ReleaseStrategy.WITH_DELAY) {
                simpleLock.release(tokenOptional.get());
            }

            return result;
        }

        return null;
    }
}
