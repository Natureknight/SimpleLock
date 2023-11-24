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

package com.simplelock.jdbc.aspect;

import com.simplelock.api.ReleaseStrategy;
import com.simplelock.api.SimpleLock;
import com.simplelock.jdbc.JdbcSimpleLock;
import com.simplelock.jdbc.common.BaseJdbcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo",
        "simplelock.jdbc.enabled=true",
        "simplelock.jdbc.auto-generate-ddl=true"
})
class SimpleJdbcLockedAspectTest extends BaseJdbcTest {

    private static final String UNIQUE_KEY = "aop-unique-key";

    @Autowired
    private DummyClassUsingAspect dummyClassUsingAspect;

    @SpyBean
    private JdbcSimpleLock jdbcSimpleLock;

    @DisplayName("Verify default values for releaseAfter and timeUnit for lock aspect")
    @Test
    void verifyAnnotatedMethod_acquireLockWithDefaultReleaseDelay() {
        // when
        dummyClassUsingAspect.lockedMethod();

        // then
        verify(jdbcSimpleLock, times(1)).acquire("lockedMethod-" + UNIQUE_KEY);
        verify(jdbcSimpleLock, never()).release(anyString());
    }

    @DisplayName("Acquire lock with 100ms release delay should release the lock after the delay have passed")
    @Test
    void verifyAnnotatedMethod_acquireLockWithCustomReleaseDelay() {
        // when
        dummyClassUsingAspect.lockedMethodWithCustomReleaseDelay();

        // then
        verify(jdbcSimpleLock, times(1)).acquire(
                "lockedMethodWithCustomReleaseDelay-" + UNIQUE_KEY);
        verify(jdbcSimpleLock, times(1)).release(anyString());
        await().atLeast(100L, TimeUnit.MILLISECONDS).until(() -> BaseJdbcTest.lockReleased(jdbcTemplate));
    }

    @DisplayName("When trying to invoke annotated method twice, only first invocation should acquire lock")
    @Test
    void lockCouldNotBeAcquired_skipExecution() {
        // when
        dummyClassUsingAspect.lockedMethod();
        dummyClassUsingAspect.lockedMethod();

        // then
        verify(jdbcSimpleLock, times(2)).acquire("lockedMethod-" + UNIQUE_KEY);
        verify(jdbcSimpleLock, times(2)).acquire("lockedMethod-" + UNIQUE_KEY);
        verify(jdbcSimpleLock, never()).release(anyString());
    }

    @DisplayName("Locked method with instant release of the acquired lock")
    @Test
    void lockedMethodWithInstantRelease_success() {
        // when
        dummyClassUsingAspect.lockedMethodWithInstantRelease();

        verify(jdbcSimpleLock, times(1)).acquire("lockedMethodWithInstantRelease-" + UNIQUE_KEY);
        verify(jdbcSimpleLock, times(1)).release(anyString());
    }

    static class DummyClassUsingAspect {

        @SimpleJdbcLocked(releaseStrategy = ReleaseStrategy.WITH_DELAY)
        public void lockedMethod() {
        }

        @SimpleJdbcLocked
        public void lockedMethodWithCustomReleaseDelay() {
        }

        @SimpleJdbcLocked
        public void lockedMethodWithInstantRelease() {
        }
    }

    @SpringBootApplication
    static class TestApplication {

        @Bean
        public DummyClassUsingAspect dummyClassUsingAspect() {
            return new DummyClassUsingAspect();
        }

        @Bean
        public SimpleJdbcLockedAspect simpleJdbcLockedAspect(SimpleLock simpleLock) {
            return new SimpleJdbcLockedAspect(simpleLock, ReleaseStrategy.WITHOUT_DELAY);
        }
    }
}
