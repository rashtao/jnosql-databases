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

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import java.util.logging.Logger;

public final class Neo4JConfiguration implements DatabaseConfiguration {

    private static final Logger LOGGER = Logger.getLogger(Neo4JConfiguration.class.getName());

    private static final String DEFAULT_BOLT = "bolt://localhost:7687";

    @Override
    public DatabaseManagerFactory apply(Settings settings) {
        var uri = settings.getOrDefault(Neo4JConfigurations.URI, DEFAULT_BOLT);
        var user = settings.get(Neo4JConfigurations.USERNAME).or(null);
        var password = settings.get(Neo4JConfigurations.PASSWORD).or(null);
        LOGGER.info("Starting configuration to Neo4J database, the uri: " + uri);
        return null;
    }
}
