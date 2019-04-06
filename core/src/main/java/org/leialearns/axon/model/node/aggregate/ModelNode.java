package org.leialearns.axon.model.node.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.leialearns.axon.model.node.command.CreateModelNodeCommandUnsafe;
import org.leialearns.axon.model.node.command.ModelNodeSetExtensibleCommand;
import org.leialearns.axon.model.node.event.ModelNodeCreatedEvent;
import org.leialearns.axon.model.node.event.ModelNodeWasMarkedAsExtensibleEvent;
import org.leialearns.axon.once.CascadingCommandTracker;
import org.leialearns.axon.once.CommandCounter;
import org.leialearns.axon.once.TriggerCommandOnceService;

@Aggregate
@Getter
@NoArgsConstructor
@Slf4j
public class ModelNode implements CascadingCommandTracker {

    @AggregateIdentifier
    private String id;

    private CommandCounter commandCounter;
    private boolean extensible = false;

    @CommandHandler
    public ModelNode(CreateModelNodeCommandUnsafe command) {
        id = command.getId();
        ModelNodeCreatedEvent.builder().id(id).data(command.getData()).build().apply();
    }

    @EventSourcingHandler
    public void on(ModelNodeCreatedEvent event, TriggerCommandOnceService onceService) {
        if (commandCounter == null) {
            commandCounter = onceService.createCounter();
        }
        id = event.getId();
        extensible = event.getData().isExtensible();
    }

    @CommandHandler
    public void handle(ModelNodeSetExtensibleCommand command) {
        if (extensible) {
            return;
        }
        extensible = true;
        ModelNodeWasMarkedAsExtensibleEvent.builder()
            .id(id)
            .build()
            .apply();
    }

    @EventSourcingHandler
    public void on(ModelNodeWasMarkedAsExtensibleEvent event) {
        extensible = true;
    }
}
