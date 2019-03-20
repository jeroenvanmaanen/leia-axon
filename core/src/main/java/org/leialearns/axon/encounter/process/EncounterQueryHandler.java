package org.leialearns.axon.encounter.process;

import org.axonframework.queryhandling.QueryHandler;
import org.leialearns.axon.encounter.persistence.EncounterDocument;
import org.leialearns.axon.encounter.persistence.EncounterQueryRepository;
import org.leialearns.axon.encounter.query.EncounterAllQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EncounterQueryHandler {

    private final EncounterQueryRepository encounterQueryRepository;

    @Autowired
    public EncounterQueryHandler(EncounterQueryRepository encounterQueryRepository) {
        this.encounterQueryRepository = encounterQueryRepository;
    }

    @QueryHandler
    public List<String> query(EncounterAllQuery query) {
        List<EncounterDocument> encounters = encounterQueryRepository.findAll();
        return encounters.stream().map(EncounterDocument::getId).collect(Collectors.toList());
    }
}
