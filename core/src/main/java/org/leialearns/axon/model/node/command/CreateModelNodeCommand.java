package org.leialearns.axon.model.node.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.leialearns.axon.StackCommand;
import org.leialearns.model.ModelNodeData;

@Value
@Builder
public class CreateModelNodeCommand implements StackCommand {

    @TargetAggregateIdentifier
    private String id;

    private ModelNodeData data;
    private Boolean isSilent;
}
