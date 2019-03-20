package org.leialearns.axon.encounter.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EncounterQueryRepository extends MongoRepository<EncounterDocument,String> {
}
