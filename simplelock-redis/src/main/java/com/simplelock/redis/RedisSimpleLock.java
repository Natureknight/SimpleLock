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

package com.simplelock.redis;

import com.simplelock.api.SimpleLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Slf4j
public class RedisSimpleLock implements SimpleLock {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final long delay;
    private final TimeUnit timeUnit;

    @Override
    public Optional<String> acquire(String key) {

        String token = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
        Boolean result = delay == 0
                ? redisTemplate.opsForValue().setIfAbsent(token, key)
                : redisTemplate.opsForValue().setIfAbsent(token, key, delay, timeUnit);

        if (Boolean.TRUE.equals(result)) {
            return Optional.of(token);
        }

        log.warn("Could not acquire Redis lock for key [{}], execution will be skipped.", key);
        return Optional.empty();
    }

    @Override
    public void release(String token) {
        redisTemplate.delete(token);
    }
}
