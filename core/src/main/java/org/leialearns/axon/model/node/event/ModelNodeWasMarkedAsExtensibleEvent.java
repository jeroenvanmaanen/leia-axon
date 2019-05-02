package org.leialearns.axon.model.node.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;

@Value
@Builder
@JsonDeserialize(builder = ModelNodeWasMarkedAsExtensibleEvent.ModelNodeWasMarkedAsExtensibleEventBuilder.class)
public class ModelNodeWasMarkedAsExtensibleEvent implements StackEvent {
    private String id;
}
