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

import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.SessionConfig;

import java.util.Objects;
import java.util.logging.Logger;

public class Neo4JDatabaseManagerFactory implements DatabaseManagerFactory {

    private static final Logger LOGGER = Logger.getLogger(Neo4JDatabaseManagerFactory.class.getName());

    private final Driver driver;

    private Neo4JDatabaseManagerFactory(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void close() {
        LOGGER.info("Closing the Neo4J driver");
        this.driver.close();
    }

    @Override
    public Neo4JDatabaseManager apply(String database) {
        Objects.requireNonNull(database, "database is required");
        LOGGER.fine(() -> "Creating a new instance of Neo4JDatabaseManager with the database: " + database);
        var session = driver.session(SessionConfig.builder().withDatabase(database).build());
        return new Neo4JDatabaseManager(session);
    }

    static Neo4JDatabaseManagerFactory of(Neo4Property property) {
        Objects.requireNonNull(property, "property is required");
        LOGGER.fine(() -> "Creating a new instance of Neo4JDatabaseManagerFactory with the uri: " + property.uri());
        AuthToken basic = AuthTokens.basic(property.user(), property.password());
        return new Neo4JDatabaseManagerFactory(GraphDatabase.driver(property.uri(), basic));
    }
}
