package org.leialearns.axon.vocabulary.persistence;

import lombok.Builder;
import lombok.Value;
import org.leialearns.model.Symbol;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vocabulary-symbol")
@CompoundIndex(def = "{'vocabulary':1,'symbol.ordinal':1}", unique = true)
@Value
@Builder
public class SymbolDocument {

    @Id
    String id;

    String vocabulary;
    Symbol symbol;
}
