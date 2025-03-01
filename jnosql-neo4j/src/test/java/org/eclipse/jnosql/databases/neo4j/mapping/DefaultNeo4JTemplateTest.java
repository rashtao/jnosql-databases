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

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.tck.entities.Person;
import net.datafaker.Faker;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.databases.neo4j.communication.Neo4JDatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class,  Neo4JTemplate.class})
@AddPackages(Music.class)
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class})
class DefaultNeo4JTemplateTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EventPersistManager persistManager;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DefaultNeo4JTemplate template;

    private Neo4JDatabaseManager manager;

    @BeforeEach
    void setUp() {
        this.manager = mock(Neo4JDatabaseManager.class);
        Instance<Neo4JDatabaseManager> instance = mock(Instance.class);
        when(instance.get()).thenReturn(manager);
        template = new DefaultNeo4JTemplate(instance, converter, entities, converters, persistManager);
    }

    @Test
    void shouldExecuteQuery() {
        String cypher = "MATCH (n:Person) RETURN n";
        Map<String, Object> parameters = Collections.emptyMap();
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add(Element.of("name", "Ada"));
        when(manager.executeQuery(cypher, parameters)).thenReturn(Stream.of(entity));

        Stream<Person> result = template.executeQuery(cypher, parameters);
        assertNotNull(result);
        assertTrue(result.findFirst().isPresent());
    }

    @Test
    void shouldThrowExceptionWhenQueryIsNull() {
        assertThrows(NullPointerException.class, () -> template.executeQuery(null, Collections.emptyMap()));
        assertThrows(NullPointerException.class, () -> template.executeQuery("MATCH (n) RETURN n", null));
    }

    @Test
    void shouldCreateEdge() {
        var faker = new Faker();
        Person source = Person.of(faker);
        Person target =  Person.of(faker);
        String relationshipType = "FRIENDS";
        Edge<Person, Person> edge = Edge.of(source, relationshipType, target);


        Mockito.when(manager.insert(Mockito.any(CommunicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Edge<Person, Person> result = template.edge(source, relationshipType, target);

        assertNotNull(result);
        assertEquals(edge, result);
        verify(manager).edge(any(), eq(relationshipType), any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingEdgeWithNullValues() {
        var faker = new Faker();
        Person source = Person.of(faker);
        Person target =  Person.of(faker);
        assertThrows(NullPointerException.class, () -> template.edge(null, "FRIENDS", target));
        assertThrows(NullPointerException.class, () -> template.edge(source, (Supplier<String>) null, target));
        assertThrows(NullPointerException.class, () -> template.edge(source, "FRIENDS", null));
    }

    @Test
    void shouldRemoveEdge() {
        var faker = new Faker();
        Person source = Person.of(faker);
        Person target =  Person.of(faker);
        String relationshipType = "FRIENDS";

        doNothing().when(manager).remove(any(), anyString(), any());
        Mockito.when(manager.insert(Mockito.any(CommunicationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        template.remove(source, relationshipType, target);
        verify(manager).remove(any(), eq(relationshipType), any());
    }

    @Test
    void shouldThrowExceptionWhenRemovingEdgeWithNullValues() {
        var faker = new Faker();
        Person source = Person.of(faker);
        Person target =  Person.of(faker);
        assertThrows(NullPointerException.class, () -> template.remove(null, "FRIENDS", target));
        assertThrows(NullPointerException.class, () -> template.remove(source, (String) null, target));
        assertThrows(NullPointerException.class, () -> template.remove(source, "FRIENDS", null));
    }
}