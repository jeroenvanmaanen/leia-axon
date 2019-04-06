package org.leialearns.axon.model.node.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ModelNodeDescendantsQuery {
    private String keyPrefix;
}
