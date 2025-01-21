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
package org.eclipse.jnosql.databases.neo4j.communication;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.neo4j.driver.Session;

import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Neo4JDatabaseManager implements DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(Neo4JDatabaseManager.class.getName());

    private final Session session;
    private final String database;

    public Neo4JDatabaseManager(Session session, String database) {
        this.session = session;
        this.database = database;
    }

    @Override
    public String name() {
        return database;
    }

    @Override
    public CommunicationEntity insert(CommunicationEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        return null;
    }

    @Override
    public CommunicationEntity insert(CommunicationEntity entity, Duration ttl) {
       throw new UnsupportedOperationException("This operation is not supported in Neo4J");
    }

    @Override
    public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return null;
    }

    @Override
    public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities, Duration ttl) {
       throw new UnsupportedOperationException("This operation is not supported in Neo4J");
    }

    @Override
    public CommunicationEntity update(CommunicationEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        return null;
    }

    @Override
    public Iterable<CommunicationEntity> update(Iterable<CommunicationEntity> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return null;
    }

    @Override
    public void delete(DeleteQuery query) {
        Objects.requireNonNull(query, "query is required");

    }

    @Override
    public Stream<CommunicationEntity> select(SelectQuery query) {
        Objects.requireNonNull(query, "query is required");
        return Stream.empty();
    }

    @Override
    public long count(String entity) {
        Objects.requireNonNull(entity, "entity is required");
        return 0;
    }

    @Override
    public void close() {
        LOGGER.fine("Closing the Neo4J session");
        this.session.close();
    }
}
