package org.leialearns.axon.once;

import org.leialearns.axon.StackEvent;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

public interface WithAllocatedTokens<T> extends StackEvent {

    String getId();
    Map<String,Long> getAllocatedTokens();
    Instant getOriginTimestamp();

    T withAllocatedTokens(Map<String,Long> segment);
    T withOriginTimestamp(Instant originTimestamp);

    @SuppressWarnings("unchecked")
    default T map(Function<T,T> mapper) {
        return mapper.apply((T) this);
    }
}
