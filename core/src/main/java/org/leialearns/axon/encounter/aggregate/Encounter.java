package org.leialearns.axon.encounter.aggregate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.leialearns.axon.encounter.event.EncounterCreatedEvent;
import org.leialearns.axon.unique.process.UniqueKeyService;
import org.leialearns.axon.encounter.command.CreateEncounterCommand;

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
