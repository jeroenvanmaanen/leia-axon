package org.leialearns.axon.model.node.process;

import org.axonframework.eventhandling.EventHandler;
import org.bson.types.ObjectId;
import org.leialearns.axon.model.node.event.ModelNodeCreatedEvent;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class ModelNodeEventHandler {

    private final MongoTemplate mongoTemplate;

    public ModelNodeEventHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventHandler
    public void on(ModelNodeCreatedEvent event) {
        String id = event.getId();
        ModelNodeDocument.builder().id(id).build();
        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        Update update = Update
            .update("_id", id)
            .set("data", event.getData())
            .set("_class", ModelNodeDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, ModelNodeDocument.class);
    }
}
