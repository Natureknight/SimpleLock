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
import com.simplelock.exception.SimpleLockAcquireException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.simplelock.core.JdbcSimpleLockQuery.ACQUIRE;
import static com.simplelock.core.JdbcSimpleLockQuery.RELEASE;

/**
 * Default implementation of {@link SimpleLock}
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class JdbcSimpleLock implements SimpleLock {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String acquire(String key) throws SimpleLockAcquireException {
        String token = UUID.randomUUID().toString();
        try {
            jdbcTemplate.update(ACQUIRE.getQuery(),
                    UUID.randomUUID().toString(),
                    key,
                    token);
        } catch (Exception ex) {
            throw new SimpleLockAcquireException("Could not acquire lock for key: " + key, ex);
        }

        return token;
    }

    @Override
    public void release(String token, int delayInMillis) {
        executeWithDelay(() -> jdbcTemplate.update(RELEASE.getQuery(), token), delayInMillis);
    }

    private void executeWithDelay(Runnable runnable, int delayInMillis) {
        if (delayInMillis == 0) {
            runnable.run();
            return;
        }

        Executors.newSingleThreadScheduledExecutor().schedule(runnable,
                delayInMillis,
                TimeUnit.MILLISECONDS);
    }
}
