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

package com.simplelock.example.service;

import com.simplelock.api.SimpleLock;
import com.simplelock.jdbc.aspect.SimpleJdbcLocked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Example locked service implementation
 *
 * @author Stanislav Dabov
 * @since 1.6.1
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcLockedService {

    private final SimpleLock simpleLock;

    /**
     * Default release delay (10 seconds).
     */
    @Scheduled(fixedRate = 1000L)
    @SimpleJdbcLocked
    public void lockedMethodWithDefaultRelease() {
        log.info("Invoke [lockedMethodWithDefaultRelease] with distributed lock");
    }

    /**
     * Custom release delay after 5000 milliseconds (5 seconds).
     */
    @Scheduled(fixedRate = 1000L)
    @SimpleJdbcLocked(releaseAfter = 5000L, timeUnit = TimeUnit.MILLISECONDS)
    public void lockedMethodWithCustomReleaseDelay() {
        log.info("Invoke [lockedMethodWithCustomReleaseDelay] with distributed lock");
    }

    /**
     * Note that the lock could be released instantly after execution also
     * by using the {@link SimpleJdbcLocked}'s attribute {@link SimpleJdbcLocked#releaseAfter()} to 0.
     */
    @Scheduled(fixedRate = 1000L)
    public void lockedMethodWithInstantRelease() {
        simpleLock.acquireWithKeyPrefix("lockedMethodWithInstantRelease", "unique-key")
                .ifPresent(token -> {
                    log.info("Invoke [lockedMethodWithInstantRelease] with distributed lock");
                    // Control over the delay programmatically, e.g:
                    // simpleLock.release(token, 5L, TimeUnit.HOURS);
                    simpleLock.releaseImmediately(token);
                });
    }
}
