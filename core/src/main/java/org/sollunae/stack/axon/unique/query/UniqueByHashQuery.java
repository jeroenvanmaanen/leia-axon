package org.sollunae.stack.axon.unique.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniqueByHashQuery {
    private String hashCode;
}
