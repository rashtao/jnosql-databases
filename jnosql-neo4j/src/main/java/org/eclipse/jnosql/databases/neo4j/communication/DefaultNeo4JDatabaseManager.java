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

import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

class DefaultNeo4JDatabaseManager implements Neo4JDatabaseManager {

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

        if (!entity.contains(ID)) {
            throw new Neo4JCommunicationException("Cannot update entity without an _id field, entity: " + entity);
        }

        Map<String, Object> entityMap = entity.toMap();
        StringBuilder cypher = new StringBuilder("MATCH (e) WHERE elementId(e) = $elementId SET ");

        entityMap.entrySet().stream()
                .filter(entry -> !ID.equals(entry.getKey()))
                .forEach(entry -> cypher.append("e.").append(entry.getKey()).append(" = $").append(entry.getKey()).append(", "));

        if (cypher.toString().endsWith(", ")) {
            cypher.setLength(cypher.length() - 2);
        }

        LOGGER.finest(() -> "Executing Cypher query to update entity: " + cypher);

        try (Transaction tx = session.beginTransaction()) {
            var elementId = entity.find(ID)
                    .orElseThrow(() -> new CommunicationException("Entity must have an ID"))
                    .get(String.class);

            Map<String, Object> params = new HashMap<>(entityMap);
            params.put("elementId", elementId);

            tx.run(cypher.toString(), Values.parameters(flattenMap(params)));
            tx.commit();
        }

        LOGGER.fine("Updated entity: " + entity.name() + " with elementId: " + entity.find(ID).orElseThrow().get(String.class));
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
        Map<String, Object> parameters = new HashMap<>();
        String cypher = Neo4JQueryBuilder.INSTANCE.buildQuery(query, parameters);

