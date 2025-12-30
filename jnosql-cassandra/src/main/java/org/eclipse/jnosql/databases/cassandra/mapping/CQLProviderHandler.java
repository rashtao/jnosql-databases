package org.eclipse.jnosql.databases.cassandra.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@ApplicationScoped
@ProviderQuery("cql")
class CQLProviderHandler implements ProviderQueryHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var parameters = context.parameters();
        var template = (CassandraTemplate) context.template();
        RepositoryMetadata metadata = context.metadata();
        var sampleQueryProvider = method.annotations().stream()
                .filter(annotation -> CQL.class.equals(annotation.annotation()))
                .findFirst().orElseThrow();

        Map<String, Object> attributes = sampleQueryProvider.attributes();
        var aql = (String) attributes.get("value");
        Map<String, Object> params = RepositoryMetadataUtils.INSTANCE.getParams(method, parameters);
        Stream<T> result;
        if (params.isEmpty()) {
            result = template.cql(aql, emptyMap());
        } else {
            result = template.cql(aql, params);
        }
        return RepositoryMetadataUtils.INSTANCE.execute(context, result);
    }
}
