package org.leialearns.axon.vocabulary.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.leialearns.axon.vocabulary.persistence.SymbolDocument;
import org.leialearns.axon.vocabulary.persistence.VocabularyDocument;
import org.leialearns.axon.vocabulary.query.AllVocabularyKeysQuery;
import org.leialearns.axon.vocabulary.query.VocabularyByKeyQuery;
import org.leialearns.axon.vocabulary.query.VocabularyGetSymbolsByKeyQuery;
import org.leialearns.model.Symbol;
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
        Query dbQuery = Query.query(Criteria.where("key").is(query.getKey()));
        VocabularyDocument vocabularyDocument = mongoTemplate.findOne(dbQuery, VocabularyDocument.class);
        return Optional.ofNullable(vocabularyDocument).map(VocabularyDocument::getId).orElse(null);
    }

    @QueryHandler
    public String[] handle(AllVocabularyKeysQuery query) {
        return mongoTemplate.findAll(VocabularyDocument.class).stream().map(VocabularyDocument::getKey).toArray(String[]::new);
    }

    @QueryHandler
    public Symbol[] handle(VocabularyGetSymbolsByKeyQuery query) {
        Query dbQuery = Query.query(Criteria.where("vocabulary").is(query.getKey()));
        return mongoTemplate.find(dbQuery, SymbolDocument.class).stream().map(SymbolDocument::getSymbol).toArray(Symbol[]::new);
    }
}
