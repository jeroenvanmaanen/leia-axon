package org.leialearns.rest;

import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.model.node.command.ModelNodeSetExtensibleCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ModelStructureService implements ModelStructureApiDelegate {

    private final StackCommandGateway commandGateway;

    public ModelStructureService(StackCommandGateway commandGateway) {
        this.commandGateway = commandGateway;
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
