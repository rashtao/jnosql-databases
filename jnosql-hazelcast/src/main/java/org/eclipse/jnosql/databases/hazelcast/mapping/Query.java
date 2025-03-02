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
package org.eclipse.jnosql.databases.hazelcast.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a dynamic query method in Hazelcast repositories.
 * <p>
 * This annotation allows developers to specify Hazelcast query expressions
 * directly in repository methods.
 * </p>
 *
 * Example usage:
 * <pre>
 * {@code
 * @Repository
 * interface PersonRepository extends HazelcastRepository<Person, String> {
 *
 *     @Query("age > 30")
 *     List<Person> findAdults();
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {

    /**
     * The Hazelcast query expression.
     *
     * @return the query string to be executed
     */
    String value();
}
