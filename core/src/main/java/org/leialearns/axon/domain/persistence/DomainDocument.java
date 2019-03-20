package org.leialearns.axon.domain.persistence;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "domain")
@Value
@Builder
public class DomainDocument {

    @Id
    String id;

    String key;
}
