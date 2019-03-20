package org.sollunae.stack.axon;

import org.axonframework.modelling.command.AggregateLifecycle;
import org.sollunae.stack.spring.AggregateLifecycleBean;

public interface StackEvent {

    default void apply() {
        AggregateLifecycle.apply(this);
    }

    default void apply(AggregateLifecycleBean aggregateLifecycle) {
        aggregateLifecycle.apply(this);
    }
}
