package org.leialearns.axon.model.node.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.bson.types.ObjectId;
import org.leialearns.axon.lag.LagService;
import org.leialearns.axon.model.node.event.ModelNodeChildAddedEvent;
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

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class ModelNodeEventHandler {
    public final static String PROCESSOR_NAME = MethodHandles.lookup().lookupClass().getPackage().getName();

    private final LagService lagService;
    private final MongoTemplate mongoTemplate;

    public ModelNodeEventHandler(LagService lagService, MongoTemplate mongoTemplate) {
        this.lagService = lagService;
        this.mongoTemplate = mongoTemplate;
    }

    @EventHandler
    public void on(ModelNodeCreatedEvent event, @Timestamp Instant timestamp) {
        log.debug("On model node created: {}: {}", event.getData().getId(), event.getData().getKey());
        String id = event.getId();
        ModelNodeData data = event.getData();
        ModelNodeDocument.builder().id(id).build();
        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        Update update = Update
            .update("_id", id)
            .set("data", data)
            .set("_class", ModelNodeDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, ModelNodeDocument.class);
        lagService.recordLag(event, timestamp);
    }

    @EventHandler
    public void on(ModelNodeWasMarkedAsExtensibleEvent event, @Timestamp Instant timestamp) {
        log.debug("On model node was marked as extensible: {}", event.getId());
        String id = event.getId();
        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        Update update = Update
            .update("_id", id)
            .set("data.extensible", true)
            .set("_class", ModelNodeDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, ModelNodeDocument.class);
        lagService.recordLag(event, timestamp);
    }

    @EventHandler
    public void on(ModelStepEvent event, @Timestamp Instant timestamp) {
        log.debug("On model step: {} -({})-> {}", event.getPreviousModelNodeId(), show(event.getSymbol()), event.getId());
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
        lagService.recordLag(event, timestamp);
    }

    @EventHandler
    public void on(ModelNodeChildAddedEvent event, @Timestamp Instant timestamp) {
        log.debug("On model node child added: {} <-({})- {}", event.getId(), show(event.getSymbol()), event.getChildId());
        lagService.recordLag(event, timestamp);
    }

    private String show(SymbolReference symbol) {
        return Optional.ofNullable(symbol).map(s -> s.getVocabulary() + ":" + s.getOrdinal()).orElse("?:?");
    }
}
