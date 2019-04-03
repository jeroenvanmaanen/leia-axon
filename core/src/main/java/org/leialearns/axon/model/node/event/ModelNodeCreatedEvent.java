package org.leialearns.axon.model.node.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;
import org.leialearns.model.ModelNodeData;

@Value
@Builder
@JsonDeserialize(builder = ModelNodeCreatedEvent.ModelNodeCreatedEventBuilder.class)
public class ModelNodeCreatedEvent implements StackEvent {
    private String id;
    private ModelNodeData data;
}