        LOGGER.fine("Executing Delete Cypher Query: " + cypher);
        try (Transaction tx = session.beginTransaction()) {
            tx.run(cypher, Values.parameters(flattenMap(parameters)));
            tx.commit();
        }
    }

    @Override
    public Stream<CommunicationEntity> select(SelectQuery query) {
        Objects.requireNonNull(query, "query is required");
        Map<String, Object> parameters = new HashMap<>();
        String cypher = Neo4JQueryBuilder.INSTANCE.buildQuery(query, parameters);

        LOGGER.fine("Executing Cypher Query for select entities: " + cypher);
        try (Transaction tx = session.beginTransaction()) {
            return tx.run(cypher, Values.parameters(flattenMap(parameters)))
                    .list(record -> extractEntity(query.name(), record, query.columns().isEmpty()))
                    .stream();
        }
    }

    @Override
    public long count(String entity) {
        Objects.requireNonNull(entity, "entity is required");
        try (Transaction tx = session.beginTransaction()) {
            String cypher = "MATCH (e:" + entity + ") RETURN count(e) AS count";
            LOGGER.fine("Executing Cypher Query for counting: " + cypher);
            long count = tx.run(cypher).single().get("count").asLong();
            tx.commit();
            return count;
        } catch (Exception e) {
            LOGGER.severe("Error executing count query: " + e.getMessage());
           throw new CommunicationException("Error executing count query", e);
        }
    }

    @Override
    public Stream<CommunicationEntity> executeQuery(String cypher, Map<String, Object> parameters) {
        Objects.requireNonNull(cypher, "Cypher query is required");
        Objects.requireNonNull(parameters, "Parameters map is required");

        try (Transaction tx = session.beginTransaction()) {
            Stream<CommunicationEntity> result = tx.run(cypher, Values.parameters(flattenMap(parameters)))
                    .list(record -> extractEntity("QueryResult", record, false))
                    .stream();
            LOGGER.fine("Executed Cypher query: " + cypher);
            tx.commit();
            return result;
        } catch (Exception e) {
            throw new CommunicationException("Error executing Cypher query", e);
        }
    }

    @Override
    public Stream<CommunicationEntity> traverse(String startNodeId, String label, int depth) {
        Objects.requireNonNull(startNodeId, "Start node ID is required");
        Objects.requireNonNull(label, "Relationship type is required");

        String cypher = "MATCH (startNode) WHERE elementId(startNode) = $elementId " +
                "MATCH (startNode)-[r:" + label + "*1.." + depth + "]-(endNode) " +
                "RETURN endNode";

        try (Transaction tx = session.beginTransaction()) {
            Stream<CommunicationEntity> result = tx.run(cypher, Values.parameters("elementId", startNodeId))
                    .list(record -> extractEntity("TraversalResult", record, false))
                    .stream();
            LOGGER.fine("Executed traversal query: " + cypher);
            tx.commit();
            return result;
        }
    }

    @Override
    public void edge(CommunicationEntity source, String label, CommunicationEntity target) {
        Objects.requireNonNull(source, "Source entity is required");
        Objects.requireNonNull(target, "Target entity is required");
        Objects.requireNonNull(label, "Relationship type is required");

        String cypher = "MATCH (s) WHERE elementId(s) = $sourceElementId " +
                "MATCH (t) WHERE elementId(t) = $targetElementId " +
                "CREATE (s)-[r:" + label + "]->(t)";

        try (Transaction tx = session.beginTransaction()) {
            var sourceId = source.find(ID).orElseThrow(() ->
                    new EdgeCommunicationException("The source entity should have the " + ID + " property")).get();
            var targetId = target.find(ID).orElseThrow(() ->
                    new EdgeCommunicationException("The target entity should have the " + ID + " property")).get();

            tx.run(cypher, Values.parameters(
                    "sourceElementId", sourceId,
                    "targetElementId", targetId
            ));

            LOGGER.fine("Created edge: " + cypher);
            tx.commit();
        }
    }

    @Override
    public void remove(CommunicationEntity source, String label, CommunicationEntity target) {
        Objects.requireNonNull(source, "Source entity is required");
        Objects.requireNonNull(target, "Target entity is required");
        Objects.requireNonNull(label, "Relationship type is required");

        String cypher = "MATCH (s) WHERE elementId(s) = $sourceElementId " +
                "MATCH (t) WHERE elementId(t) = $targetElementId " +
                "MATCH (s)-[r:" + label + "]-(t) DELETE r";

        var sourceId = source.find(ID).orElseThrow(() ->
                new EdgeCommunicationException("The source entity should have the " + ID + " property")).get();
        var targetId = target.find(ID).orElseThrow(() ->
                new EdgeCommunicationException("The target entity should have the " + ID + " property")).get();

        try (Transaction tx = session.beginTransaction()) {
            tx.run(cypher, Values.parameters(
                    "sourceElementId", sourceId,
                    "targetElementId", targetId
            ));
            LOGGER.fine("Removed edge: " + cypher);
            tx.commit();
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

                Map<String, Object> properties = entity.toMap();
                StringBuilder cypher = new StringBuilder("CREATE (e:");
                cypher.append(entity.name()).append(" {");

                properties.keySet().forEach(key -> cypher.append(key).append(": $").append(key).append(", "));

                if (!properties.isEmpty()) {
                    cypher.setLength(cypher.length() - 2);
                }
                cypher.append("}) RETURN e");
                LOGGER.fine("Executing Cypher Query to insert entities: " + cypher);

                var result = tx.run(cypher.toString(), Values.parameters(flattenMap(properties)));
                var record = result.hasNext() ? result.next() : null;
                var insertedNode = record.get("e").asNode();
                entity.add(ID, insertedNode.elementId());
                entitiesResult.add(entity);
            }
            tx.commit();
        }
        LOGGER.fine("Inserted entities: " + entitiesResult.size());
        return entitiesResult;
    }

    private CommunicationEntity extractEntity(String entityName, org.neo4j.driver.Record record, boolean isFullNode) {
        List<Element> elements = new ArrayList<>();

        for (String key : record.keys()) {
            var value = record.get(key);

            if (value.hasType(org.neo4j.driver.types.TypeSystem.getDefault().NODE())) {
                var node = value.asNode();
                node.asMap().forEach((k, v) -> elements.add(Element.of(k, v))); // Extract properties
                elements.add(Element.of(ID, node.elementId()));
            } else {
                String fieldName = key.contains(".") ? key.substring(key.indexOf('.') + 1) : key;
                elements.add(Element.of(fieldName, value.asObject()));
            }
        }

        return CommunicationEntity.of(entityName, elements);
    }
}
