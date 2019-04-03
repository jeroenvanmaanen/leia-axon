package org.leialearns.axon.model.node.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.model.node.aggregate.ModelNode;
import org.leialearns.axon.model.node.command.CreateModelNodeCommand;
import org.leialearns.axon.model.node.command.CreateModelNodeCommandUnsafe;
import org.leialearns.axon.unique.process.UniqueKeyService;
import org.leialearns.model.ModelNodeData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModelNodeCommandHandler {

    private final UniqueKeyService uniqueKeyService;
    private final StackCommandGateway commandGateway;

    public ModelNodeCommandHandler(UniqueKeyService uniqueKeyService, StackCommandGateway commandGateway) {
        this.uniqueKeyService = uniqueKeyService;
        this.commandGateway = commandGateway;
    }

    @CommandHandler
    public String handle(CreateModelNodeCommand command) {
        String id = command.getId();
        ModelNodeData data = command.getData();
        String key = data.getKey();
        log.trace("Create Model Node: {}: unique key service: {}", key, uniqueKeyService);
        try {
            uniqueKeyService.assertUnique(ModelNode.class.toString(), key);
        } catch (IllegalStateException exception) {
            if (command.getIsSilent() == Boolean.TRUE) {
                log.trace("Model Node not created; key already exists: {}: {}: {}", key, exception, String.valueOf(exception.getCause()));
            } else {
                log.warn("Model Node not created; key already exists: {}: {}: {}", key, exception, String.valueOf(exception.getCause()));
            }
            return null;
        }
        return CreateModelNodeCommandUnsafe.builder().id(id).data(data).build().sendAndWait(commandGateway);
    }
}
