package org.leialearns.axon.domain.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;

@Value
@Builder
@JsonDeserialize(builder = DomainCreatedEvent.DomainCreatedEventBuilder.class)
public class DomainCreatedEvent implements StackEvent {
    String id;
    String key;
}
