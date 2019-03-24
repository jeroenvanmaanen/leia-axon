package org.leialearns.axon.domain.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.leialearns.axon.domain.event.VocabularyCreatedEvent;
import org.leialearns.axon.domain.persistence.VocabularyDocument;
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
}
