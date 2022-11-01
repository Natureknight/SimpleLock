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

import com.simplelock.exception.SimpleLockAcquireException;

/**
 * Common interface to define API for locking.
 * Provide your own implementation in your configuration in order to override the default behaviour.
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
public interface SimpleLock {

    /**
     * Acquire the lock and return unique token as {@link java.util.UUID UUID} to be used for unlocking later on.
     *
     * @param key given unique key of the lock.
     * @return the lock token
     */
    String acquire(String key) throws SimpleLockAcquireException;

    /**
     * Release the lock by given lock token.
     *
     * @param token         lock token to unlock
     * @param delayInMillis time period until we release the lock in
     *                      {@link java.util.concurrent.TimeUnit#MILLISECONDS milliseconds}
     */
    void release(String token, int delayInMillis);
}
