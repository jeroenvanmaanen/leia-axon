package org.leialearns.axon.domain.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DomainByKeyQuery {
    String key;
}
