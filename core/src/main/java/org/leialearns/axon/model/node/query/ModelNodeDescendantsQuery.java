package org.leialearns.axon.model.node.query;

import lombok.Builder;
import lombok.Value;
import org.leialearns.model.SymbolReference;

@Value
@Builder
public class ModelNodeDescendantsQuery {
    private SymbolReference[] pathPrefix;
}
