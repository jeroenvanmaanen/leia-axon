package org.leialearns.axon.once;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@JsonDeserialize(builder = TokenFulfilledEvent.TokenFulfilledEventBuilder.class)
public class TokenFulfilledEvent {
    private String id;
    private String sourceId;
    private long token;
    private Instant originTimestamp;
}
