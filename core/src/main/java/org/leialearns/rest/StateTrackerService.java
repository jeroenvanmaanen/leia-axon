package org.leialearns.rest;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.bson.types.ObjectId;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.model.node.aggregate.ModelNodeHelper;
import org.leialearns.axon.model.node.command.CreateModelNodeCommand;
import org.leialearns.axon.model.node.command.ModelNodeFetchChildCommand;
import org.leialearns.axon.model.node.command.ModelNodeSetExtensibleCommand;
import org.leialearns.axon.model.node.command.ModelStepCommand;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.leialearns.axon.model.node.query.ModelNodeByIdQuery;
import org.leialearns.axon.model.node.query.ModelNodeByKeyQuery;
import org.leialearns.axon.model.node.query.NextModelNodeQuery;
import org.leialearns.model.ModelNodeData;
import org.leialearns.model.Symbol;
import org.leialearns.model.SymbolReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class StateTrackerService implements StateTrackerApiDelegate {

    private final ModelNodeHelper helper;
    private final VocabularyService vocabularyService;
    private final StackCommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final QueryService queryService;

    public StateTrackerService(
        ModelNodeHelper helper, VocabularyService vocabularyService,
        StackCommandGateway commandGateway, QueryGateway queryGateway, QueryService queryService
    ) {
        this.helper = helper;
        this.vocabularyService = vocabularyService;
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.queryService = queryService;
    }

    @Override
    public ResponseEntity<String> recordActionStep(String currentStateId, String vocabulary, String symbol) {
        try {
            String nextStateId = advance(currentStateId, vocabulary, symbol);
            return ResponseEntity.ok(nextStateId);
        } catch (Exception e) {
            log.error("Error while recording action: {}: {}: {}", currentStateId, vocabulary, symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> recordPerceptionStep(String currentStateId, String vocabulary, String symbol) {
        try {
            recordSymbol(currentStateId, vocabulary, symbol);
            String nextStateId = advance(currentStateId, vocabulary, symbol);
            return ResponseEntity.ok(nextStateId);
        } catch (Exception e) {
            log.error("Error while recording perception: {}: {}: {}", currentStateId, vocabulary, symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public void recordSymbol(String currentStateId, String vocabulary, String symbol) {
        log.debug("Record symbol: {} -> {}:{}", currentStateId, vocabulary, symbol);
        // TODO: record symbol on current state
    }

    public String advance(String currentStateId, String vocabulary, String symbolName) throws ExecutionException, InterruptedException {
        log.debug("Advance: {} -({}:{})-> ?", currentStateId, vocabulary, symbolName);
        ModelNodeDocument rootNode = null;
        if (StringUtils.isEmpty(currentStateId)) {
            rootNode = getOrCreateRootNode();
            currentStateId = rootNode.getId();
        }
        Symbol symbol = vocabularyService.getOrCreateSymbolInternal(vocabulary, symbolName);
        SymbolReference symbolReference = new SymbolReference().vocabulary(vocabulary).ordinal(symbol.getOrdinal());
        String nextStateId = queryGateway.query(NextModelNodeQuery.builder().currentNodeId(currentStateId).nextSymbol(symbolReference).build(), String.class).get();
        ModelNodeData nextState;
        if (nextStateId == null) {
            if (rootNode == null) {
                rootNode = getOrCreateRootNode();
            }
            nextStateId = rootNode.getId();
            nextState = rootNode.getData();
        } else {
            nextState = queryGateway.query(ModelNodeByIdQuery.builder().id(nextStateId).build(), ModelNodeData.class).get();
        }
        if (nextState.isExtensible()) {
            log.debug("Next state before extending: {}", nextState);
            ModelNodeData currentState = null;
            List<SymbolReference> currentPath;
            if (StringUtils.isEmpty(currentStateId)) {
                currentPath = Collections.emptyList();
            } else {
                currentState = queryGateway.query(ModelNodeByIdQuery.builder().id(currentStateId).build(), ModelNodeData.class).get();
                currentPath = Optional.ofNullable(currentState).map(ModelNodeData::getPath).orElse(Collections.emptyList());
            }
            if (nextState.getPath().isEmpty()) {
                log.debug("Extending root node ({}) with: {}:{} ({})", nextStateId, vocabulary, symbol.getOrdinal(), symbolName);
                nextStateId = ModelNodeFetchChildCommand.builder().id(nextStateId).symbol(symbolReference).build().sendAndWait(commandGateway);
            } else if (currentPath.size() >= nextState.getPath().size()) {
                SymbolReference extension = currentPath.get(nextState.getPath().size() - 1);
                log.debug("Extending node ({}) with: {}:{}", nextStateId, extension.getVocabulary(), extension.getOrdinal());
                nextStateId = ModelNodeFetchChildCommand.builder().id(nextStateId).symbol(extension).build().sendAndWait(commandGateway);
            } else if (currentState != null && !currentState.isExtensible()) {
                log.debug("Marking current node as extensible, because next state is: {}: -({}:{})-> {}",
                    currentStateId, vocabulary, symbolName, nextStateId);
                ModelNodeSetExtensibleCommand.builder().id(currentStateId).build().send(commandGateway);
            }
        }
        recordStep(currentStateId, symbol, nextStateId);
        return nextStateId;
    }

    private ModelNodeDocument getOrCreateRootNode() {
        List<SymbolReference> path = Collections.emptyList();
        String key = helper.getKey(path);
        ModelNodeDocument result;
        try {
            result = queryGateway.query(ModelNodeByKeyQuery.builder().key(key).build(), ModelNodeDocument.class).get();
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            log.trace("Get by key failed: {}", key);
        }
        ModelNodeData data = new ModelNodeData();
        data.path(path);
        data.key(key);
        data.extensible(true); // The root node is extensible by default
        String id = CreateModelNodeCommand.builder()
            .id(ObjectId.get().toString())
            .data(data)
            .isSilent(true)
            .build()
            .sendAndWait(commandGateway);
        log.debug("Result of create model node: {}: {}", key, id);
        if (id == null) {
            Object query = ModelNodeByKeyQuery.builder().key(key).build();
            result = queryService.queryWithRetry("Get existing Model Node", query, ModelNodeDocument.class);
        } else {
            result = ModelNodeDocument.builder().id(id).data(data).build();
        }
        log.debug("Result of query model node: {}: {}", key, Optional.ofNullable(result).map(ModelNodeDocument::getId).orElse(null));
        return result;
    }

    private void recordStep(String currentStateId, Symbol symbol, String nextStateId) {
        log.debug("Next state: {}", nextStateId);
        SymbolReference symbolReference = new SymbolReference().vocabulary(symbol.getVocabulary()).ordinal(symbol.getOrdinal());
        ModelStepCommand.builder()
            .previousModelNodeId(currentStateId)
            .symbol(symbolReference)
            .id(nextStateId)
            .build()
            .send(commandGateway);
    }
}
