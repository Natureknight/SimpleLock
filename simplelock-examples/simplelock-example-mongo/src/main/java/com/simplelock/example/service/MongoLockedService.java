package com.simplelock.example.service;

import com.simplelock.api.SimpleLock;
import com.simplelock.mongo.aspect.SimpleMongoLocked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoLockedService {

    private final SimpleLock simpleLock;

    /**
     * Default release delay (10 seconds).
     */
    @Scheduled(fixedRate = 10000L)
    @SimpleMongoLocked
    public void lockedMethodWithDefaultRelease() {
        log.info("Invoke [lockedMethodWithDefaultRelease] with distributed lock");
    }

    /**
     * Custom release delay after 5000 milliseconds (5 seconds).
     */
    @Scheduled(fixedRate = 5000L)
    @SimpleMongoLocked
    public void lockedMethodWithCustomReleaseDelay() {
        log.info("Invoke [lockedMethodWithCustomReleaseDelay] with distributed lock");
    }

    @Scheduled(fixedRate = 3000L)
    public void lockedMethodWithInstantRelease() {
        simpleLock.acquire("unique-key")
                .ifPresent(token -> {
                    log.info("Invoke [lockedMethodWithInstantRelease] with distributed lock");
                    simpleLock.release(token);
                });
    }
}
