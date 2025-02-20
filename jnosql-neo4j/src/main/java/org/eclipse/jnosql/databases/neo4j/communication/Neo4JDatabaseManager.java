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

/**
 * This specialization of {@link DatabaseManager} is designed for Neo4j databases.
 *
 * <p>Neo4j does not natively support Time-To-Live (TTL) operations, but TTL can be managed
 * using the APOC library. Implementations of this interface should handle TTL-related methods
 * accordingly—either by integrating APOC's TTL features or throwing {@link UnsupportedOperationException}
 * if TTL is not supported.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * Neo4JDatabaseManager manager = ...; // Obtain an instance
 * CommunicationEntity entity = CommunicationEntity.of("User");
 * entity.add("name", "Alice");
 * entity.add("age", 30);
 *
 * manager.insert(entity); // Insert into Neo4j
 * </pre>
 *
 * <h3>Implementation Notes:</h3>
 * <p>Ensure proper transaction and session management when implementing this interface.
 * Unsupported TTL operations should result in an {@link UnsupportedOperationException}.</p>
 *
 * @see DatabaseManager
 */
public interface Neo4JDatabaseManager extends DatabaseManager {
}
