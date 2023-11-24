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

import com.simplelock.api.ReleaseStrategy;
import com.simplelock.api.SimpleLock;
import com.simplelock.jdbc.aspect.SimpleJdbcLockedAspect;
import com.simplelock.jdbc.common.BaseJdbcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test")
class JdbcSimpleLockTest extends BaseJdbcTest {

    @Autowired
    private SimpleLock simpleLock;

    @Nested
    class AcquireLockTests {

        @DisplayName("Acquire lock should return optional with token")
        @Test
        void acquireLockOnce_shouldReturnOptionalWithToken() {
            // when
            Optional<String> tokenOptional = simpleLock.acquire(UNIQUE_KEY);

            // then
            assertThat(tokenOptional).isPresent();
            verify(jdbcTemplate, times(1)).update(eq(JdbcSimpleLockQuery.ACQUIRE.getQuery()),
                    anyString(), eq(UNIQUE_KEY), any(LocalDateTime.class), eq(tokenOptional.get()));
        }

        @DisplayName("Acquire lock twice for the same key won't return token for the second attempt")
        @Test
        void acquireLockTwice_shouldReturnEmptyOptionalForSecondInvocation() {
            // when
            Optional<String> tokenOptional1 = simpleLock.acquire(UNIQUE_KEY);
            Optional<String> tokenOptional2 = simpleLock.acquire(UNIQUE_KEY);

            // then
            assertThat(tokenOptional1).isPresent();
            assertThat(tokenOptional2).isEmpty();
            verify(jdbcTemplate, times(1)).update(eq(JdbcSimpleLockQuery.ACQUIRE.getQuery()),
                    anyString(), eq(UNIQUE_KEY), any(LocalDateTime.class), eq(tokenOptional1.get()));
        }
    }

    @Nested
    class ReleaseLockTests {

        @DisplayName("Acquire and instantly release the lock, should delete lock record from DB #1")
        @Test
        void releaseLockWithNoExplicitInstantRelease_successful() {
            // given
            var tokenOptional = simpleLock.acquire(UNIQUE_KEY);
            assertTrue(tokenOptional.isPresent());

            // when
            simpleLock.release(tokenOptional.get());

            // then
            assertTrue(lockReleased(jdbcTemplate));
        }

        @DisplayName("Acquire and instantly release the lock, should delete lock record from DB #2")
        @Test
        void releaseLockWithExplicitInstantRelease_successful() {
            // given
            var tokenOptional = simpleLock.acquire(UNIQUE_KEY);
            assertTrue(tokenOptional.isPresent());

            // when
            simpleLock.release(tokenOptional.get());

            // then
            assertTrue(lockReleased(jdbcTemplate));
        }
    }

    @SpringBootApplication
    static class JdbcSimpleLockTestApplication {

        @Bean
        public SimpleLock simpleLock(JdbcTemplate jdbcTemplate) {
            return new JdbcSimpleLock(jdbcTemplate);
        }

        @Bean
        public SimpleJdbcLockedAspect simpleJdbcLockedAspect(SimpleLock simpleLock) {
            return new SimpleJdbcLockedAspect(simpleLock, ReleaseStrategy.WITHOUT_DELAY);
        }
    }
}
