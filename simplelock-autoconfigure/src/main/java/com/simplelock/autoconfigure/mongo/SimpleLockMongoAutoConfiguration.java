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
    public SimpleMongoLockedAspect simpleJdbcLockedAspect(final SimpleLock simpleLock) {
        return new SimpleMongoLockedAspect(simpleLock);
    }

    @ConditionalOnMissingBean
    @Bean
    public SimpleLock simpleLock(MongoTemplate mongoTemplate) {
        return new MongoSimpleLock(mongoTemplate);
    }
}
