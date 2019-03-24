package org.leialearns.axon.vocabulary.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.vocabulary.aggregate.Vocabulary;
import org.leialearns.axon.vocabulary.command.CreateVocabularyCommand;
import org.leialearns.axon.vocabulary.command.CreateVocabularyCommandUnsafe;
import org.leialearns.axon.unique.process.UniqueKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VocabularyCommandHandler {

    private final StackCommandGateway commandGateway;
    private final UniqueKeyService uniqueKeyService;

    @Autowired
    public VocabularyCommandHandler(StackCommandGateway commandGateway, UniqueKeyService uniqueKeyService) {
        this.commandGateway = commandGateway;
        this.uniqueKeyService = uniqueKeyService;
    }

    @CommandHandler
    public String handle(CreateVocabularyCommand command) {
        String id = command.getId();
        String key = command.getKey();
        log.trace("Create vocabulary: {}: unique key service: {}", key, uniqueKeyService);
        try {
            uniqueKeyService.assertUnique(Vocabulary.class.toString(), key);
        } catch (IllegalStateException exception) {
            log.warn("Vocabulary not created; key already exists: {}: {}: {}", key, exception, String.valueOf(exception.getCause()));
            return null;
        }
        return CreateVocabularyCommandUnsafe.builder().id(id).key(key).build().sendAndWait(commandGateway);
    }
}
