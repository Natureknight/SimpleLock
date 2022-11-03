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

package com.simplelock.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class BaseSimpleLockTest {

    protected static final String SELECT_QUERY = "SELECT * FROM simple_lock";
    protected static final String UNIQUE_KEY = "unique-key";

    @SpyBean
    protected JdbcTemplate jdbcTemplate;

    protected final RowMapper<SimpleLockRow> rowMapper = (rs, rowNum) -> SimpleLockRow.builder()
            .id(rs.getString(1))
            .lockKey(rs.getString(2))
            .token(rs.getString(3))
            .build();

    protected SimpleLockRow getSimpleLockRow() {
        return jdbcTemplate.queryForObject(SELECT_QUERY, rowMapper);
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
