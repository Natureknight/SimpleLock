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
import com.simplelock.redis.aspect.SimpleRedisLocked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockedService {

    private final SimpleLock simpleLock;

    /**
     * Default release delay (10 seconds).
     */
    @Scheduled(fixedRate = 10000L)
    @SimpleRedisLocked
    public void lockedMethodWithDefaultRelease() {
        log.info("Invoke [lockedMethodWithDefaultRelease] with distributed lock");
    }

    /**
     * Custom release delay after 5000 milliseconds (5 seconds).
     */
    @Scheduled(fixedRate = 5000L)
    @SimpleRedisLocked
    public void lockedMethodWithCustomReleaseDelay() {
        log.info("Invoke [lockedMethodWithCustomReleaseDelay] with distributed lock");
    }

    @Scheduled(fixedRate = 3000L)
    public void lockedMethodWithInstantRelease() {
        simpleLock.acquire("unique-key")
                .ifPresent(token -> {
                    log.info("Invoke [lockedMethodWithInstantRelease] with distributed lock");
                    simpleLock.release(token);
                });
    }
}
