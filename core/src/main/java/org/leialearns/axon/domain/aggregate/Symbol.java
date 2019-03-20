package org.leialearns.axon.domain.aggregate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
@JsonDeserialize(builder = Symbol.SymbolBuilder.class)
public class Symbol {
    private String name;
    private int ordinal;
    private Integer descriptionLength;
}
