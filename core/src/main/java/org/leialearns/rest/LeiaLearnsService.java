package org.leialearns.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.vocabulary.command.CreateVocabularyCommand;
import org.leialearns.axon.vocabulary.command.DeclareClosedCommand;
import org.leialearns.axon.vocabulary.command.DeclareOpenCommand;
import org.leialearns.axon.vocabulary.command.GetOrCreateSymbolCommand;
import org.leialearns.axon.vocabulary.query.AllVocabularyKeysQuery;
import org.leialearns.axon.vocabulary.query.VocabularyByKeyQuery;
import org.leialearns.axon.vocabulary.query.VocabularyGetSymbolsByKeyQuery;
import org.leialearns.model.ArrayOfString;
import org.leialearns.model.ArrayOfSymbol;
import org.leialearns.model.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
public class LeiaLearnsService implements LeiaLearnsApiDelegate {

    private StackCommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final ObjectMapper yamlObjectMapper;

    @Autowired
    public LeiaLearnsService(StackCommandGateway commandGateway, QueryGateway queryGateway,
                             @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.yamlObjectMapper = yamlObjectMapper;
        log.info("YAML object mapper: {}", yamlObjectMapper);
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
            String vocabularyId = getVocabularyId(key);
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

    private String getVocabularyId(String key) {
        int sleep = 10;
        int wait = 0;
        do {
            try {
                String vocabularyId = queryGateway.query(VocabularyByKeyQuery.builder().key(key).build(), String.class).get();
                if (vocabularyId != null) {
                    return vocabularyId;
                }
                Thread.sleep(sleep);
            } catch (Exception e) {
                log.warn("Exception while getting vocabulary ID: {}", key, e);
            }
            sleep = sleep * 3 / 2;
            wait += sleep;
        } while (wait < 2000);
        log.debug("Could not find vocabulary for key: {}", key);
        return null;
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

    @Override
    public ResponseEntity<Void> uploadVocabulary(MultipartFile data) {
        log.info("Upload vocabulary YAML");
        try {
            uploadVocabulary(data.getInputStream());
            return ResponseEntity.ok(null);
        } catch (RuntimeException | IOException exception) {
            log.error("Exception while uploading vocabulary YAML", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void uploadVocabulary(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            StringBuilder builder = new StringBuilder();
            line = reader.readLine();
            if (line == null) {
                return;
            }
            if (!line.startsWith("%") && !line.equals("---")) {
                builder.append(line);
            }
            while ((line = reader.readLine()) != null) {
                if (line.equals("---")) {
                    String item = builder.toString();
                    builder = new StringBuilder();
                    updateVocabulary(item);
                } else {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(line);
                }
            }
            updateVocabulary(builder.toString());
        }
    }

    private void updateVocabulary(String item) {
        if (StringUtils.isEmpty(item)) {
            return;
        }
        try {
            VocabularyUpdate update = yamlObjectMapper.readValue(item, VocabularyUpdate.class);
            Symbol symbol = update.getSymbol();
            String vocabulary = symbol.getVocabulary();
            switch (update.getType()) {
                case CREATE_VOCABULARY:
                    createVocabulary(vocabulary);
                    break;
                case ADD_SYMBOL:
                    getOrCreateSymbol(vocabulary, symbol.getName());
                    break;
                case CLOSE_VOCABULARY:
                    closeVocabulary(vocabulary);
                    break;
                case DECLARE_VOCABULARY_OPEN:
                    declareVocabularyOpen(vocabulary);
                    break;
                default:
                    log.warn("Unknown vocabulary update type: {}", update.getType());
            }
        } catch (Exception exception) {
            log.warn("Exception while importing vocabulary: {}: {}", exception.toString(), String.valueOf(exception.getCause()));
        }
    }
}
