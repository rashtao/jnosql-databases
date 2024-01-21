/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *
 * Maximillian Arruda
 */

package org.eclipse.jnosql.databases.dynamodb.communication;


import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.document.Document;
import org.eclipse.jnosql.communication.document.DocumentEntity;
import org.eclipse.jnosql.communication.document.DocumentManager;
import org.eclipse.jnosql.mapping.core.config.MappingConfigurations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.MATCHES;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.NAMED;
import static org.eclipse.jnosql.databases.dynamodb.communication.DocumentEntityGenerator.createRandomEntity;
import static org.eclipse.jnosql.databases.dynamodb.communication.DynamoDBTestUtils.CONFIG;

@EnabledIfSystemProperty(named = NAMED, matches = MATCHES)
class DynamoDBDocumentManagerTest {

    private static Faker faker = new Faker();

    private DynamoDbClient dynamoDbClient;

    private UnaryOperator<String> entityNameResolver;

    @BeforeEach
    void setUp() {
        var settings = CONFIG.getSettings();
        entityNameResolver = entityName -> settings.get(DynamoDBConfigurations.ENTITY_PARTITION_KEY, String.class).orElse(entityName);
        dynamoDbClient = CONFIG.getDynamoDbClient(settings);
        tearDown();
    }

    private DocumentManager getDocumentManagerCannotCreateTables() {
        var settings = CONFIG.customSetting(Settings.builder()
                .put(DynamoDBConfigurations.CREATE_TABLES, "false"));
        var database = settings.get(MappingConfigurations.DOCUMENT_DATABASE, String.class).orElseThrow();
        var documentManagerFactory = CONFIG.getDocumentManagerFactory(settings);
        return documentManagerFactory.apply(database);
    }

    private DocumentManager getDocumentManagerCanCreateTables() {
        var settings = CONFIG.customSetting(Settings.builder()
                .put(DynamoDBConfigurations.CREATE_TABLES, "true"));
        var database = settings.get(MappingConfigurations.DOCUMENT_DATABASE, String.class).orElseThrow();
        var documentManagerFactory = CONFIG.getDocumentManagerFactory(settings);
        return documentManagerFactory.apply(database);
    }


