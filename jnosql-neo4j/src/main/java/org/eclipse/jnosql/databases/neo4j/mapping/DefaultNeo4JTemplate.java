package org.eclipse.jnosql.databases.neo4j.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.databases.neo4j.communication.Neo4JDatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

@ApplicationScoped
@Typed(Neo4JTemplate.class)
public class DefaultNeo4JTemplate extends AbstractSemiStructuredTemplate implements Neo4JTemplate {

    private static final Logger LOGGER = Logger.getLogger(DefaultNeo4JTemplate.class.getName());

    private Instance<Neo4JDatabaseManager> manager;

    private EntityConverter converter;

    private EntitiesMetadata entities;

    private Converters converters;

    private EventPersistManager persistManager;


    @Inject
    DefaultNeo4JTemplate(Instance<Neo4JDatabaseManager> manager,
                           EntityConverter converter,
                           EntitiesMetadata entities,
                           Converters converters,
                           EventPersistManager persistManager) {
        this.manager = manager;
        this.converter = converter;
        this.entities = entities;
        this.converters = converters;
        this.persistManager = persistManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Stream<T> executeQuery(String cypher, Map<String, Object> parameters) {
        Objects.requireNonNull(cypher, "cypher is required");
        Objects.requireNonNull(parameters, "parameters is required");
        return manager.get().executeQuery(cypher, parameters)
                .map(e -> (T) converter.toEntity(e));
    }

    @Override
    public <T> Stream<T> traverse(String startNodeId, Supplier<String> relationship, int depth) {
        Objects.requireNonNull(startNodeId, "startNodeId is required");
        Objects.requireNonNull(relationship, "relationship is required");
        return traverse(startNodeId, relationship.get(), depth);
    }


    @Override
    public <T> Stream<T> traverse(String startNodeId, String relationship, int depth) {
        Objects.requireNonNull(startNodeId, "startNodeId is required");
        Objects.requireNonNull(relationship, "relationship is required");
        return manager.get().traverse(startNodeId, relationship, depth)
                .map(e -> (T) converter.toEntity(e));
    }

    @Override
    public <T, E> void edge(T source, Supplier<String> relationship, E target) {
        Objects.requireNonNull(source, "source is required");
        Objects.requireNonNull(relationship, "relationship is required");
        Objects.requireNonNull(target, "target is required");
        edge(source, relationship.get(), target);
    }


    @Override
    public <T, E> void edge(T source, String relationshipType, E target) {
        Objects.requireNonNull(relationshipType, "relationshipType is required");
        Objects.requireNonNull(source, "source is required");
        Objects.requireNonNull(target, "target is required");

         this.find(source.getClass(), source);
        this.find(target.getClass(), target);


    }



    @Override
    public <T, E> void remove(T source, String relationshipType, E target) {

    }

    @Override
    public <T, E> void remove(T source, Supplier<String> relationship, E target) {

    }

    @Override
    protected EntityConverter converter() {
        return converter;
    }

    @Override
    protected DatabaseManager manager() {
        return manager.get();
    }

    @Override
    protected EventPersistManager eventManager() {
        return persistManager;
    }

    @Override
    protected EntitiesMetadata entities() {
        return entities;
    }

    @Override
    protected Converters converters() {
        return converters;
    }
}
