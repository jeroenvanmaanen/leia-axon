package org.sollunae.stack.axon;

public interface StackCommand {
    String getId();

    default StackCommand send(StackCommandGateway commandGateway) {
        commandGateway.send(this);
        return this;
    }
    default String sendAndWait(StackCommandGateway commandGateway) {
        return commandGateway.sendAndWait(this);
    }
}
