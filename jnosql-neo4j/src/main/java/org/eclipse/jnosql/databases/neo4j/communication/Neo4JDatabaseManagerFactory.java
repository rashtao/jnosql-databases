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
