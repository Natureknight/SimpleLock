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

package com.simplelock.aspect;

import com.simplelock.api.SimpleLock;
import com.simplelock.common.BaseSimpleLockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo;MODE=MySQL",
        "simplelock.jdbc.enabled=true",
        "simplelock.jdbc.auto-generate-ddl=true"
})
@DirtiesContext
public class SimpleJdbcLockedAspectTest extends BaseSimpleLockTest {

    @Autowired
    private DummyClassUsingAspect dummyClassUsingAspect;

    @SpyBean
    private SimpleLock simpleLock;

    @DisplayName("Verify default values for releaseAfter and timeUnit for lock aspect")
    @Test
    void verifyAnnotatedMethod_acquireLockWithDefaultReleaseDelay() throws InterruptedException {
        // when
        dummyClassUsingAspect.lockedMethod();

        // then
        verify(simpleLock, times(1)).acquireForCurrentMethod("lockedMethod");
        verify(simpleLock, times(1)).release(anyString(), eq(10L), eq(TimeUnit.SECONDS));
    }

    @DisplayName("Acquire lock with custom delay should release the lock after the delay have passed")
    @Test
    void verifyAnnotatedMethod_acquireLockWithCustomReleaseDelay() throws InterruptedException {
        // when
        dummyClassUsingAspect.lockedMethodWithCustomReleaseDelay();

        // then
        verify(simpleLock, times(1)).acquireForCurrentMethod("lockedMethodWithCustomReleaseDelay");
        verify(simpleLock, never()).release(anyString(), anyInt(), any(TimeUnit.class));
        Thread.sleep(500);
        verify(simpleLock, times(1)).release(anyString(), eq(100L), eq(TimeUnit.MILLISECONDS));
    }

    static class DummyClassUsingAspect {

        @SimpleJdbcLocked
        public void lockedMethod() {
        }

        @SimpleJdbcLocked(releaseAfter = 100L, timeUnit = TimeUnit.MILLISECONDS)
        public void lockedMethodWithCustomReleaseDelay() {
        }
    }

    @TestConfiguration
    static class AspectTestConfiguration {

        @Bean
        public DummyClassUsingAspect dummyClassUsingAspect() {
            return new DummyClassUsingAspect();
        }
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
