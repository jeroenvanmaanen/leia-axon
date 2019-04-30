package org.leialearns.axon.lag;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lag-statistics")
@CompoundIndex(def = "{ 'key': 1, 'minute': 1 }")
@Value
@Builder
public class LagDocument {

    @Id
    String id;

    String key;
    Long minute;

    double weight;
    double value;
}
