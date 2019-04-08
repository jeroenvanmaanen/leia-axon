package org.leialearns.axon.model.node.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.bson.types.ObjectId;
import org.leialearns.axon.model.node.event.ModelNodeCreatedEvent;
import org.leialearns.axon.model.node.event.ModelNodeWasMarkedAsExtensibleEvent;
import org.leialearns.axon.model.node.event.ModelStepEvent;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.leialearns.axon.model.node.persistence.TransitionDocument;
import org.leialearns.model.ModelNodeData;
import org.leialearns.model.SymbolReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModelNodeEventHandler {

    private final MongoTemplate mongoTemplate;

    public ModelNodeEventHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventHandler
    public void on(ModelNodeCreatedEvent event) {
        String id = event.getId();
        ModelNodeData data = event.getData();
        ModelNodeDocument.builder().id(id).build();
        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        Update update = Update
            .update("_id", id)
            .set("data", data)
            .set("_class", ModelNodeDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, ModelNodeDocument.class);
    }

    @EventHandler
    public void on(ModelNodeWasMarkedAsExtensibleEvent event) {
        String id = event.getId();
        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        Update update = Update
            .update("_id", id)
            .set("data.extensible", true)
            .set("_class", ModelNodeDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, ModelNodeDocument.class);
    }

    @EventHandler
    public void on(ModelStepEvent event) {
        String sourceId = event.getPreviousModelNodeId();
        SymbolReference first = event.getSymbol();
        Criteria criteria = Criteria
            .where("sourceId").is(sourceId)
            .and("symbolReference.vocabulary").is(first.getVocabulary())
            .and("symbolReference.ordinal").is(first.getOrdinal());
        Query query = Query.query(criteria);
        Update update = Update
            .update("sourceId", sourceId)
            .set("symbolReference", first)
            .set("targetId", event.getId())
            .set("_class", TransitionDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, TransitionDocument.class);
    }
}
