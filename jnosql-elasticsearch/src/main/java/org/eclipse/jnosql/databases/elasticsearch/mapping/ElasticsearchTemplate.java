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
package org.eclipse.jnosql.databases.elasticsearch.mapping;


import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;

import java.util.stream.Stream;

/**
 * An Elasticsearch-specific extension of {@link DocumentTemplate},
 * providing a method to perform search queries using {@link SearchRequest}.
 *
 * This template allows executing Elasticsearch queries and retrieving results
 * as a stream of entities mapped by Eclipse JNoSQL.
 *
 * Example usage:
 * <pre>
 * {@code
 * @Inject
 * private ElasticsearchTemplate elasticsearchTemplate;
 *
 * SearchRequest request = new SearchRequest.Builder()
 *         .index("documents")
 *         .query(q -> q.match(m -> m.field("title").query("Eclipse JNoSQL")))
 *         .build();
 *
 * Stream<Document> results = elasticsearchTemplate.search(request);
 * results.forEach(System.out::println);
 * }
 * </pre>
 *
 * @see DocumentTemplate
 */
public interface ElasticsearchTemplate extends DocumentTemplate {

    /**
     * Executes a search query using the provided {@link SearchRequest}.
     * The search query should be built using Elasticsearch's client API and passed
     * to this method. The results will be mapped to the specified entity type
     * and returned as a stream.
     *
     * @param <T>   the entity type
     * @param query the Elasticsearch query request
     * @return a stream of entities resulting from the search query
     * @throws NullPointerException if the query is null
     */
    <T> Stream<T> search(SearchRequest query);
}
