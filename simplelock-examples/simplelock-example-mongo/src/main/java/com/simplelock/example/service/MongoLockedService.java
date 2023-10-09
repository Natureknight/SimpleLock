package com.simplelock.example.service;

import com.simplelock.api.SimpleLock;
import com.simplelock.mongo.aspect.SimpleMongoLocked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoLockedService {

    private final SimpleLock simpleLock;

    /**
     * Default release delay (10 seconds).
     */
    @Scheduled(fixedRate = 1000L)
    @SimpleMongoLocked
    public void lockedMethodWithDefaultRelease() {
        log.info("Invoke [lockedMethodWithDefaultRelease] with distributed lock");
    }

    /**
     * Custom release delay after 5000 milliseconds (5 seconds).
     */
    @Scheduled(fixedRate = 1000L)
    @SimpleMongoLocked(releaseAfter = 5000L, timeUnit = TimeUnit.MILLISECONDS)
    public void lockedMethodWithCustomReleaseDelay() {
        log.info("Invoke [lockedMethodWithCustomReleaseDelay] with distributed lock");
    }

    /**
     * Note that the lock could be released instantly after execution also
     * by using the {@link SimpleMongoLocked}'s attribute {@link SimpleMongoLocked#releaseAfter()} to 0.
     */
    @Scheduled(fixedRate = 1000L)
    public void lockedMethodWithInstantRelease() {
        simpleLock.acquireWithKeyPrefix("lockedMethodWithInstantRelease", "unique-key")
                .ifPresent(token -> {
                    log.info("Invoke [lockedMethodWithInstantRelease] with distributed lock");
                    // Control over the delay programmatically, e.g:
                    // simpleLock.release(token, 5L, TimeUnit.HOURS);
                    simpleLock.releaseImmediately(token);
                });
    }
}
