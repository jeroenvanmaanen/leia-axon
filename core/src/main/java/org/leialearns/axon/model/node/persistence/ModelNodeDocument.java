package org.leialearns.axon.model.node.persistence;

import lombok.Builder;
import lombok.Value;
import org.leialearns.model.ModelNodeData;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "model-node")
@Value
@Builder
@CompoundIndexes(
    @CompoundIndex(name = "key", def = "{ 'data.key' : 1 }")
)
public class ModelNodeDocument {

    @Id
    String id;

    ModelNodeData data;
}
