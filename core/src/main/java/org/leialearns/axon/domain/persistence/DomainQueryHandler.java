package org.leialearns.axon.domain.persistence;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.leialearns.axon.domain.query.DomainByKeyQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class DomainQueryHandler {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DomainQueryHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    public String handle(DomainByKeyQuery query) {
        Query mongoQuery = Query.query(Criteria.where("key").is(query.getKey()));
        DomainDocument domainDocument = mongoTemplate.findOne(mongoQuery, DomainDocument.class);
        return Optional.ofNullable(domainDocument).map(DomainDocument::getId).orElse(null);
    }
}
