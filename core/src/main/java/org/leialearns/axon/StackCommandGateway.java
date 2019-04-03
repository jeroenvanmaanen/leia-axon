package org.leialearns.axon;

import org.leialearns.axon.vocabulary.command.GetOrCreateSymbolCommand;
import org.leialearns.model.Symbol;

public interface StackCommandGateway {
    String unsafeSendAndWait(StackCommandUnsafe command);
    void unsafeSend(StackCommandUnsafe command);
    Symbol getOrCreateSymbol(GetOrCreateSymbolCommand command);
}
