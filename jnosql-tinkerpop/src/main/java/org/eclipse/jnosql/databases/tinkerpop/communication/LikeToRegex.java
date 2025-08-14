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
package org.eclipse.jnosql.databases.tinkerpop.communication;

import java.util.regex.Pattern;

public enum LikeToRegex {
    INSTANCE;


    private static String LikeToRegex(String likePattern) {
        if (likePattern == null) {
            return "a^";
        } // match nothing
        StringBuilder sb = new StringBuilder("^");
        for (char c : likePattern.toCharArray()) {
            switch (c) {
                case '%': sb.append(".*"); break;
                case '_': sb.append('.'); break;
                default: sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        sb.append('$');
        return sb.toString();
    }

}
