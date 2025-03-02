/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.databases.orientdb.mapping;


import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.databases.orientdb.communication.OrientDBLiveCallback;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;

import java.util.Map;
import java.util.stream.Stream;

/**
 * A specialized {@link DocumentTemplate} for OrientDB.
 * <p>
 * This template provides methods for executing native SQL queries,
 * live queries, and queries with named parameters.
 * </p>
 */
public interface OrientDBTemplate extends DocumentTemplate {

    /**
     * Executes a native OrientDB SQL query.
     * <p>
     * This method allows running SQL queries with positional parameters.
     * Example usage:
     * <pre>
     * {@code
     * Stream<User> users = template.sql("SELECT FROM User WHERE age > ?", 30);
     * }
     * </pre>
     * </p>
     *
     * @param <T>    the expected result type
     * @param query  the SQL query string
     * @param params optional positional parameters for the query
     * @return a stream of results matching the query
     * @throws NullPointerException if the query or params are null
     */
    <T> Stream<T> sql(String query, Object... params);

    /**
     * Executes a native OrientDB SQL query with named parameters.
     * <p>
     * Example usage:
     * <pre>
     * {@code
     * Map<String, Object> params = Map.of("age", 30);
     * Stream<User> users = template.sql("SELECT FROM User WHERE age > :age", params);
     * }
     * </pre>
     * </p>
     *
     * @param <T>    the expected result type
     * @param query  the SQL query string
     * @param params a map of named parameters for the query
     * @return a stream of results matching the query
     * @throws NullPointerException if the query or params are null
     */
    <T> Stream<T> sql(String query, Map<String, Object> params);

    /**
     * Executes a live query in OrientDB.
     * <p>
     * A live query listens for real-time changes in the database and triggers callbacks
     * for each event that occurs (insert, update, delete).
     * Example usage:
     * <pre>
     * {@code
     * template.live(selectQuery, event -> System.out.println("Update: " + event));
     * }
     * </pre>
     * </p>
     *
     * @param <T>      the expected result type
     * @param query    the query definition using {@link SelectQuery}
     * @param callBacks callback to handle live query events
     * @throws NullPointerException if either query or callBacks is null
     */
    <T> void live(SelectQuery query, OrientDBLiveCallback<T> callBacks);

    /**
     * Executes a live query in OrientDB using a SQL string.
     * <p>
     * The query must include the "LIVE" keyword.
     * Example usage:
     * <pre>
     * {@code
     * template.live("LIVE SELECT FROM User", event -> System.out.println("User changed: " + event));
     * }
     * </pre>
     * </p>
     *
     * @param <T>      the expected result type
     * @param query    the SQL query string containing the "LIVE" keyword
     * @param callBacks callback to handle live query events
     * @param params   optional positional parameters for the query
     * @throws NullPointerException if either query or callBacks is null
     */
    <T> void live(String query, OrientDBLiveCallback<T> callBacks, Object... params);
}
