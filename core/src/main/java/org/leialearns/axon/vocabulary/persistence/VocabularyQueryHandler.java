package org.leialearns.axon.vocabulary.persistence;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.leialearns.axon.vocabulary.query.VocabularyByKeyQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class VocabularyQueryHandler {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public VocabularyQueryHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    public String handle(VocabularyByKeyQuery query) {
        Query mongoQuery = Query.query(Criteria.where("key").is(query.getKey()));
        VocabularyDocument vocabularyDocument = mongoTemplate.findOne(mongoQuery, VocabularyDocument.class);
        return Optional.ofNullable(vocabularyDocument).map(VocabularyDocument::getId).orElse(null);
    }
}
