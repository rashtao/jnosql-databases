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

import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;

import java.util.Objects;

public class Neo4JDatabaseManagerFactory implements DatabaseManagerFactory {

    private final Neo4Property property;

    Neo4JDatabaseManagerFactory(Neo4Property property) {
        this.property = property;
    }

    @Override
    public void close() {

    }

    @Override
    public DatabaseManager apply(String database) {
        Objects.requireNonNull(database, "database is required");
        return null;
    }
}
