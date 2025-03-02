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
package org.eclipse.jnosql.databases.arangodb.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a dynamic AQL (ArangoDB Query Language) query for methods
 * in the {@link ArangoDBRepository} interface.
 *
 * <p>This annotation enables executing custom AQL queries directly from repository methods,
 * similar to how queries are defined in other JNoSQL repositories.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @AQL("FOR p IN Person RETURN p")
 * List<Person> findAll();
 * }</pre>
 *
 * <p>Parameterized query:</p>
 * <pre>{@code
 * @AQL("FOR p IN Person FILTER p.name == @name RETURN p")
 * List<Person> findByName(@Param("name") String name);
 * }</pre>
 *
 * @see ArangoDBRepository
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AQL {

    /**
     * The AQL query string to be executed.
     *
     * @return the AQL query
     */
    String value();
}
