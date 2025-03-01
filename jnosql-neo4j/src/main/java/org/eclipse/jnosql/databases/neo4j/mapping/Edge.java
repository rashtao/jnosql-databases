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

import java.util.Objects;

/**
 * Represents an edge in a graph database, linking a source entity to a target entity
 * through a specified relationship type.
 *
 * <p>This class models relationships between nodes in a Neo4J database, where each
 * edge is defined by a source node, a target node, and a relationship type.</p>
 *
 * <p>Edges are immutable and ensure that a valid relationship exists between two entities.</p>
 *
 * @param <T> The entity type representing the source node.
 * @param <E> The entity type representing the target node.
 */
public class Edge<T, E> {

    private final T source;
    private final E target;
    private final String relationship;

    private Edge(T source, E target, String relationship) {
        this.source = source;
        this.target = target;
        this.relationship = relationship;
    }

    /**
     * Retrieves the source entity of the edge.
     *
     * @return The source entity.
     */
    public T source() {
        return source;
    }

    /**
     * Retrieves the target entity of the edge.
     *
     * @return The target entity.
     */
    public E target() {
        return target;
    }

    /**
     * Retrieves the relationship type of the edge.
     *
     * @return The relationship type.
     */
    public String relationship() {
        return relationship;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge<?, ?> edge = (Edge<?, ?>) o;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target) && Objects.equals(relationship, edge.relationship);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, relationship);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source +
                ", target=" + target +
                ", relationship='" + relationship + '\'' +
                '}';
    }

    static <T, E> Edge<T, E> of(T source, E target, String relationship) {
        return new Edge<>(source, target, relationship);
    }
}
