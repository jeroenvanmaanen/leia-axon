package org.leialearns.rest;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.model.node.command.ModelNodeSetExtensibleCommand;
import org.leialearns.axon.model.node.persistence.ModelNodeDocument;
import org.leialearns.axon.model.node.query.ModelNodeByKeyQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModelStructureService implements ModelStructureApiDelegate {

    private final StackCommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public ModelStructureService(StackCommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @Override
    public ResponseEntity<String> getRootNodeId() {
        try {
            ModelNodeByKeyQuery query = ModelNodeByKeyQuery.builder().key("/").build();
            ModelNodeDocument node =  queryGateway.query(query, ModelNodeDocument.class).get();
            return ResponseEntity.ok(node.getId());
        } catch (Exception e) {
            log.error("Exception in get root note ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> markExtensible(String modelNodeId) {
        ModelNodeSetExtensibleCommand.builder()
            .id(modelNodeId)
            .build()
            .sendAndWait(commandGateway);
        return ResponseEntity.ok(null);
    }
}
