package org.leialearns.axon.model.node.persistence;

import lombok.Builder;
import lombok.Value;
import org.leialearns.model.SymbolReference;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transition")
@Value
@Builder
public class TransitionDocument {

    @Id
    private String id;

    private String sourceId;
    private SymbolReference symbolReference;
    private String targetId;
}
