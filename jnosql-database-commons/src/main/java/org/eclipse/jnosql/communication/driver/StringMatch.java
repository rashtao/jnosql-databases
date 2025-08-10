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
package org.eclipse.jnosql.communication.driver;

import java.util.Objects;


/**
 * Represents strategies for matching string values in database queries,
 * typically for SQL {@code LIKE} clauses or NoSQL regex-like searches.
 * <p>
 * Each constant defines a specific way to wrap the given value
 * with wildcard symbols ({@code %}) to produce a matching pattern.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * String pattern = StringMatch.CONTAINS.format("Ota"); // "%Ota%"
 * }</pre>
 */
public enum StringMatch {

    DEFAULT {
        @Override
        public String apply(String value) {
            return value;
        }
    },
    CONTAINS {
        @Override
        public String apply(String value) {
            return "%" + value + "%";
        }
    },
    STARTS_WITH {
        @Override
        public String apply(String value) {
            return value + "%";
        }
    },
    ENDS_WITH {
        @Override
        public String apply(String value) {
            return "%" + value;
        }
    };

    public abstract String apply(String value);

    public String format(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        return apply(value);
    }
}
