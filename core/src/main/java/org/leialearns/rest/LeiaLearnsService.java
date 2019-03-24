package org.leialearns.rest;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.vocabulary.command.CreateVocabularyCommand;
import org.leialearns.axon.vocabulary.command.DeclareClosedCommand;
import org.leialearns.axon.vocabulary.command.DeclareOpenCommand;
import org.leialearns.axon.vocabulary.query.AllVocabularyKeysQuery;
import org.leialearns.axon.vocabulary.query.VocabularyByKeyQuery;
import org.leialearns.axon.model.node.command.GetOrCreateSymbolCommand;
import org.leialearns.axon.vocabulary.query.VocabularyGetSymbolsByKeyQuery;
import org.leialearns.model.ArrayOfString;
import org.leialearns.model.ArrayOfSymbol;
import org.leialearns.model.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
public class LeiaLearnsService implements LeiaLearnsApiDelegate {

    private StackCommandGateway commandGateway;
    private final QueryGateway queryGateway;

    @Autowired
    public LeiaLearnsService(StackCommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("Post construct: {}", getClass());
    }

    @Override
    public ResponseEntity<Void> createVocabulary(String key) {
        String id = CreateVocabularyCommand.builder()
            .id(UUID.randomUUID().toString())
            .key(key)
            .build()
            .sendAndWait(commandGateway);
        log.info("Created Vocabulary: {}: {}", key, id);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<ArrayOfString> getVocabularyKeys() {
        try {
            ArrayOfString result = new ArrayOfString();
            String[] keys = queryGateway.query(new AllVocabularyKeysQuery(), String[].class).get();
            result.addAll(Arrays.asList(keys));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Symbol> getOrCreateSymbol(String key, String symbolName) {
        try {
            String vocabularyId = queryGateway.query(VocabularyByKeyQuery.builder().key(key).build(), String.class).get();
            Symbol symbol = commandGateway.getOrCreateSymbol(GetOrCreateSymbolCommand.builder()
                .id(vocabularyId)
                .name(symbolName)
                .build());
            return ResponseEntity.ok(symbol);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ArrayOfSymbol> getSymbols(String key) {
        try {
            ArrayOfSymbol result = new ArrayOfSymbol();
            Symbol[] symbols = queryGateway.query(VocabularyGetSymbolsByKeyQuery.builder().key(key).build(), Symbol[].class).get();
            result.addAll(Arrays.asList(symbols));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> closeVocabulary(String key) {
        String id = getVocabularyId(key);
        log.debug("Close vocabulary: {}: {}", key, id);
        DeclareClosedCommand.builder().id(id).build().send(commandGateway);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Void> declareVocabularyOpen(String key) {
        String id = getVocabularyId(key);
        log.debug("Declare vocabulary open: {}: {}", key, id);
        DeclareOpenCommand.builder().id(id).build().send(commandGateway);
        return ResponseEntity.ok(null);
    }

    private String getVocabularyId(String key) {
        try {
            return queryGateway.query(VocabularyByKeyQuery.builder().key(key).build(), String.class).get();
        } catch (Exception e) {
            log.debug("Could not find vocabulary for key: {}", key, e);
            throw new RuntimeException();
        }
    }
}
