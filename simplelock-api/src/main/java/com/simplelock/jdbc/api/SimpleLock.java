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

package com.simplelock.jdbc.api;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Common interface to define API for locking.
 * Provide your own implementation in your configuration in order to override the default behaviour.
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
public interface SimpleLock {

    /**
     * Acquire the lock and return unique token as {@link java.util.UUID UUID} to be used for
     * releasing the lock later on.
     *
     * @param key given unique key of the lock.
     * @return the lock token
     */
    Optional<String> acquire(String key);

    /**
     * Similar to {@link SimpleLock#acquire(String)}, but will also include the key prefix
     * in the key so same unique key could be used, but it will behave like a different lock
     * if e.g. invoked from different methods.
     *
     * @param keyPrefix given key prefix e.g. invocation method name
     * @param key       given unique key suffix of the lock
     * @return the lock token
     */
    Optional<String> acquireWithKeyPrefix(String keyPrefix, String key);

    /**
     * Release the lock with the default delay by given token
     *
     * @param token given token
     */
    default void release(String token) {
        release(token, 10L, TimeUnit.SECONDS);
    }

    /**
     * Release the lock immediately
     *
     * @param token given token
     */
    void releaseImmediately(String token);

    /**
     * Release the lock by given lock token. Lock will be released synchronously in case
     * the {@code releaseAfter} is set to zero, and asynchronously if {@code releaseAfter} is
     * bigger than zero.
     *
     * @param token        lock token to unlock
     * @param releaseAfter time period until we release the lock
     * @param timeUnit     chosen {@link TimeUnit}
     */
    void release(String token, long releaseAfter, TimeUnit timeUnit);
}
