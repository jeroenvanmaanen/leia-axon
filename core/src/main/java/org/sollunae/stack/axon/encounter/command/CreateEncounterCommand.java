package org.sollunae.stack.axon.encounter.command;

import lombok.Builder;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
public class CreateEncounterCommand {

    @TargetAggregateIdentifier
    private String id;
}
