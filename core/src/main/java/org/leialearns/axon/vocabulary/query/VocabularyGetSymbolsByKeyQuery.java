package org.leialearns.axon.vocabulary.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = VocabularyGetSymbolsByKeyQuery.VocabularyGetSymbolsByKeyQueryBuilder.class)
public class VocabularyGetSymbolsByKeyQuery {
    String key;
}
