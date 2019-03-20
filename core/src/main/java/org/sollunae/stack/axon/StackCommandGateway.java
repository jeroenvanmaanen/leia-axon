package org.sollunae.stack.axon;

public interface StackCommandGateway {
    String sendAndWait(StackCommand ledgerCommand);
    void send(StackCommand ledgerCommand);
}
