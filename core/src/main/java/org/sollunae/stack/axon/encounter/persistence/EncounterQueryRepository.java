package org.sollunae.stack.axon.encounter.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EncounterQueryRepository extends MongoRepository<EncounterDocument,String> {
}
