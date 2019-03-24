package org.leialearns.axon.vocabulary.query;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VocabularyByKeyQuery {
    String key;
}
