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

package com.simplelock.autoconfigure.jdbc;

import com.simplelock.api.SimpleLock;
import com.simplelock.jdbc.aspect.SimpleJdbcLockedAspect;
import com.simplelock.jdbc.JdbcSimpleLock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Autoconfiguration for {@link SimpleLock} default implementation {@link JdbcSimpleLock}.
 *
 * @author Stanislav Dabov
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(value = "simplelock.jdbc.enabled", havingValue = "true")
@EnableConfigurationProperties(SimpleLockJdbcConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnClass(JdbcTemplate.class)
public class SimpleLockJdbcAutoConfiguration {

    @Bean
    public SimpleJdbcLockedAspect simpleJdbcLockedAspect(final SimpleLock simpleLock) {
        return new SimpleJdbcLockedAspect(simpleLock);
    }

    @ConditionalOnMissingBean
    @Bean
    public SimpleLock simpleLock(JdbcTemplate jdbcTemplate) {
        return new JdbcSimpleLock(jdbcTemplate);
    }

    /**
     * Configuration for init DDL functionality.
     */
    @AutoConfiguration
    @ConditionalOnProperty(value = "simplelock.jdbc.auto-generate-ddl", havingValue = "true")
    @ConditionalOnClass(DataSource.class)
    public static class SimpleJdbcLockInitConfiguration {

        @Bean
        public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
            ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();

            resourceDatabasePopulator.addScript(new ClassPathResource("/sql/simple_lock.sql"));
            DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
            dataSourceInitializer.setDataSource(dataSource);
            dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

            return dataSourceInitializer;
        }
    }
}
