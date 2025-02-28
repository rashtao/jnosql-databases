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

import net.datafaker.Faker;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.Elements;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.eclipse.jnosql.communication.semistructured.DeleteQuery.delete;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Neo4JDatabaseManagerTest {

    public static final String COLLECTION_NAME = "person";
    private static DatabaseManager entityManager;

    @BeforeAll
    public static void setUp() {
        entityManager = DatabaseContainer.INSTANCE.get("neo4j");
    }

    @Test
    void shouldInsert() {
        var entity = getEntity();
        var communicationEntity = entityManager.insert(entity);
        assertTrue(communicationEntity.elements().stream().map(Element::name).anyMatch(s -> s.equals("_id")));
    }

    @Test
    void shouldInsertEntities() {
        var entities = List.of(getEntity(), getEntity());
        var result = entityManager.insert(entities);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result).allMatch(e -> e.elements().stream().map(Element::name).anyMatch(s -> s.equals("_id")));
        });
    }

    @Test
    void shouldCount() {
        var entity = getEntity();
        entityManager.insert(entity);
        long count = entityManager.count(COLLECTION_NAME);
        assertTrue(count > 0);
    }

    @Test
    void shouldUpdate() {
        var entity = getEntity();
        var communicationEntity = entityManager.insert(entity);
        var id = communicationEntity.find("_id").orElseThrow().get();
        var update = CommunicationEntity.of(COLLECTION_NAME);
        update.add("_id", id);
        update.add("name", "Lucas");

        var result = entityManager.update(update);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.find("name").orElseThrow().get()).isEqualTo("Lucas");
        });
        assertTrue(result.find("name").isPresent());
    }

    @Test
    void shouldUpdateEntities() {
        var entity = getEntity();
        var communicationEntity = entityManager.insert(entity);
        var id = communicationEntity.find("_id").orElseThrow().get();
        var update = CommunicationEntity.of(COLLECTION_NAME);
        update.add("_id", id);
        update.add("name", "Lucas");

        var result = entityManager.update(List.of(update));
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result).allMatch(e -> e.find("name").orElseThrow().get().equals("Lucas"));
        });
    }

    @Test
    void shouldSelectById() {
        var entity = getEntity();
        var communicationEntity = entityManager.insert(entity);
        var id = communicationEntity.find("_id").orElseThrow().get();
        var query = SelectQuery.select().from(COLLECTION_NAME).where("_id").eq(id).build();
        var entities = entityManager.select(query).toList();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(entities).hasSize(1);
            softly.assertThat(entities).allMatch(e -> e.find("_id").isPresent());
        });
    }

    @BeforeEach
    void beforeEach() {
        delete().from(COLLECTION_NAME).delete(entityManager);
    }




    private CommunicationEntity getEntity() {
        Faker faker = new Faker();

        CommunicationEntity entity = CommunicationEntity.of(COLLECTION_NAME);
        Map<String, Object> map = new HashMap<>();
        map.put("name", faker.name().fullName());
        map.put("city", faker.address().city());
        map.put("age", faker.number().randomNumber());
        List<Element> documents = Elements.of(map);
        documents.forEach(entity::add);
        return entity;
    }
}
