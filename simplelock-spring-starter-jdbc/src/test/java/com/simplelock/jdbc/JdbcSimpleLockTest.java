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

package com.simplelock.jdbc;

import com.simplelock.api.SimpleLock;
import com.simplelock.common.BaseJdbcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo",
        "simplelock.jdbc.enabled=true",
        "simplelock.jdbc.cleanup-on-startup=true",
        "simplelock.jdbc.auto-generate-ddl=true"
})
public class JdbcSimpleLockTest extends BaseJdbcTest {

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
                    anyString(), eq(UNIQUE_KEY), eq(tokenOptional.get()));
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
                    anyString(), eq(UNIQUE_KEY), eq(tokenOptional1.get()));
        }
    }

    @Nested
    class ReleaseLockTests {

        @DisplayName("Acquire and instantly release the lock, should delete lock record from DB")
        @Test
        void instantReleaseLock_successful() {
            // when
            String token = simpleLock.acquire(UNIQUE_KEY).orElseThrow();

            // then
            SimpleLockRow result = jdbcTemplate.queryForObject(SELECT_QUERY, rowMapper);
            assertNotNull(result);
            assertEquals(token, result.getToken());
            simpleLock.release(token, 0L, TimeUnit.MILLISECONDS);
            assertThrows(EmptyResultDataAccessException.class,
                    () -> jdbcTemplate.queryForObject(SELECT_QUERY, rowMapper));
        }

        @DisplayName("Acquire and release the lock with delay, should delete lock record from DB after the delay")
        @Test
        void releaseLockWithDelay_successful() {
            // when
            String token = simpleLock.acquire(UNIQUE_KEY).orElseThrow();

            // then
            SimpleLockRow result = jdbcTemplate.queryForObject(SELECT_QUERY, rowMapper);
            assertNotNull(result);
            assertEquals(token, result.getToken());
            simpleLock.release(token, 100L, TimeUnit.MILLISECONDS);
            result = jdbcTemplate.queryForObject(SELECT_QUERY, rowMapper);
            assertNotNull(result);
            assertEquals(token, result.getToken());
            await().atLeast(100L, TimeUnit.MILLISECONDS).until(lockReleased());
        }
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
