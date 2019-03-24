package org.leialearns.axon.domain.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;
import org.leialearns.model.Symbol;

@Value
@Builder
@JsonDeserialize(builder = SymbolCreatedEvent.SymbolCreatedEventBuilder.class)
public class SymbolCreatedEvent implements StackEvent {
    private Symbol symbol;
}
