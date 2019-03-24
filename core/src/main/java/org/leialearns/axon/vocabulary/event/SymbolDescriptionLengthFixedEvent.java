package org.leialearns.axon.vocabulary.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;
import org.leialearns.model.Symbol;

@Value
@Builder
@JsonDeserialize(builder = SymbolDescriptionLengthFixedEvent.SymbolDescriptionLengthFixedEventBuilder.class)
public class SymbolDescriptionLengthFixedEvent implements StackEvent {
    private Symbol symbol;
}
