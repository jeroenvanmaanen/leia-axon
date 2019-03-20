package org.leialearns.axon.unique.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.axon.StackEvent;

@Value
@Builder
@JsonDeserialize(builder = UniqueBucketChildAddedEvent.UniqueBucketChildAddedEventBuilder.class)
public class UniqueBucketChildAddedEvent implements StackEvent {
    String parentId;
    String childId;
    String keyPrefix;
    String fullPrefix;
}
