package org.leialearns.axon.domain.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.leialearns.axon.StackCommand;

@Value
@Builder
public class CreateVocabularyCommandUnsafe implements StackCommand {

    @TargetAggregateIdentifier
    private String id;

    private String key;
}
