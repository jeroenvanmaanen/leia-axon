package org.leialearns.axon.model.node.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.leialearns.axon.model.node.command.CreateNodeCommand;
import org.leialearns.axon.model.node.event.NodeCreatedEvent;
import org.leialearns.axon.once.CascadingCommandTracker;
import org.leialearns.axon.once.CommandCounter;
import org.leialearns.axon.once.TriggerCommandOnceService;

@Aggregate
@Getter
@NoArgsConstructor
@Slf4j
public class Node implements CascadingCommandTracker {

    @AggregateIdentifier
    private String id;

    private CommandCounter commandCounter;

    @CommandHandler
    public Node(CreateNodeCommand command) {
        id = command.getId();
        NodeCreatedEvent.builder().id(id).build().apply();
    }

    @EventSourcingHandler
    public void on(NodeCreatedEvent event, TriggerCommandOnceService onceService) {
        if (commandCounter == null) {
            commandCounter = onceService.createCounter();
        }
        id = event.getId();
    }
}
