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
package org.eclipse.jnosql.databases.cassandra.mapping;

import jakarta.data.repository.Param;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;
import org.eclipse.jnosql.mapping.column.spi.ColumnExtension;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, ColumnTemplate.class, EntityConverter.class,
        CQL.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class,
        ColumnExtension.class, CassandraExtension.class})
public class CassandraRepositoryProxyTest {

    private CassandraTemplate template;

    @Inject
    private Converters converters;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private HumanRepository personRepository;

    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(CassandraTemplate.class);
        CassandraRepositoryProxy handler = new CassandraRepositoryProxy(template,
                HumanRepository.class, converters, entitiesMetadata);

        when(template.insert(any(ContactCassandra.class))).thenReturn(new ContactCassandra());
        when(template.insert(any(ContactCassandra.class), any(Duration.class))).thenReturn(new ContactCassandra());
        when(template.update(any(ContactCassandra.class))).thenReturn(new ContactCassandra());
        this.personRepository = (HumanRepository) Proxy.newProxyInstance(HumanRepository.class.getClassLoader(),
                new Class[]{HumanRepository.class},
                handler);
    }


    @Test
    public void shouldFindByName() {
        personRepository.findByName("Ada");
        verify(template).cql("select * from Person where name = ?", "Ada");
    }

    @Test
    public void shouldDeleteByName() {
        personRepository.deleteByName("Ada");
        verify(template).delete(Mockito.any(DeleteQuery.class));
    }

    @Test
    public void shouldFindAll() {
        personRepository.findAllQuery();
        verify(template).cql("select * from Person");
    }

    @Test
    public void shouldFindByNameCQL() {
        personRepository.findByName("Ada");
        verify(template).cql(Mockito.eq("select * from Person where name = ?"), Mockito.any(Object.class));
    }

    @Test
    public void shouldFindByName2CQL() {
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);

        personRepository.findByName2("Ada");
        verify(template).cql(Mockito.eq("select * from Person where name = :name"), captor.capture());
        Map map = captor.getValue();
        assertEquals("Ada", map.get("name"));
    }

    @Test
    public void shouldSaveUsingInsert() {
        ContactCassandra contact = new ContactCassandra("Ada", 10);
        personRepository.save(contact);
        verify(template).insert(eq(contact));
    }

    @Test
    public void shouldSaveUsingUpdate() {
        ContactCassandra contact = new ContactCassandra("Ada-2", 10);
        when(template.find(ContactCassandra.class, "Ada-2")).thenReturn(Optional.of(contact));
        personRepository.save(contact);
        verify(template).update(eq(contact));
    }

    @Test
    public void shouldDelete(){
        personRepository.deleteById("id");
        verify(template).delete(ContactCassandra.class, "id");
    }


    @Test
    public void shouldDeleteEntity(){
        ContactCassandra contact = new ContactCassandra("Ada", 10);
        personRepository.delete(contact);
        verify(template).delete(ContactCassandra.class, contact.getName());
    }

    interface HumanRepository extends CassandraRepository<ContactCassandra, String> {

        void deleteByName(String namel);

        @CQL("select * from Person")
        List<ContactCassandra> findAllQuery();

        @CQL("select * from Person where name = ?")
        List<ContactCassandra> findByName(String name);

        @CQL("select * from Person where name = :name")
        List<ContactCassandra> findByName2(@Param("name") String name);
    }

}