package org.leialearns.rest;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class QueryService {

    private final QueryGateway queryGateway;

    public QueryService(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    public <R> R queryWithRetry(String label, Object query, Class<R> resultType) {
        String queryTypeName = getSimpleTypeName(query);
        String resultTypeName = getSimpleName(resultType);
        int sleep = 10;
        int wait = 0;
        do {
            try {
                R result = queryGateway.query(query, resultType).get();
                if (result != null) {
                    return result;
                }
                Thread.sleep(sleep);
            } catch (Exception e) {
                log.warn("Exception while executing query: {}: {}: {}", label, queryTypeName, resultTypeName, e);
            }
            sleep = sleep * 3 / 2;
            wait += sleep;
        } while (wait < 2000);
        log.debug("Did not get result for query: {}: {}: {}", label, queryTypeName, resultTypeName);
        return null;
    }

    private String getSimpleTypeName(Object object) {
        return Optional.ofNullable(object).map(Object::getClass).map(this::getSimpleName).orElse("?");
    }

    private String getSimpleName(Class<?> type) {
        return Optional.ofNullable(type).map(Class::getSimpleName).orElse("?");
    }
}
