package org.leialearns.axon.once;

import java.time.Instant;
import java.util.function.Function;

public interface CascadingCommand<T extends CascadingCommand<T>> {
    String getId();
    String getSourceAggregateIdentifier();
    long getAllocatedToken();
    Instant getOriginTimestamp();
    T withAllocatedToken(long token);
    T withSourceAggregateIdentifier(String sourceId);
    T withOriginTimestamp(Instant originTimestamp);

    @SuppressWarnings("unchecked")
    default T map(Function<T,T> mapper) {
        return mapper.apply((T) this);
    }
}
