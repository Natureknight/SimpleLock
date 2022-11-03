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
import com.simplelock.common.BaseSimpleLockTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo;MODE=MySQL",
        "simplelock.jdbc.enabled=true",
        "simplelock.jdbc.cleanup-on-startup=true",
        "simplelock.jdbc.auto-generate-ddl=true"
})
@Sql(statements = "TRUNCATE TABLE simple_lock", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DirtiesContext
public class JdbcSimpleLockTest extends BaseSimpleLockTest {

    @Autowired
    private SimpleLock simpleLock;

    @Nested
    class AcquireLockTests {

        @Test
        void acquireLockOnce_shouldReturnOptionalWithToken() {
            // when
            Optional<String> tokenOptional = simpleLock.acquire(UNIQUE_KEY);

            // then
            assertThat(tokenOptional).isPresent();
            verify(jdbcTemplate, times(1)).update(eq(JdbcSimpleLockQuery.ACQUIRE.getQuery()),
                    anyString(), eq(UNIQUE_KEY), eq(tokenOptional.get()));
        }

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

        @Test
        void instantReleaseLock_successful() {
            // when
            String token = simpleLock.acquire(UNIQUE_KEY).orElseThrow();

            // then
            SimpleLockRow result = getSimpleLockRow();
            assertNotNull(result);
            simpleLock.release(token);
            result = getSimpleLockRow();
            assertNotNull(result);
        }

        @Test
        void releaseLockWithDelay_successful() throws InterruptedException {
            // when
            String token = simpleLock.acquire(UNIQUE_KEY).orElseThrow();

            // then - release with 100 ms delay
            SimpleLockRow result = getSimpleLockRow();
            assertNotNull(result);
            assertEquals(token, result.getToken());

            simpleLock.release(token, 100L, TimeUnit.MILLISECONDS);
            // select after release schedule
            result = getSimpleLockRow();
            assertNotNull(result);
            assertEquals(token, result.getToken());

            Thread.sleep(500);
            // select after some time, should not have records
            assertThrows(EmptyResultDataAccessException.class, JdbcSimpleLockTest.this::getSimpleLockRow);
        }
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
