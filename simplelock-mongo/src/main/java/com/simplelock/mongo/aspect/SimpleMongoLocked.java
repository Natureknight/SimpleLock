package com.simplelock.mongo.aspect;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleMongoLocked {

    /**
     * Whether to release lock immediately. This flag will effectively
     * ignore if the flags below are also set.
     *
     * @return whether to release the lock immediately after execution
     */
    boolean releaseImmediately() default false;

    /**
     * Default to {@link Long long}. If set to 0, the lock will be
     * released immediately.
     *
     * @return the period to hold the lock for.
     */
    long releaseAfter() default 10L;

    /**
     * Default to {@link TimeUnit#SECONDS}
     *
     * @return the chosen {@link TimeUnit}
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
