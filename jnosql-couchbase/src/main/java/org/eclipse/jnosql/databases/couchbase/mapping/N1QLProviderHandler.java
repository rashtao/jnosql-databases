package org.eclipse.jnosql.databases.couchbase.mapping;

import com.couchbase.client.java.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.ProviderQuery;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;
import java.util.stream.Stream;

@ApplicationScoped
@ProviderQuery("n1ql")
class N1QLProviderHandler implements ProviderQueryHandler {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var parameters = context.parameters();
        var template = (CouchbaseTemplate) context.template();
        var sampleQueryProvider = method.annotations().stream()
                .filter(annotation -> N1QL.class.equals(annotation.annotation()))
                .findFirst().orElseThrow();

        Map<String, Object> attributes = sampleQueryProvider.attributes();
        var cql = (String) attributes.get("value");
        Map<String, Object> params = RepositoryMetadataUtils.INSTANCE.getParamsFromName(method, parameters);
        Stream<T> result;
        if (params.isEmpty()) {
            result = template.n1qlQuery(cql);
        } else {
            var jsonObject = JsonObject.create();
            params.forEach(jsonObject::put);
            result = template.n1qlQuery(cql, jsonObject);
        }
        return RepositoryMetadataUtils.INSTANCE.execute(context, result);
    }
}
