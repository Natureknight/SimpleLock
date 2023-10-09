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

package com.simplelock.autoconfigure.jdbc;

import com.simplelock.jdbc.JdbcSimpleLock;
import com.simplelock.jdbc.aspect.SimpleJdbcLockedAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see SimpleLockJdbcAutoConfiguration
 */
class SimpleLockJdbcAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withPropertyValues("spring.datasource.url=jdbc:h2:mem:demo")
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    JdbcTemplateAutoConfiguration.class))
            .withUserConfiguration(SimpleLockJdbcAutoConfiguration.class);

    @DisplayName("When enabled, should register corresponding beans within context")
    @Test
    void contextLoads_simpleJdbcLockEnabled_beansRegistered() {
        runner.withPropertyValues(
                        "simplelock.jdbc.enabled=true",
                        "simplelock.jdbc.auto-generate-ddl=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(SimpleJdbcLockedAspect.class);
                    assertThat(context).hasSingleBean(JdbcSimpleLock.class);
                    assertThat(context).doesNotHaveBean(DataSourceInitializer.class);
                });
    }

    @DisplayName("When cleanup on startup enabled, should register corresponding beans within context")
    @Test
    void contextLoads_autoGenerateDdlEnabled_beansRegistered() {
        runner.withPropertyValues(
                        "simplelock.jdbc.enabled=true",
                        "simplelock.jdbc.auto-generate-ddl=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(SimpleJdbcLockedAspect.class);
                    assertThat(context).hasSingleBean(JdbcSimpleLock.class);
                    assertThat(context).hasSingleBean(DataSourceInitializer.class);
                });
    }
}
