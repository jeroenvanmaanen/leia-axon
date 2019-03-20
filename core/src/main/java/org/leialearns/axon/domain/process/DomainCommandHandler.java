package org.leialearns.axon.domain.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.domain.aggregate.Domain;
import org.leialearns.axon.domain.command.CreateDomainCommand;
import org.leialearns.axon.domain.command.CreateDomainCommandUnsafe;
import org.leialearns.axon.unique.process.UniqueKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DomainCommandHandler {

    private final StackCommandGateway commandGateway;
    private final UniqueKeyService uniqueKeyService;

    @Autowired
    public DomainCommandHandler(StackCommandGateway commandGateway, UniqueKeyService uniqueKeyService) {
        this.commandGateway = commandGateway;
        this.uniqueKeyService = uniqueKeyService;
    }

    @CommandHandler
    public String handle(CreateDomainCommand command) {
        String id = command.getId();
        String key = command.getKey();
        log.trace("Create domain: {}: unique key service: {}", key, uniqueKeyService);
        try {
            uniqueKeyService.assertUnique(Domain.class.toString(), key);
        } catch (IllegalStateException exception) {
            log.warn("Domain not created; key already exists: {}: {}: {}", key, exception, String.valueOf(exception.getCause()));
            return null;
        }
        return CreateDomainCommandUnsafe.builder().id(id).key(key).build().sendAndWait(commandGateway);
    }
}
