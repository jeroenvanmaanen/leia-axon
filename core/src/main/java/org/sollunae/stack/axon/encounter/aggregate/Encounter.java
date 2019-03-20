package org.sollunae.stack.axon.encounter.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.sollunae.stack.axon.encounter.command.CreateEncounterCommand;
import org.sollunae.stack.axon.encounter.event.EncounterCreatedEvent;
import org.sollunae.stack.axon.unique.process.UniqueKeyService;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Getter
@NoArgsConstructor
@Slf4j
public class Encounter {

    @AggregateIdentifier
    private String id;

    @CommandHandler
    public Encounter(CreateEncounterCommand createCommand, UniqueKeyService uniqueKeyService) {
        id = createCommand.getId();
        apply(EncounterCreatedEvent.builder().id(id).build());
    }

    @EventSourcingHandler
    public void on(EncounterCreatedEvent encounterCreatedEvent) {
        id = encounterCreatedEvent.getId();
    }
}
