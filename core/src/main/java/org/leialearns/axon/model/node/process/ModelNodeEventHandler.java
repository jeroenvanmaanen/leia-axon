package org.leialearns.axon.model.node.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryGateway;
import org.bson.types.ObjectId;
import org.leialearns.axon.model.node.aggregate.ModelNodeHelper;
import org.leialearns.axon.model.node.event.ModelNodeCreatedEvent;
import org.leialearns.axon.model.node.event.ModelNodeWasMarkedAsExtensibleEvent;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.leialearns.axon.model.node.persistence.TransitionDocument;
import org.leialearns.axon.model.node.query.ModelNodeDescendantsQuery;
import org.leialearns.model.ModelNodeData;
import org.leialearns.model.SymbolReference;
import org.leialearns.util.StreamUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;

@Component
@Slf4j
public class ModelNodeEventHandler {

    private final ModelNodeHelper helper;
    private final MongoTemplate mongoTemplate;
    private final QueryGateway queryGateway;

    public ModelNodeEventHandler(ModelNodeHelper helper, MongoTemplate mongoTemplate, QueryGateway queryGateway) {
        this.helper = helper;
        this.mongoTemplate = mongoTemplate;
        this.queryGateway = queryGateway;
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

        addTransitions(id, data.getPath());
    }

    private void addTransitions(String id, Collection<SymbolReference> path) {
        if (path.isEmpty()) {
            log.debug("Path is empty");
            return;
        }
        Iterator<SymbolReference> it = path.iterator();
        SymbolReference first = it.next();
        String key = helper.getKey(StreamUtil.asStream(() -> it).toArray(SymbolReference[]::new));
        try {
            String[] sourceIds = queryGateway.query(ModelNodeDescendantsQuery.builder().keyPrefix(key).build(), String[].class).get();
            log.debug("Number of source identifiers: {}", sourceIds.length);
            for (String sourceId : sourceIds) {
                addTransition(sourceId, first, id);
            }
        } catch (Exception e) {
            log.warn("Exception while adding transitions", e);
        }
    }

    private void addTransition(String sourceId, SymbolReference first, String targetId) {
        Criteria criteria = Criteria
            .where("sourceId").is(sourceId)
            .and("symbolReference.vocabulary").is(first.getVocabulary())
            .and("symbolReference.ordinal").is(first.getOrdinal());
        Query query = Query.query(criteria);
        Update update = Update
            .update("sourceId", sourceId)
            .set("symbolReference", first)
            .set("targetId", targetId)
            .set("_class", TransitionDocument.class.getCanonicalName());
        mongoTemplate.upsert(query, update, TransitionDocument.class);
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
}
