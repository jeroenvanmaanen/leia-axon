package org.leialearns.axon.lag;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.leialearns.axon.model.node.event.ModelStepEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class LagEventHandler {

    private final LagService lagService;

    public LagEventHandler(LagService lagService) {
        this.lagService = lagService;
    }

    @EventHandler
    public void on(ModelStepEvent event, @Timestamp Instant timestamp) {
        String key = Optional.ofNullable(event).map(Object::getClass).map(Class::getCanonicalName).orElse("null");
        log.trace("Log event: {}: {}", key, event);
        lagService.recordLag(key, timestamp, Instant.now());
    }
}
