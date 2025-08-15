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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OracleNoSqlLikeConverterTest {

    @ParameterizedTest(name = "LIKE \"{0}\" -> pattern \"{1}\"")
    @CsvSource(textBlock = """
        %Ota%;.*\\QOta\\E.*
        Ota%;\\QOta\\E.*
        %Ota;.*\\QOta\\E
        Ota;\\QOta\\E
        Ot_;\\QOt\\E.
        _ta;.\\Qta\\E
        %t_a%;.*\\Qt\\E.\\Qa\\E.*
        .+;\\Q.+\\E
        """, delimiter = ';')
    @DisplayName("Converts SQL LIKE to Oracle NoSQL regex_like pattern (%, _ and literal quoting)")
    void shouldConvertSqlLikeToOracleNoSqlRegex(String like, String expected) {
        String actual = OracleNoSqlLikeConverter.convert(like);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Returns empty string for null input")
    void shouldReturnEmptyForNull() {
        assertThat(OracleNoSqlLikeConverter.convert(null)).isEqualTo("");
    }

    @Test
    @DisplayName("Returns empty string for empty input")
    void shouldReturnEmptyForEmptyString() {
        assertThat(OracleNoSqlLikeConverter.convert("")).isEqualTo("");
    }
}