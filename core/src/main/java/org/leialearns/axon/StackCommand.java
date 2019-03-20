package org.leialearns.axon;

public interface StackCommand extends StackCommandUnsafe {

    default StackCommand send(StackCommandGateway commandGateway) {
        commandGateway.unsafeSend(this);
        return this;
    }

    default String sendAndWait(StackCommandGateway commandGateway) {
        return commandGateway.unsafeSendAndWait(this);
    }
}
