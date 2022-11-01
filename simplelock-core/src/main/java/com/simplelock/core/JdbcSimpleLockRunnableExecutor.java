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

import com.simplelock.api.LockRunnableExecutor;
import com.simplelock.api.SimpleLock;
import lombok.RequiredArgsConstructor;

import static java.util.Objects.nonNull;

/**
 * Default implementation of {@link LockRunnableExecutor} for JDBC distributed locks.
 *
 * @author Stanislav Dabov
 * @since 1.0.7
 */
@RequiredArgsConstructor
public class JdbcSimpleLockRunnableExecutor implements LockRunnableExecutor {

    private static final String UNIQUE_KEY = "unique-key-db37d712-c1e7-45b7-835c-f24b2a526fb9";

    private final SimpleLock simpleLock;

    @Override
    public void executeLocked(Runnable runnable, int delayInMillis) {
        String token = null;
        try {
            token = simpleLock.acquire(UNIQUE_KEY);
            runnable.run();
        } finally {
            if (nonNull(token)) {
                simpleLock.release(token, delayInMillis);
            }
        }
    }
}
