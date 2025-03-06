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
import org.eclipse.jnosql.databases.neo4j.mapping.Neo4JTemplate;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.graph.Edge;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.MATCHES;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.NAMED;

@EnableAutoWeld
@AddPackages(value = {Database.class, EntityConverter.class, Neo4JTemplate.class})
@AddPackages(Magazine.class)
@AddPackages(Reflections.class)
@AddPackages(Converters.class)
@AddExtensions({EntityMetadataExtension.class})
@EnabledIfSystemProperty(named = NAMED, matches = MATCHES)
public class TemplateIntegrationTest {

    static {
        DatabaseContainer.INSTANCE.host();
        System.setProperty(Neo4JConfigurations.URI.get(), DatabaseContainer.INSTANCE.host());
        System.setProperty(Neo4JConfigurations.DATABASE.get(), "neo4j");
    }

    @Inject
    private Neo4JTemplate template;

    @BeforeEach
    void setUp() {
        template.delete(Magazine.class).execute();
    }

    @Test
    void shouldFindById() {
        Magazine magazine = template.insert(new Magazine(null, "Effective Java", 1));

        assertThat(template.find(Magazine.class, magazine.id()))
                .isNotNull().get().isEqualTo(magazine);
    }

    @Test
    void shouldInsert() {
        Magazine magazine = template.insert(new Magazine(null, "Effective Java", 1));

        Optional<Magazine> optional = template.find(Magazine.class, magazine.id());
        assertThat(optional).isNotNull().isNotEmpty()
                .get().isEqualTo(magazine);
    }

    @Test
    void shouldUpdate() {
        Magazine magazine = template.insert(new Magazine(null, "Effective Java", 1));

        Magazine updated = new Magazine(magazine.id(), magazine.title() + " updated", 2);

        assertThat(template.update(updated))
                .isNotNull()
                .isNotEqualTo(magazine);

        assertThat(template.find(Magazine.class, magazine.id()))
                .isNotNull().get().isEqualTo(updated);

    }

    @Test
    void shouldDeleteById() {
        Magazine magazine = template.insert(new Magazine(null, "Effective Java", 1));

        template.delete(Magazine.class, magazine.id());
        assertThat(template.find(Magazine.class, magazine.id()))
                .isNotNull().isEmpty();
    }

    @Test
    void shouldDeleteAll(){
        for (int index = 0; index < 20; index++) {
            Magazine magazine = template.insert(new Magazine(null, "Effective Java", 1));
            assertThat(magazine).isNotNull();
        }

        template.delete(Magazine.class).execute();
        assertThat(template.select(Magazine.class).result()).isEmpty();
    }

    @Test
    void shouldCreateEdge() {
        Magazine firstEdition = template.insert(new Magazine(null, "Effective Java", 1));
        Magazine secondEdition = template.insert(new Magazine(null, "Effective Java", 2));
        Edge<Magazine, Magazine> edge = Edge.source(firstEdition).label("NEXT").target(secondEdition).property("year", 2025).build();
        template.edge(edge);
    }
}
