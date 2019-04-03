package org.leialearns.axon.model.node.persistence;

import lombok.Builder;
import lombok.Value;
import org.leialearns.model.ModelNodeData;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "model-node")
@Value
@Builder
public class ModelNodeDocument {

    @Id
    String id;

    ModelNodeData data;
}
