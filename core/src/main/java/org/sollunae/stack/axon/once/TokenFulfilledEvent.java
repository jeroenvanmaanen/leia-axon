package org.sollunae.stack.axon.once;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = TokenFulfilledEvent.TokenFulfilledEventBuilder.class)
public class TokenFulfilledEvent {
    private String id;
    private String sourceId;
    private long token;
}
