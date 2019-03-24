package org.leialearns.axon.domain.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;

@Value
@Builder
@JsonDeserialize(builder = VocabularyCreatedEvent.VocabularyCreatedEventBuilder.class)
public class VocabularyCreatedEvent implements StackEvent {
    String id;
    String key;
}
