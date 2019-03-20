package org.leialearns.axon.model.node.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.leialearns.axon.StackCommand;

@Value
@Builder
public class GetOrCreateSymbolCommand implements StackCommand {

    @TargetAggregateIdentifier
    private String id;

    private String name;
}
