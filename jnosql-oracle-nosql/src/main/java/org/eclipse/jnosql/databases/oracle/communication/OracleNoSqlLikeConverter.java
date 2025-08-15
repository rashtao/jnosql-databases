/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.databases.oracle.communication;

import java.util.Set;
import java.util.regex.Pattern;

enum OracleNoSqlLikeConverter {
    INSTANCE;

    // Regex metacharacters that must be escaped for Oracle NoSQL regex_like
    private static final Set<Character> META = Set.of(
            '.', '^', '$', '*', '+', '?', '(', ')', '[', ']', '{', '}', '\\', '|'
    );

    /**
     * SQL LIKE (%, _) -> Oracle NoSQL regex_like pattern.
     * Examples:
     *   "Lu%"   -> "Lu.*"
     *   "%Lu"   -> ".*Lu"
     *   "%Lu%"  -> ".*Lu.*"
     *   "Lu"    -> "Lu"        // exact match equivalent in regex_like
     *   "a.c"   -> "a\\.c"     // '.' escaped
     */
    static String convert(Object value) {
        if (value == null) return ""; // let caller decide behavior for empty
        String like = value.toString();
        StringBuilder out = new StringBuilder(like.length());

        for (int i = 0; i < like.length(); i++) {
            char c = like.charAt(i);
            switch (c) {
                case '%': out.append(".*"); break; // zero or more
                case '_': out.append('.');  break; // exactly one
                default:
                    if (META.contains(c)) out.append('\\');
                    out.append(c);
            }
        }
        return out.toString();
    }
}
