package org.leialearns.axon;

import org.leialearns.axon.once.CascadingCommand;
import org.leialearns.axon.once.TriggerCommandOnceService;
import org.leialearns.axon.once.WithAllocatedTokens;

public interface StackCommandCascade extends StackCommandUnsafe, CascadingCommand<StackCommandCascade> {

    default StackCommandCascade send(
        StackCommandGateway commandGateway, WithAllocatedTokens event, TriggerCommandOnceService onceService
    ) {
        commandGateway.unsafeSend(prepare(event, onceService));
        return this;
    }

    default String sendAndWait(
        StackCommandGateway commandGateway, WithAllocatedTokens event, TriggerCommandOnceService onceService
    ) {
        return commandGateway.unsafeSendAndWait(prepare(event, onceService));
    }

    default StackCommandCascade prepare(WithAllocatedTokens<?> event, TriggerCommandOnceService onceService) {
        return onceService.<StackCommandCascade>prepareCommand(event).apply(this);
    }
}
