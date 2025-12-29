package org.eclipse.jnosql.databases.arangodb.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.eclipse.jnosql.mapping.core.repository.DynamicReturn.toSingleResult;

@ApplicationScoped
@ProviderQuery("aql-query")
class AQLProviderHandler  implements ProviderQueryHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var parameters = context.parameters();
        var template = (ArangoDBTemplate) context.template();
        RepositoryMetadata metadata = context.metadata();
        var sampleQueryProvider = method.annotations().stream()
                .filter(annotation -> AQL.class.equals(annotation.annotation()))
                .findFirst().orElseThrow();

        Map<String, Object> attributes = sampleQueryProvider.attributes();
        var aql = (String) attributes.get("value");
        Map<String, Object> params = RepositoryMetadataUtils.INSTANCE.getParams(method, parameters);
        Stream<T> result;
        if (params.isEmpty()) {
            result = template.aql(aql, emptyMap());
        } else {
            result = template.aql(aql, params);
        }
        return (T) DynamicReturn.builder()
                .methodName(method.name())
                .classSource(metadata.type())
                .returnType(method.returnType().orElseThrow())
                .result(() -> (Stream<Object>) result)
                .singleResult(toSingleResult(method.name()).apply(() -> result))
                .build()
                .execute();
    }
}
