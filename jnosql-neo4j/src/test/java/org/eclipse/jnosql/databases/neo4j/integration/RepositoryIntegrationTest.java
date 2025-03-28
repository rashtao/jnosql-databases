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
package org.eclipse.jnosql.databases.neo4j.integration;


import jakarta.inject.Inject;
import org.eclipse.jnosql.databases.neo4j.communication.DatabaseContainer;
import org.eclipse.jnosql.databases.neo4j.communication.Neo4JConfigurations;
import org.eclipse.jnosql.databases.neo4j.mapping.Neo4JExtension;
import org.eclipse.jnosql.databases.neo4j.mapping.Neo4JTemplate;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.MATCHES;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.NAMED;

@EnableAutoWeld
@AddPackages(value = {Database.class, EntityConverter.class, Neo4JTemplate.class})
@AddPackages(Magazine.class)
@AddPackages(Reflections.class)
@AddPackages(Converters.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, Neo4JExtension.class})
@EnabledIfSystemProperty(named = NAMED, matches = MATCHES)
public class RepositoryIntegrationTest {

    static {
        DatabaseContainer.INSTANCE.host();
        System.setProperty(Neo4JConfigurations.URI.get(), DatabaseContainer.INSTANCE.host());
        System.setProperty(Neo4JConfigurations.DATABASE.get(), "neo4j");
    }

    @Inject
    private MagazineRepository repository;

    @Test
    void shouldSave() {
        Magazine magazine = new Magazine(null, "Effective Java", 1);
        assertThat(repository.save(magazine))
                .isNotNull();

    }

}
