package org.leialearns.axon.vocabulary.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;

@Value
@Builder
@JsonDeserialize(builder = ClosedEvent.ClosedEventBuilder.class)
public class ClosedEvent implements StackEvent {
}
