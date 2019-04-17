package org.leialearns.axon.model.node.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.bson.types.ObjectId;
import org.leialearns.axon.model.node.aggregate.ModelNodeHelper;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.leialearns.axon.model.node.persistence.TransitionDocument;
import org.leialearns.axon.model.node.query.*;
import org.leialearns.model.ModelNodeData;
import org.leialearns.model.SymbolReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ModelNodeQueryHandler {

    private final MongoTemplate mongoTemplate;
    private final ModelNodeHelper helper;

    public ModelNodeQueryHandler(MongoTemplate mongoTemplate, ModelNodeHelper helper) {
        this.mongoTemplate = mongoTemplate;
        this.helper = helper;
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
    public String query(NextModelNodeQuery query) {
        SymbolReference nextSymbol = query.getNextSymbol();
        Criteria criteria = Criteria.where("sourceId").is(query.getCurrentNodeId())
            .and("symbolReference.vocabulary").is(nextSymbol.getVocabulary())
            .and("symbolReference.ordinal").is(nextSymbol.getOrdinal());
        Query dbQuery = Query.query(criteria);
        return Optional.ofNullable(mongoTemplate.findOne(dbQuery, TransitionDocument.class))
            .map(TransitionDocument::getTargetId)
            .orElse(null);
    }

    @QueryHandler
    public String[] query(ModelNodeDescendantsQuery query) {
        try {
            String pattern = getPrefixPattern(query.getPathPrefix());
            log.debug("Prefix pattern: /{}/", pattern);
            Query dbQuery = Query.query(Criteria.where("data.key").regex(pattern));
            return mongoTemplate.find(dbQuery, ModelNodeDocument.class)
                .stream()
                .peek(node -> log.debug("Descendant: {}", node.getData().getKey()))
                .map(ModelNodeDocument::getId)
                .toArray(String[]::new);
        } catch (RuntimeException e) {
            log.warn("Exception while executing ModelNodeDescendantsQuery", e);
            throw e;
        }
    }

    @QueryHandler
    public ModelNodeDocument[] query(ModelNodeChildrenQuery query) {
        try {
            return findChildren(query.getPathPrefix());
        } catch (RuntimeException e) {
            log.warn("Exception while executing ModelNodeChildrenQuery", e);
            throw e;
        }
    }

    @QueryHandler
    public ModelNodeDocument[] query(ModelNodeChildrenByIdQuery query) {
        try {
            ModelNodeDocument node = mongoTemplate.findById(query.getNodeId(), ModelNodeDocument.class);
            if (node == null) {
                throw new IllegalArgumentException(String.format("Node not found: %s", query.getNodeId()));
            }
            return findChildren(node.getData().getPath().toArray(new SymbolReference[0]));
        } catch (RuntimeException e) {
            log.warn("Exception while executing ModelNodeChildrenByIdQuery", e);
            throw e;
        }
    }

    private ModelNodeDocument[] findChildren(SymbolReference[] pathPrefix) {
        String pattern = getPrefixPattern(pathPrefix);
        log.debug("Prefix pattern: /{}/", pattern);
        Query dbQuery = Query.query(Criteria.where("data.key").regex(pattern).and("data.depth").is(pathPrefix.length + 1));
        return mongoTemplate.find(dbQuery, ModelNodeDocument.class)
            .stream()
            .peek(node -> log.debug("Child: {}", node.getData().getKey()))
            .toArray(ModelNodeDocument[]::new);
    }

    private String getPrefixPattern(SymbolReference[] pathPrefix) {
        String keyPrefix = helper.getKey(pathPrefix);
        return "^" + keyPrefix.replaceAll("([\\\\(){}|?*+^$\\[\\]])", "\\\\\\1");
    }
}
