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

package com.simplelock.example.service.impl;

import com.simplelock.api.SimpleLock;
import com.simplelock.aspect.SimpleJdbcLocked;
import com.simplelock.example.service.ExampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockedExampleService implements ExampleService {

    private final SimpleLock simpleLock;

    @Scheduled(fixedRate = 1000L)
    @SimpleJdbcLocked
    @Override
    public void lockedMethodWithDefaultRelease() {
        log.info("Invoke [lockedMethodWithDefaultRelease] with distributed lock");
    }

    @Scheduled(fixedRate = 1000L)
    @SimpleJdbcLocked(releaseAfter = 5000L, timeUnit = TimeUnit.MILLISECONDS)
    @Override
    public void lockedMethodWithCustomReleaseDelay() {
        log.info("Invoke [lockedMethodWithCustomReleaseDelay] with distributed lock");
    }

    @Scheduled(fixedRate = 1000L)
    @Override
    public void lockedMethodWithInstantRelease() {
        simpleLock.acquireWithKeyPrefix("lockedMethodWithInstantRelease", "unique-key")
                // IMPORTANT: lock has been acquired successfully only if the token is present,
                // so make sure you have this check wrapping your service logic that needs to be locked
                .ifPresent(token -> {
                    log.info("Invoke [lockedMethodWithInstantRelease] with distributed lock");

                    // IMPORTANT: Don't forget to release your lock after execution, unless
                    // your requirement is to have it locked forever for some reason
                    simpleLock.releaseImmediately(token);
                });
    }
}
