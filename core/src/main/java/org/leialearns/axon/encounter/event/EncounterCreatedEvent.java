package org.leialearns.axon.encounter.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = EncounterCreatedEvent.EncounterCreatedEventBuilder.class)
public class EncounterCreatedEvent {
    private String id;
}
