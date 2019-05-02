package org.leialearns.axon.lag;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class LagEventHandler {

    @Getter
    private Instant last = Instant.now();

    @EventHandler
    public void on(Object event, @Timestamp Instant timestamp) {
        log.trace("Event: {}: {}", timestamp, event);
        last = timestamp;
    }
}
