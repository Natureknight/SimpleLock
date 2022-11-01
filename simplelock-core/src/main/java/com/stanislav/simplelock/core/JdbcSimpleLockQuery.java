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

package com.stanislav.simplelock.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration holding the JDBC queries.
 *
 * @author Stanislav Dabov
 * @since 1.0.6
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
enum JdbcSimpleLockQuery {

    /**
     * Query to acquire the lock. Could fail with UK constraint violation on `lock_key` column.
     */
    ACQUIRE("INSERT INTO simple_lock (id, lock_key, token) VALUES (?, ?, ?)"),

    /**
     * Release the lock by given token, which is the result from acquire operation.
     */
    RELEASE("DELETE FROM simple_lock WHERE token = ?"),
    ;

    @Getter
    private final String query;
}
