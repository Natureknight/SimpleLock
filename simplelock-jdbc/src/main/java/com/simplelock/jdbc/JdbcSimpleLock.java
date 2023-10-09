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

package com.simplelock.jdbc;

import com.simplelock.api.SimpleLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.simplelock.jdbc.JdbcSimpleLockQuery.ACQUIRE;
import static com.simplelock.jdbc.JdbcSimpleLockQuery.RELEASE;
import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Default implementation of {@link SimpleLock}
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
@AllArgsConstructor
@Slf4j
public class JdbcSimpleLock implements SimpleLock {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<String> acquire(String key) {
        String token = UUID.randomUUID().toString();
        try {
            jdbcTemplate.update(ACQUIRE.getQuery(),
                    UUID.randomUUID().toString(),
                    key,
                    token);
            log.debug("Acquired JDBC simple lock for key [{}]", key);
        } catch (DuplicateKeyException ex) {
            log.warn("Could not acquire Jdbc lock for key [{}], execution will be skipped.", key);
            return Optional.empty();
        }

        return Optional.of(token);
    }

    @Override
    public Optional<String> acquireWithKeyPrefix(
            @NonNull String keyPrefix,
            @NonNull String key) {

        return acquire(keyPrefix + "-" + key);
    }

    @Override
    public void releaseImmediately(String token) {
        log.debug("Lock for token [{}] will be released immediately", token);
        jdbcTemplate.update(RELEASE.getQuery(), token);
    }

    @Override
    public void release(String token, long releaseAfter, TimeUnit timeUnit) {
        if (releaseAfter == 0L) {
            log.info("Releasing a lock for token [{}] with delay of 0, using releaseImmediately instead", token);
            releaseImmediately(token);
            return;
        }

        log.debug("Lock will be released for token [{}] after [{}] {}", token,
                releaseAfter,
                timeUnit.toString().toLowerCase(Locale.ROOT));

        runAsync(() -> jdbcTemplate.update(RELEASE.getQuery(), token),
                delayedExecutor(releaseAfter, timeUnit));
    }
}
