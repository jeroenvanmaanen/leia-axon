package org.sollunae.stack.axon.encounter.persistence;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "encounter")
@Value
@Builder
public class EncounterDocument {

    @Id
    private String id;
}
