package org.leialearns.axon.model.node.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;
import org.leialearns.model.SymbolReference;

@Value
@Builder
@JsonDeserialize(builder = ModelNodeChildAddedEvent.ModelNodeChildAddedEventBuilder.class)
public class ModelNodeChildAddedEvent implements StackEvent {
    private String id;
    private String childId;
    private SymbolReference symbol;
}
