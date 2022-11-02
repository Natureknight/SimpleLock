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

package com.simplelock.api;

import java.util.concurrent.TimeUnit;

/**
 * API for simplifying the way to use {@link SimpleLock} implementations.
 *
 * @author Stanislav Dabov
 * @since 1.0.7
 */
public interface LockRunnableExecutor {

    /**
     * Execute the given {@link Runnable} without holding the lock.
     *
     * @param runnable given {@link Runnable} to execute with a distributed lock
     */
    default void executeLocked(Runnable runnable) {
        executeLocked(runnable, null, 0L, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute the given {@link Runnable} with holding the lock for given milliseconds.
     *
     * @param runnable     given {@link Runnable} to execute with a distributed lock
     * @param key          given lock key, optional
     * @param releaseAfter time period to release the lock afer
     * @param timeUnit     chosen {@link TimeUnit}
     */
    void executeLocked(Runnable runnable, String key, long releaseAfter, TimeUnit timeUnit);
}
