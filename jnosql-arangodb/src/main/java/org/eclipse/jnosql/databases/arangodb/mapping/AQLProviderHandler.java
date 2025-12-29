package org.eclipse.jnosql.databases.arangodb.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
@ProviderQuery("aql-query")
class AQLProviderHandler  implements ProviderQueryHandler {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        return null;
    }
}
