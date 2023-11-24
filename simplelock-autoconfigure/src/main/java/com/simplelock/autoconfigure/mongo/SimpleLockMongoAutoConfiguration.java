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

package com.simplelock.autoconfigure.mongo;

import com.simplelock.api.SimpleLock;
import com.simplelock.mongo.MongoSimpleLock;
import com.simplelock.mongo.aspect.SimpleMongoLockedAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.core.MongoTemplate;

@AutoConfiguration
@ConditionalOnProperty(value = "simplelock.mongo.enabled", havingValue = "true")
@EnableConfigurationProperties(SimpleLockMongoConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnClass(MongoTemplate.class)
public class SimpleLockMongoAutoConfiguration {

    @Bean
    public SimpleMongoLockedAspect simpleJdbcLockedAspect(
            SimpleLock simpleLock,
            SimpleLockMongoConfigurationProperties properties) {
        return new SimpleMongoLockedAspect(simpleLock, properties.getExpiry().getReleaseStrategy());
    }

    @ConditionalOnMissingBean
    @Bean
    public SimpleLock simpleLock(
            MongoTemplate mongoTemplate,
            SimpleLockMongoConfigurationProperties properties) {

        return new MongoSimpleLock(mongoTemplate,
                properties.getExpiry().getMinDelay(),
                properties.getExpiry().getTimeUnit()
        );
    }
}
