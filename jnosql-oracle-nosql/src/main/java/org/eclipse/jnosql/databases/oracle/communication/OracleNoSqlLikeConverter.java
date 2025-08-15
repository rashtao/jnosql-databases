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

import java.util.regex.Pattern;

enum OracleNoSqlLikeConverter {
    INSTANCE;

    static String convert(Object value) {

        String like = value == null ? null : value.toString();

        if (like == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(like.length());
        StringBuilder literal = new StringBuilder();

        for (int i = 0; i < like.length(); i++) {
            char c = like.charAt(i);
            if (c == '%' || c == '_') {
                if (!literal.isEmpty()) {
                    out.append(Pattern.quote(literal.toString()));
                    literal.setLength(0);
                }
                out.append(c == '%' ? ".*" : ".");
            } else {
                literal.append(c);
            }
        }
        if (!literal.isEmpty()) {
            out.append(Pattern.quote(literal.toString()));
        }
        return out.toString();
    }
}
