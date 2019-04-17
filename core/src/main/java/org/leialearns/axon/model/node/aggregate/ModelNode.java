package org.leialearns.axon.model.node.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Aggregate;
import org.leialearns.axon.model.node.command.CreateModelNodeCommandUnsafe;
import org.leialearns.axon.model.node.command.ModelNodeSetExtensibleCommand;
import org.leialearns.axon.model.node.command.ModelStepCommand;
import org.leialearns.axon.model.node.event.ModelNodeCreatedEvent;
import org.leialearns.axon.model.node.event.ModelNodeWasMarkedAsExtensibleEvent;
import org.leialearns.axon.model.node.event.ModelStepEvent;
import org.leialearns.axon.model.node.query.ModelNodeDescendantsQuery;
import org.leialearns.axon.once.CascadingCommandTracker;
import org.leialearns.axon.once.CommandCounter;
import org.leialearns.axon.once.TriggerCommandOnceService;
import org.leialearns.model.ModelNodeData;
import org.leialearns.model.SymbolReference;
import org.leialearns.util.StreamUtil;

import java.util.*;

@Aggregate
@Getter
@NoArgsConstructor
@Slf4j
public class ModelNode implements CascadingCommandTracker {

    @AggregateIdentifier
    private String id;

    private CommandCounter commandCounter;

    private SymbolReference mostRecent = null;
    private boolean extensible = false;
    private Set<String> incoming = new HashSet<>();

    @CommandHandler
    public ModelNode(CreateModelNodeCommandUnsafe command, QueryGateway queryGateway) {
        id = command.getId();
        ModelNodeData data = command.getData();
        data.setDepth(data.getPath().size());
        ModelNodeCreatedEvent.builder().id(id).data(command.getData()).build().apply();
        Collection<SymbolReference> path = command.getData().getPath();
        addTransitions(path, queryGateway);
    }

    @EventSourcingHandler
    public void on(ModelNodeCreatedEvent event, TriggerCommandOnceService onceService) {
        if (commandCounter == null) {
            commandCounter = onceService.createCounter();
        }
        id = event.getId();
        extensible = event.getData().isExtensible();
        Collection<SymbolReference> path = event.getData().getPath();
        if (!path.isEmpty()) {
            mostRecent = path.iterator().next();
        }
    }

    private void addTransitions(Collection<SymbolReference> path, QueryGateway queryGateway) {
        if (path.isEmpty()) {
            log.debug("Path is empty");
            return;
        }
        Iterator<SymbolReference> it = path.iterator();
        SymbolReference first = it.next();
        SymbolReference[] pathPrefix = StreamUtil.asStream(() -> it).toArray(SymbolReference[]::new);
        try {
            String[] sourceIds = queryGateway.query(ModelNodeDescendantsQuery.builder().pathPrefix(pathPrefix).build(), String[].class).get();
            log.debug("Number of source identifiers: {}", sourceIds.length);
            for (String sourceId : sourceIds) {
                addTransition(sourceId, first, id);
            }
        } catch (Exception e) {
            log.warn("Exception while adding transitions", e);
        }
    }

    private void addTransition(String sourceId, SymbolReference first, String id) {
        ModelStepEvent.builder()
            .previousModelNodeId(sourceId)
            .symbol(first)
            .id(id)
            .build()
            .apply();
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

    @CommandHandler
    public void handle(ModelStepCommand command, ModelNodeHelper helper) {
        String sourceId = command.getPreviousModelNodeId();
        SymbolReference symbol = command.getSymbol();
        String symbolLabel = helper.show(symbol);
        if (mostRecent != null && !mostRecent.equals(command.getSymbol())) {
            throw new IllegalArgumentException(String.format("Step symbol mismatch: %s -(%s)-> %s: %s", sourceId, symbolLabel, id, mostRecent));
        }
        if (incoming.contains(sourceId)) {
            log.trace("Already registered: {} -({})-> {}", sourceId, symbolLabel, id);
            return;
        }
        log.debug("Model step: {} -({})-> {}", sourceId, symbolLabel, id);
        ModelStepEvent.builder()
            .previousModelNodeId(sourceId)
            .symbol(symbol)
            .id(id)
            .build()
            .apply();
    }

    @EventSourcingHandler
    public void on(ModelStepEvent event) {
        incoming.add(event.getPreviousModelNodeId());
    }
}
