package org.eclipse.jnosql.databases.arangodb.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;

@ApplicationScoped
@ProviderQuery("aql-query")
class AQLProviderHandler  implements ProviderQueryHandler {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var parameters = context.parameters();

        var sampleQueryProvider = method.annotations().stream()
                .filter(annotation -> AQL.class.equals(annotation.annotation()))
                .findFirst().orElseThrow();

        Map<String, Object> attributes = sampleQueryProvider.attributes();
        String value = (String) attributes.get("value");
        return null;
    }
}
