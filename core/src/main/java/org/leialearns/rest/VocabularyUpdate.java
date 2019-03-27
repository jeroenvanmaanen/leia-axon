package org.leialearns.rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import org.leialearns.model.Symbol;

@Value
@Builder
@JsonDeserialize(builder = VocabularyUpdate.VocabularyUpdateBuilder.class)
public class VocabularyUpdate {
    private Symbol symbol;
    private VocabularyUpdateType type;
}
