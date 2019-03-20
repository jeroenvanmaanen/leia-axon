package org.leialearns.axon;

import org.axonframework.modelling.command.AggregateLifecycle;
import org.leialearns.spring.AggregateLifecycleBean;

public interface StackEvent {

    default void apply() {
        AggregateLifecycle.apply(this);
    }

    default void apply(AggregateLifecycleBean aggregateLifecycle) {
        aggregateLifecycle.apply(this);
    }
}
