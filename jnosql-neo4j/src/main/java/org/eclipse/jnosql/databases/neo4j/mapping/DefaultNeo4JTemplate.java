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
import java.util.function.Supplier;
import java.util.stream.Stream;

@ApplicationScoped
@Typed(Neo4JTemplate.class)
public class DefaultNeo4JTe extends AbstractSemiStructuredTemplate implements Neo4JTemplate {

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

    @Override
    public <T> Stream<T> executeQuery(String cypher, Map<String, Object> parameters) {
        return Stream.empty();
    }

    @Override
    public <T> Stream<T> traverse(String startNodeId, String relationship, int depth) {
        return Stream.empty();
    }

    @Override
    public <T> Stream<T> traverse(String startNodeId, Supplier<String> relationship, int depth) {
        return Stream.empty();
    }

    @Override
    public <T, E> void edge(T source, String relationshipType, E target) {

    }

    @Override
    public <T, E> void edge(T source, Supplier<String> relationship, E target) {

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
