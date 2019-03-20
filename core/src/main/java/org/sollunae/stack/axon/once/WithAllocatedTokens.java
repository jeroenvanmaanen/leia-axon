package org.sollunae.stack.axon.once;

import org.sollunae.stack.axon.StackEvent;

import java.util.Map;
import java.util.function.Function;

public interface WithAllocatedTokens<T> extends StackEvent {

    String getId();
    Map<String,Long> getAllocatedTokens();

    T withAllocatedTokens(Map<String,Long> segment);

    @SuppressWarnings("unchecked")
    default T map(Function<T,T> mapper) {
        return mapper.apply((T) this);
    }
}
