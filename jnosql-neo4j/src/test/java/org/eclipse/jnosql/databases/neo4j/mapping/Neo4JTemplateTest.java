package org.eclipse.jnosql.databases.neo4j.mapping;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class,  Neo4JTemplate.class})
@AddPackages(Music.class)
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class})
class Neo4JTemplateTest {

    @Inject
    private Neo4JTemplate template;

    @Test
    void shouldInjectMongoDBTemplate() {
        Assertions.assertNotNull(template);
    }
}