package org.leialearns.axon.model.node.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.leialearns.axon.StackCommand;
import org.leialearns.model.SymbolReference;

@Value
@Builder
public class ModelNodeFetchChildCommand implements StackCommand {

    @TargetAggregateIdentifier
    private String id;

    private SymbolReference symbol;
}
