/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.databases.tinkerpop.mapping;

import jakarta.inject.Inject;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.databases.tinkerpop.cdi.mock.MockGraphProducer;
import org.eclipse.jnosql.databases.tinkerpop.mapping.spi.TinkerpopExtension;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TinkerpopTemplate.class})
@AddPackages(MockGraphProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, TinkerpopExtension.class})
class DefaultGraphTemplateProducerTest {

    @Inject
    private GraphTemplateProducer producer;


    @Test
    void shouldReturnErrorWhenManagerNull() {
        Assertions.assertThrows(NullPointerException.class, () -> producer.apply(null));
    }

    @Test
    void shouldReturn() {
        var graph = Mockito.mock(Graph.class);
        var template = producer.apply(graph);
        assertNotNull(template);
    }
}
