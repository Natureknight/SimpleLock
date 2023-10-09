package com.simplelock.mongo;

import com.simplelock.api.SimpleLock;
import com.simplelock.mongo.model.SimpleLockDocument;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.NonNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.runAsync;

@AllArgsConstructor
@Slf4j
public class MongoSimpleLock implements SimpleLock, InitializingBean {

    private static final String MONGO_COLLECTION = "simple_lock";

    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<String> acquire(String key) {
        String token = UUID.randomUUID().toString();
        try {
            mongoTemplate.insert(SimpleLockDocument.builder()
                            .id(new ObjectId())
                            .lockKey(key)
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
        mongoTemplate.remove(new Query(), MONGO_COLLECTION);

        mongoTemplate.indexOps(MONGO_COLLECTION)
                .ensureIndex(new Index()
                        .on("lockKey", Sort.Direction.ASC)
                        .unique());
    }

    @Override
    public Optional<String> acquireWithKeyPrefix(
            @NonNull String keyPrefix,
            @NonNull String key) {
        return acquire(keyPrefix + "-" + key);
    }

    @Override
    public void releaseImmediately(String token) {
        Query query = new Query(Criteria.where("token").is(token));
        mongoTemplate.remove(query, MONGO_COLLECTION);
    }

    @Override
    public void release(String token, long releaseAfter, TimeUnit timeUnit) {
        if (releaseAfter == 0L) {
            log.info("Releasing a lock for token [{}] with delay of 0, using releaseImmediately instead", token);
            releaseImmediately(token);
            return;
        }

        log.debug("Lock will be released for token [{}] after [{}] {}", token,
                releaseAfter,
                timeUnit.toString().toLowerCase(Locale.ROOT));

        runAsync(() -> releaseImmediately(token), delayedExecutor(releaseAfter, timeUnit));
    }
}
