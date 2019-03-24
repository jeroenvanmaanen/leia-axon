package org.leialearns.rest;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.vocabulary.command.CreateVocabularyCommand;
import org.leialearns.axon.vocabulary.query.VocabularyByKeyQuery;
import org.leialearns.axon.model.node.command.GetOrCreateSymbolCommand;
import org.leialearns.model.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
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
    public ResponseEntity<Void> createDomain(String key) {
        String id = CreateVocabularyCommand.builder()
            .id(UUID.randomUUID().toString())
            .key(key)
            .build()
            .sendAndWait(commandGateway);
        log.info("Created Domain: {}: {}", key, id);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Symbol> getOrCreateSymbol(String key, String symbolName) {
        try {
            String domainId = queryGateway.query(VocabularyByKeyQuery.builder().key(key).build(), String.class).get();
            Symbol symbol = commandGateway.getOrCreateSymbol(GetOrCreateSymbolCommand.builder()
                .id(domainId)
                .name(symbolName)
                .build());
            return ResponseEntity.ok(symbol);
        } catch (Exception e) {
            log.error("Internal server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> prod(String name) {
        return ResponseEntity.of(Optional.of("Hello, " + name + "!"));
    }
}
