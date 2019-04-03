package org.leialearns.axon.model.node.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ModelNodeByIdQuery {
    private String id;
}
