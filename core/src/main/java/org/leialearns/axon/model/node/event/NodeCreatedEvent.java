package org.leialearns.axon.model.node.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;

@Value
@Builder
@JsonDeserialize(builder = NodeCreatedEvent.NodeCreatedEventBuilder.class)
public class NodeCreatedEvent implements StackEvent {
    private String id;
}
