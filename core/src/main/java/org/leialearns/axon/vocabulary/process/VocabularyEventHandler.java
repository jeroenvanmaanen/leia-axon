package org.leialearns.axon.vocabulary.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.leialearns.axon.vocabulary.event.SymbolCreatedEvent;
import org.leialearns.axon.vocabulary.event.SymbolDescriptionLengthFixedEvent;
import org.leialearns.axon.vocabulary.event.VocabularyCreatedEvent;
import org.leialearns.axon.vocabulary.persistence.SymbolDocument;
import org.leialearns.axon.vocabulary.persistence.VocabularyDocument;
import org.leialearns.model.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VocabularyEventHandler {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public VocabularyEventHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventHandler
    public void on(VocabularyCreatedEvent event) {
        String id = event.getId();
        String key = event.getKey();
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("id", id).set("key", key).set("_class", VocabularyDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, VocabularyDocument.class);
    }

    @EventHandler
    public void on(SymbolCreatedEvent event) {
        String vocabulary = event.getVocabulary();
        Symbol symbol = event.getSymbol();
        Integer ordinal = symbol.getOrdinal();
        Query query = Query.query(Criteria.where("vocabulary").is(vocabulary).and("symbol.ordinal").is(ordinal));
        Update update = Update.update("vocabulary", vocabulary).set("symbol", symbol).set("_class", SymbolDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, SymbolDocument.class);
    }

    @EventHandler
    public void on(SymbolDescriptionLengthFixedEvent event) {
        String vocabulary = event.getVocabulary();
        Symbol symbol = event.getSymbol();
        Integer ordinal = symbol.getOrdinal();
        Query query = Query.query(Criteria.where("vocabulary").is(vocabulary).and("symbol.ordinal").is(ordinal));
        Update update = Update.update("vocabulary", vocabulary).set("symbol", symbol).set("_class", SymbolDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, SymbolDocument.class);
    }
}
