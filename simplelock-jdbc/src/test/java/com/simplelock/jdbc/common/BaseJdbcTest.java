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

package com.simplelock.jdbc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@Sql(statements = "DROP TABLE simple_lock", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/sql/simple_lock.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DirtiesContext
public abstract class BaseJdbcTest {

    protected static final String SELECT_QUERY = "SELECT * FROM simple_lock";
    protected static final String UNIQUE_KEY = "unique-key";

    protected static final RowMapper<SimpleLockRow> ROW_MAPPER = (rs, rowNum) -> SimpleLockRow.builder()
            .id(rs.getString(1))
            .lockKey(rs.getString(2))
            .token(rs.getString(3))
            .build();

    @SpyBean
    protected JdbcTemplate jdbcTemplate;

    protected static boolean lockReleased(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.queryForObject(SELECT_QUERY, ROW_MAPPER);
            return false;
        } catch (EmptyResultDataAccessException ex) {
            // if the select query return empty result, then
            // we know that the lock has been released
            return true;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    protected static class SimpleLockRow {

        private String id;
        private String lockKey;
        private String token;
    }
}
