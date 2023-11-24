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

package com.simplelock.mongo;

import com.simplelock.api.SimpleLock;
import com.simplelock.mongo.model.SimpleLockDocument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Slf4j
public class MongoSimpleLock implements SimpleLock, InitializingBean {

    public static final String EXPIRY_INDEX_NAME = "idx_created_at_ttl";

    private static final String MONGO_COLLECTION = "simple_lock";

    private final MongoTemplate mongoTemplate;
    private final long expiry;
    private final TimeUnit timeUnit;

    @Override
    public Optional<String> acquire(String key) {
        String token = UUID.randomUUID().toString();
        try {
            mongoTemplate.insert(SimpleLockDocument.builder()
                            .lockKey(key)
                            .createdAt(LocalDateTime.now(Clock.systemUTC()))
                            .token(token)
                            .build(),
                    MONGO_COLLECTION);
            log.debug("Acquired Mongo simple lock for key [{}]", key);
        } catch (Exception ex) {
            log.warn("Could not acquire Mongo lock for key [{}], execution will be skipped.", key);
            return Optional.empty();
        }

        return Optional.of(token);
    }

    /**
     * Cleanup on startup.
     */
    @Override
    public void afterPropertiesSet() {
        ensureLockKeyUniqueIndex();
        cleanupExistingTtlIndex();
        ensureCreatedAtTtlIndex();
    }

    @Override
    public void release(String token) {
        log.debug("Releasing MongoDB lock by token {} immediately", token);
        Query query = new Query(Criteria.where("token").is(token));
        mongoTemplate.remove(query, MONGO_COLLECTION);
    }

    private void ensureLockKeyUniqueIndex() {
        mongoTemplate.indexOps(MONGO_COLLECTION)
                .ensureIndex(new Index()
                        .on("lockKey", Sort.Direction.ASC)
                        .unique());
    }

    private void ensureCreatedAtTtlIndex() {
        mongoTemplate.indexOps(MONGO_COLLECTION)
                .ensureIndex(new Index()
                        .named(EXPIRY_INDEX_NAME)
                        .on("createdAt", Sort.Direction.ASC)
                        .expire(expiry, timeUnit));
    }

    private void cleanupExistingTtlIndex() {
        mongoTemplate.indexOps(MONGO_COLLECTION).getIndexInfo()
                .stream()
                .filter(ii -> EXPIRY_INDEX_NAME.equals(ii.getName()))
                .findFirst()
                .ifPresent(ii -> mongoTemplate.indexOps(MONGO_COLLECTION).dropIndex(ii.getName()));
    }
}
