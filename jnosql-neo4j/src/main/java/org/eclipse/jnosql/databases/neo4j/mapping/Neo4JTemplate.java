/*
 *
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
 *
 */
package org.eclipse.jnosql.databases.neo4j.mapping;

import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Neo4JTemplate extends SemiStructuredTemplate {

    <T> Stream<T> executeQuery(String cypher, Map<String, Object> parameters);

    <T> Stream<T> traverse(String startNodeId, String relationship, int depth);

    <T> Stream<T> traverse(String startNodeId, Supplier<String> relationship, int depth);

    <T, E> void edge(T source, String relationshipType, E target);

    <T, E> void edge(T source, Supplier<String> relationship, E target);

    <T, E> void remove(T source, String relationshipType, E target);

    <T, E> void remove(T source, Supplier<String> relationship, E target);
}
