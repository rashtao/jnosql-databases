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

import jakarta.data.Sort;
import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.ValueUtil;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNeo4JDatabaseManager implements Neo4JDatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DefaultNeo4JDatabaseManager.class.getName());
    public static final String ID = "_id";

    private final Session session;
    private final String database;

    public DefaultNeo4JDatabaseManager(Session session, String database) {
        this.session = session;
        this.database = database;
    }

    @Override
    public String name() {
        return database;
    }

    public CommunicationEntity insert(CommunicationEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        return insertEntities(Collections.singletonList(entity)).iterator().next();
    }

    @Override
    public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities) {
        Objects.requireNonNull(entities, "entities is required");
        return insertEntities(entities);
    }

    @Override
    public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities, Duration ttl) {
       throw new UnsupportedOperationException("This operation is not supported in Neo4J");
    }

    @Override
    public CommunicationEntity insert(CommunicationEntity entity, Duration ttl) {
        throw new UnsupportedOperationException("This operation is not supported in Neo4J");
    }

    @Override
    public CommunicationEntity update(CommunicationEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        Objects.requireNonNull(entity, "entity is required");

        if (!entity.contains(ID)) {
            throw new CommunicationException("Cannot update entity without an _id field, entity: " + entity);
        }

        Map<String, Object> entityMap = entity.toMap();
        StringBuilder cypher = new StringBuilder("MATCH (e:");
        cypher.append(entity.name()).append(" { ").append(ID).append(": $").append(ID).append(" }) SET ");

        entityMap.entrySet().stream()
                .filter(entry -> !ID.equals(entry.getKey()))
                .forEach(entry -> cypher.append("e.").append(entry.getKey()).append(" = $").append(entry.getKey()).append(", "));

        if (cypher.toString().endsWith(", ")) {
            cypher.setLength(cypher.length() - 2);
        }

        LOGGER.fine("Cypher: " + cypher);

        try (Transaction tx = session.beginTransaction()) {
            tx.run(cypher.toString(), Values.parameters(flattenMap(entityMap)));
            tx.commit();
        }

        LOGGER.fine("Updated entity: " + entity.name() + " with _id: " + entity.find(ID).orElseThrow().get(String.class));
        return entity;
    }

    @Override
    public Iterable<CommunicationEntity> update(Iterable<CommunicationEntity> entities) {
        Objects.requireNonNull(entities, "entities is required");
        entities.forEach(this::update);
        return entities;
    }

    @Override
    public void delete(DeleteQuery query) {
        Objects.requireNonNull(query, "query is required");
        StringBuilder cypher = new StringBuilder("MATCH (e:");
        cypher.append(query.name()).append(")");

        Map<String, Object> parameters = new HashMap<>();
        query.condition().ifPresent(c -> {
            cypher.append(" WHERE ");
            createWhereClause(cypher, c, parameters);
        });

        cypher.append(" DELETE e");

        LOGGER.fine("Executing Delete Cypher Query: " + cypher);
        try (Transaction tx = session.beginTransaction()) {
            tx.run(cypher.toString(), Values.parameters(flattenMap(parameters)));
            tx.commit();
        }
    }

    @Override
    public Stream<CommunicationEntity> select(SelectQuery query) {
        Objects.requireNonNull(query, "query is required");
        StringBuilder cypher = new StringBuilder("MATCH (e:");
        cypher.append(query.name()).append(")");

        Map<String, Object> parameters = new HashMap<>();
        query.condition().ifPresent(c -> {
            cypher.append(" WHERE ");
            createWhereClause(cypher, c, parameters);
        });

        cypher.append(" RETURN ");
        List<String> columns = query.columns();
        if (columns.isEmpty()) {
            cypher.append("e ");
        } else {
            cypher.append(columns.stream().map(col -> "e." + col).collect(Collectors.joining(", ")));
        }
        if (query.skip() > 0) {
            cypher.append(" SKIP ").append(query.skip());
        }
        if (query.limit() > 0) {
            cypher.append(" LIMIT ").append(query.limit());
        }
        List<Sort<?>> sorts = query.sorts();
        if (!sorts.isEmpty()) {
            cypher.append(" ORDER BY ");
            cypher.append(sorts.stream()
                    .map(order -> "e." + order.property() + " " + (order.isAscending() ? "ASC" : "DESC"))
                    .collect(Collectors.joining(", ")));
        }

        LOGGER.fine("Executing Cypher Query: " + cypher);
        try (Transaction tx = session.beginTransaction()) {
            return tx.run(cypher.toString(), Values.parameters(flattenMap(parameters)))
                    .list(record -> extractEntity(query.name(), record, columns.isEmpty()))
                    .stream();
        }
    }

    private void createWhereClause(StringBuilder cypher, CriteriaCondition condition, Map<String, Object> parameters) {
        Element element = condition.element();
        String fieldName = element.name();

        switch (condition.condition()) {
            case EQUALS:
            case GREATER_THAN:
            case GREATER_EQUALS_THAN:
            case LESSER_THAN:
            case LESSER_EQUALS_THAN:
            case LIKE:
            case IN:
                parameters.put(fieldName, ValueUtil.convert(element.value()));
                cypher.append("e.").append(fieldName).append(" ")
                        .append(getConditionOperator(condition.condition()))
                        .append(" $").append(fieldName);
                break;
            case BETWEEN:
                List<?> values = element.get(List.class);
                parameters.put(fieldName + "_start", ValueUtil.convert(Value.of(values.get(0))));
                parameters.put(fieldName + "_end", ValueUtil.convert(Value.of(values.get(1))));
                cypher.append("e.").append(fieldName).append(" >= $").append(fieldName).append("_start AND e.")
                        .append(fieldName).append(" <= $").append(fieldName).append("_end");
                break;
            case NOT:
                cypher.append("NOT (");
                createWhereClause(cypher, element.get(CriteriaCondition.class), parameters);
                cypher.append(")");
                break;
            case AND:
            case OR:
                cypher.append("(");
                List<CriteriaCondition> conditions = element.get(List.class);
                for (int i = 0; i < conditions.size(); i++) {
                    if (i > 0) {
                        cypher.append(" ").append(getConditionOperator(condition.condition())).append(" ");
                    }
                    createWhereClause(cypher, conditions.get(i), parameters);
                }
                cypher.append(")");
                break;
            default:
                throw new CommunicationException("Unsupported condition: " + condition.condition());
        }
    }

    private CommunicationEntity extractEntity(String entityName, org.neo4j.driver.Record record, boolean isFullNode) {
        List<Element> elements = new ArrayList<>();

        for (String key : record.keys()) {
            String fieldName = key.contains(".") ? key.substring(key.indexOf('.') + 1) : key;
            if (isFullNode && record.get(key).hasType(org.neo4j.driver.types.TypeSystem.getDefault().NODE())) {
                record.get(key).asNode().asMap().forEach((k, v) -> elements.add(Element.of(k, v)));
            } else {
                elements.add(Element.of(fieldName, record.get(key).asObject()));
            }
        }
        return CommunicationEntity.of(entityName, elements);
    }
    private String getConditionOperator(Condition condition) {
        return switch (condition) {
            case EQUALS -> "=";
            case GREATER_THAN -> ">";
            case GREATER_EQUALS_THAN -> ">=";
            case LESSER_THAN -> "<";
            case LESSER_EQUALS_THAN -> "<=";
            case LIKE -> "CONTAINS";
            case IN -> "IN";
            case AND -> "AND";
            case OR -> "OR";
            default -> throw new CommunicationException("Unsupported operator: " + condition);
        };
    }


    @Override
    public long count(String entity) {
        Objects.requireNonNull(entity, "entity is required");
        try (Transaction tx = session.beginTransaction()) {
            String cypher = "MATCH (e:" + entity + ") RETURN count(e) AS count";
            long count = tx.run(cypher).single().get("count").asLong();
            tx.commit();
            return count;
        } catch (Exception e) {
            LOGGER.severe("Error executing count query: " + e.getMessage());
           throw new CommunicationException("Error executing count query", e);
        }
    }
    @Override
    public void close() {
        LOGGER.fine("Closing the Neo4J session, the database name is: " + database);
        this.session.close();
    }

    private Object[] flattenMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .flatMap(entry -> java.util.stream.Stream.of(entry.getKey(), entry.getValue()))
                .toArray();
    }

    private Iterable<CommunicationEntity> insertEntities(Iterable<CommunicationEntity> entities) {
        List<CommunicationEntity> entitiesResult = new ArrayList<>();
        try (Transaction tx = session.beginTransaction()) {
            for (CommunicationEntity entity : entities) {
                if (!entity.contains(ID)) {
                    String generatedId = UUID.randomUUID().toString();
                    LOGGER.fine("The entity does not contain an _id field. Generating one: " + generatedId);
                    entity.add(ID, generatedId);
                }

                Map<String, Object> properties = entity.toMap();
                StringBuilder cypher = new StringBuilder("CREATE (e:");
                cypher.append(entity.name()).append(" {");

                properties.keySet().forEach(key -> cypher.append(key).append(": $").append(key).append(", "));

                if (!properties.isEmpty()) {
                    cypher.setLength(cypher.length() - 2);
                }
                cypher.append("})");
                LOGGER.fine("Cypher: " + cypher);

                tx.run(cypher.toString(), Values.parameters(flattenMap(properties)));
                entitiesResult.add(entity);
            }
            tx.commit();
        }
        LOGGER.fine("Inserted entities: " + entitiesResult.size());
        return entitiesResult;
    }
}
