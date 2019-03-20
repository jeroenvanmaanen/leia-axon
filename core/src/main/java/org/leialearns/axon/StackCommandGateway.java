package org.leialearns.axon;

import org.leialearns.axon.domain.aggregate.Symbol;
import org.leialearns.axon.model.node.command.GetOrCreateSymbolCommand;

public interface StackCommandGateway {
    String unsafeSendAndWait(StackCommandUnsafe command);
    void unsafeSend(StackCommandUnsafe command);
    Symbol getOrCreateSymbol(GetOrCreateSymbolCommand command);
}
