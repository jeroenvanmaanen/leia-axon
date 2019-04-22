package org.leialearns.axon.model.node.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Aggregate;
import org.bson.types.ObjectId;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.model.node.command.CreateModelNodeCommandUnsafe;
import org.leialearns.axon.model.node.command.ModelNodeFetchChildCommand;
import org.leialearns.axon.model.node.command.ModelNodeSetExtensibleCommand;
import org.leialearns.axon.model.node.command.ModelStepCommand;
import org.leialearns.axon.model.node.event.ModelNodeChildAddedEvent;
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
    private List<SymbolReference> path;

    private CommandCounter commandCounter;

    private SymbolReference mostRecent = null;
    private boolean extensible = false;
    private final Set<String> incoming = new HashSet<>();
    private final Map<SymbolReference,String> children = new HashMap<>();

    @CommandHandler
    public ModelNode(CreateModelNodeCommandUnsafe command, QueryGateway queryGateway) {
        id = command.getId();
        path = command.getData().getPath();
        ModelNodeData data = command.getData();
        data.setId(id);
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
        path = event.getData().getPath();
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
    public String handle(ModelNodeFetchChildCommand command, StackCommandGateway commandGateway, ModelNodeHelper helper) {
        try {
            SymbolReference symbol = command.getSymbol();
            return children.computeIfAbsent(symbol, s -> createChild(symbol, commandGateway, helper));
        } catch (RuntimeException exception) {
            log.error("Caught exception", exception);
            throw exception;
        }
    }

    private String createChild(SymbolReference symbol, StackCommandGateway commandGateway, ModelNodeHelper helper) {
        ModelNodeData data = new ModelNodeData();
        data.setId(ObjectId.get().toString());
        List<SymbolReference> childPath = new ArrayList<>(this.path);
        childPath.add(symbol);
        data.setPath(childPath);
        data.setDepth(childPath.size());
        data.setKey(helper.getKey(childPath));
        data.setExtensible(false);
        String childId = CreateModelNodeCommandUnsafe.builder().id(data.getId()).data(data).build().sendAndWait(commandGateway);
        ModelNodeChildAddedEvent.builder().id(id).childId(childId).symbol(symbol).build().apply();
        return childId;
    }

    @EventSourcingHandler
    public void on(ModelNodeChildAddedEvent event) {
        children.put(event.getSymbol(), event.getChildId());
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
