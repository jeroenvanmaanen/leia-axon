package org.leialearns.axon.encounter.process;

import org.axonframework.eventhandling.EventHandler;
import org.leialearns.axon.encounter.event.EncounterCreatedEvent;
import org.leialearns.axon.encounter.persistence.EncounterDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountEventHandler {

    @EventHandler
    public void on(EncounterCreatedEvent event, MongoTemplate mongoTemplate) {
        try {
            mongoTemplate.insert(EncounterDocument.builder().id(event.getId()).build());
        } catch (RuntimeException exception) {
            throw new RuntimeException("Could not insert unique key document", exception);
        }
    }
}
