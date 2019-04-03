package org.leialearns.axon.model.node.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.bson.types.ObjectId;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.leialearns.axon.model.node.query.ModelNodeByIdQuery;
import org.leialearns.axon.model.node.query.ModelNodeByKeyQuery;
import org.leialearns.axon.model.node.query.NextModelNodeQuery;
import org.leialearns.model.ModelNodeData;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ModelNodeQueryHandler {

    private final MongoTemplate mongoTemplate;

    public ModelNodeQueryHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    public ModelNodeData query(ModelNodeByIdQuery query) {
        Query dbQuery = Query.query(Criteria.where("_id").is(new ObjectId(query.getId())));
        return Optional.ofNullable(mongoTemplate.findOne(dbQuery, ModelNodeDocument.class))
            .map(ModelNodeDocument::getData)
            .orElse(null);
    }

    @QueryHandler
    public ModelNodeDocument query(ModelNodeByKeyQuery query) {
        Query dbQuery = Query.query(Criteria.where("data.key").is(query.getKey()));
        return mongoTemplate.findOne(dbQuery, ModelNodeDocument.class);
    }

    @QueryHandler
    public ModelNodeDocument query(NextModelNodeQuery query) {
        return null;
    }
}
