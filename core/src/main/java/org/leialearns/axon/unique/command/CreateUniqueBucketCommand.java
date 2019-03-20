package org.leialearns.axon.unique.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.leialearns.axon.StackCommand;

@Value
@Builder
public class CreateUniqueBucketCommand implements StackCommand {

    @TargetAggregateIdentifier
    private String id;

    private String fullPrefix;
    private int maxKeys;
    private int childKeyPrefixLength;
}
