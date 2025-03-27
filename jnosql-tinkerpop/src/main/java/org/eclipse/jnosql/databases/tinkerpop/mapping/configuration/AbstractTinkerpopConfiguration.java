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
package org.eclipse.jnosql.databases.tinkerpop.mapping.configuration;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.eclipse.jnosql.databases.tinkerpop.communication.TinkerpopGraphDatabaseManager;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * An abstract configuration class that serves as an adapter to integrate Apache TinkerPop's {@link Graph}
 * with Eclipse JNoSQL. This class implements a template method pattern to provide a standardized way
 * to create and manage graph database instances.
 * <p>
 * Subclasses are required to implement the {@link #graph(Settings)} method to provide the specific
 * {@link Graph} instance based on the given settings.
 * </p>
 */
public abstract class AbstractTinkerpopConfiguration implements DatabaseConfiguration {

    private static final Logger LOGGER = Logger.getLogger(AbstractTinkerpopConfiguration.class.getName());


    /**
     * Abstract method to be implemented by subclasses to provide the specific {@link Graph} instance
     * based on the given settings.
     *
     * @param settings the settings to configure the graph instance
     * @return the configured {@link Graph} instance
     */
    abstract Graph graph(Settings settings);

    @Override
    public DatabaseManagerFactory apply(Settings settings) {
        LOGGER.fine(() -> "Creating graph database manager");
        Graph graph = graph(settings);
        Objects.requireNonNull(graph, "Graph cannot be null");
        return new GraphDatabaseManagerFactory(graph);
    }

    /**
     * A factory class to create instances of {@link DatabaseManager} for the graph database.
     */
    static class GraphDatabaseManagerFactory implements DatabaseManagerFactory {

        private final Graph graph;

        GraphDatabaseManagerFactory(Graph graph) {
            this.graph = graph;
        }

        @Override
        public void close() {

        }

        @Override
        public DatabaseManager apply(String database) {
            LOGGER.fine(() -> "Creating graph database manager where we will ignore the database name: " + database);
            return TinkerpopGraphDatabaseManager.of(graph);
        }
    }
}