    @AfterEach
    void tearDown() {
        dynamoDbClient.listTables()
                .tableNames()
                .forEach(tableName ->
                        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(tableName).build())
                );
    }

    @Test
    void shouldReturnName() {
        try (var manager = getDocumentManagerCannotCreateTables()) {
            var database = CONFIG
                    .getSettings()
                    .get(MappingConfigurations.DOCUMENT_DATABASE, String.class).orElseThrow();
            Assertions.assertThat(manager.name()).isEqualTo(database);
        }
    }

    @Test
    void shouldReturnErrorWhenInsertNull() {

        try (var documentManager = getDocumentManagerCannotCreateTables()) {
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> documentManager.insert((DocumentEntity) null))
                        .as("should return error when insert a null DocumentEntity reference")
                        .isExactlyInstanceOf(NullPointerException.class);
                softly.assertThatThrownBy(() -> documentManager.insert((DocumentEntity) null, Duration.ofSeconds(1)))
                        .as("should return error when insert a null DocumentEntity reference with TTL param")
                        .isInstanceOfAny(NullPointerException.class);
                softly.assertThatThrownBy(() -> documentManager.insert((DocumentEntity) null, null))
                        .as("should return error when insert a null DocumentEntity reference with nullable TTL param")
                        .isInstanceOfAny(NullPointerException.class);
                softly.assertThatThrownBy(() -> documentManager.insert(DocumentEntityGenerator.createRandomEntity(), null))
                        .as("should return error when insert a null DocumentEntity reference with nullable TTL param")
                        .isInstanceOfAny(NullPointerException.class);
                softly.assertThatThrownBy(() -> documentManager.insert((Iterable<DocumentEntity>) null))
                        .as("should return error when insert a null Iterable<DocumentEntity> reference")
                        .isInstanceOfAny(NullPointerException.class);
                softly.assertThatThrownBy(() -> documentManager.insert((Iterable<DocumentEntity>) null, Duration.ofSeconds(1)))
                        .as("should return error when insert a null Iterable<DocumentEntity> reference with TTL param")
                        .isInstanceOfAny(NullPointerException.class);
                softly.assertThatThrownBy(() -> documentManager.insert(List.of(DocumentEntityGenerator.createRandomEntity()), null))
                        .as("should return error when insert a null Iterable<DocumentEntity> reference with nullable TTL param")
                        .isInstanceOfAny(NullPointerException.class);
            });
        }
    }

    @Test
    void shouldInsert() {

        try (var documentManager = getDocumentManagerCanCreateTables()) {

            assertSoftly(softly -> {
                DocumentEntity entity = createRandomEntity();
                var _entityType = entity.name();
                var id = entity.find(DocumentEntityConverter.ID, String.class).orElseThrow();
                var persistedEntity = documentManager.insert(entity);

                softly.assertThat(persistedEntity)
                        .as("documentManager.insert(DocumentEntity) method should return a non-null persistent DocumentEntity")
                        .isNotNull();

                var persistedItem = getItem(_entityType, id);

                softly.assertThat(persistedItem).as("should return the item from dynamodb").isNotNull();
            });

            assertSoftly(softly -> {
                var entities = List.of(createRandomEntity(), createRandomEntity(), createRandomEntity());
                Iterable<DocumentEntity> persistedEntities = documentManager.insert(entities);
                softly.assertThat(persistedEntities)
                        .as("documentManager.insert(Iterable<>) should returns the non-null list of DocumentEntity").isNotNull();

                assertThat(persistedEntities)
                        .as("documentmanager.insert(iterable<>) should returns a corresponded list of DocumentEntity")
                        .hasSize(3);

                persistedEntities.forEach(entity -> {
                    var _entityType = entity.name();
                    var id = entity.find("_id", String.class).orElseThrow();
                    var persistedItem = getItem(_entityType, id);
                    softly.assertThat(persistedItem)
                            .as("all items of the list of DocumentEntity should be stored on dynamodb database. the entity %s not found"
                                    .formatted(id))
                            .isNotNull();
                });
            });
        }
    }

    @Test
    void shouldUpdate() {
        try (var documentManager = getDocumentManagerCanCreateTables()) {

            var entity1 = createRandomEntity();
            var entity2 = createRandomEntity();
            var entity3 = createRandomEntity();

            documentManager.insert(List.of(entity1, entity2, entity3));

            final BiConsumer<SoftAssertions, DocumentEntity> assertions = (softly, updatedEntity) -> {
                Map<String, AttributeValue> item = getItem(updatedEntity.name(), updatedEntity.find(DocumentEntityConverter.ID, String.class).orElseThrow());
                softly.assertThat(item.get("name"))
                        .as("the name attribute should exists in the returned item from dynamodb")
                        .isNotNull();
                softly.assertThat(item.get("name").s())
                        .as("the name attribute should had be updated successfully")
                        .isEqualTo(updatedEntity.find("name", String.class).orElse(null));
            };

            assertSoftly(softly -> {
                entity1.add(Document.of("name", faker.name().fullName()));
                var updatedEntity = documentManager.update(entity1);
                softly.assertThat(updatedEntity)
                        .as("documentManager.update(DocumentEntity) method should return a non-null persistent DocumentEntity")
                        .isNotNull();
                assertions.accept(softly, updatedEntity);
            });

            assertSoftly(softly -> {
                entity2.add(Document.of("name", faker.name().fullName()));
                entity3.add(Document.of("name", faker.name().fullName()));

                var updatedEntities = documentManager.update(List.of(entity2, entity2));
                softly.assertThat(updatedEntities)
                        .as("documentManager.update(Iterable<>) method should return a non-null list of DocumentEntity")
                        .isNotNull();
                softly.assertThat(updatedEntities)
                        .as("the size of the returned list of DocumentEntity from " +
                                "documentManager.update(Iterable<>) method should be equals to the size of the submitted list of DocumentEntity")
                        .hasSize(2);
                updatedEntities.forEach(updatedEntity -> assertions.accept(softly, updatedEntity));
            });
        }
    }

    private Map<String, AttributeValue> getItem(String _entityType, String id) {
        return dynamoDbClient
                .getItem(GetItemRequest.builder()
                        .tableName(_entityType)
                        .key(Map.of(
                                entityNameResolver.apply(_entityType), AttributeValue.builder().s(_entityType).build(),
                                DocumentEntityConverter.ID, AttributeValue.builder().s(id).build()
                        ))
                        .build())
                .item();
    }


    @Test
    void shouldCountByCollectionName() {

        try (var dmCanCreateTable = getDocumentManagerCanCreateTables();
             var dmCannotCreateTable = getDocumentManagerCannotCreateTables()) {
            assertSoftly(softly -> {

                DocumentEntity entity = createRandomEntity();
                DocumentEntity entity2 = createRandomEntity();

                dmCanCreateTable.insert(entity);
                dmCanCreateTable.insert(entity2);

                softly.assertThatThrownBy(() -> dmCanCreateTable.count((String) null))
                        .as("should return an error when a nullable String is passed as arg")
                        .isInstanceOfAny(NullPointerException.class);

                softly.assertThat(dmCanCreateTable.count(entity.name()))
                        .as("the returned count number of items from an given existent table name is incorrect")
                        .isEqualTo(2L);

                String nonExistentTable = UUID.randomUUID().toString();

                softly.assertThat(dmCannotCreateTable.count(nonExistentTable))
                        .as("the returned count number of items from a given an non-existent table name is incorrect")
                        .isEqualTo(0L);

                softly.assertThatThrownBy(() -> dynamoDbClient
                                .describeTable(DescribeTableRequest
                                        .builder()
                                        .tableName(nonExistentTable
                                        ).build()))
                        .as("it must not create a table")
                        .isInstanceOfAny(ResourceNotFoundException.class);

                var entityBName = "entityB";
                DocumentEntity entity3 = createRandomEntity(entityBName);
                dmCanCreateTable.insert(entity3);

                softly.assertThat(dmCannotCreateTable.count(entity3.name()))
                        .as("the returned count number of items from a given table name is incorrect")
                        .isEqualTo(1L);
            });

        }
    }

    @Test
    void shouldCountByDocumentQuery() {
    }
}
