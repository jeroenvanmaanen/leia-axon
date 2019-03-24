package org.leialearns.axon.vocabulary.persistence;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vocabulary")
@Value
@Builder
public class VocabularyDocument {

    @Id
    String id;

    String key;
}
