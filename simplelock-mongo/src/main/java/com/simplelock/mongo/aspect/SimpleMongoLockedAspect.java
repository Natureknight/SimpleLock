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

package com.simplelock.mongo.aspect;

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
public class SimpleMongoLockedAspect {

    private static final String UNIQUE_KEY = "aop-unique-key";

    private final SimpleLock simpleLock;
    private final ReleaseStrategy releaseStrategy;

    @Around("@annotation(com.simplelock.mongo.aspect.SimpleMongoLocked)")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SimpleMongoLocked annotation = signature.getMethod().getAnnotation(SimpleMongoLocked.class);

        log.debug("Intercepted method [{}] annotated with [{}]", signature.getMethod().getName(),
                SimpleMongoLocked.class.getSimpleName());

        // Try to acquire lock by using just the method name as unique key
        Optional<String> tokenOptional = simpleLock.acquire(
                signature.getMethod().getName() + "-" + UNIQUE_KEY);

        if (tokenOptional.isPresent()) {
            // Proceed with execution if lock successfully acquired
            Object result = joinPoint.proceed();

            // Instantly release the lock after execution
            if (annotation.releaseStrategy() == ReleaseStrategy.WITHOUT_DELAY
                    && releaseStrategy == ReleaseStrategy.WITHOUT_DELAY) {
                simpleLock.release(tokenOptional.get());
            }

            // Don't explicitly release for MongoDB, instead the index will
            // ensure that the lock document will expire based on the values set
            // in the configuration properties
            return result;
        }

        return null;
    }
}
