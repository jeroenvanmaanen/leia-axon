package org.leialearns.axon.lag;

import lombok.extern.slf4j.Slf4j;
import org.leialearns.axon.once.WithAllocatedTokens;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class LagService {
    private static final double DECAY = 1.0;
    private static final double DECAY_PER_MINUTE = 1.0 + DECAY; // relevance per minute
    private static final double DECAY_PER_SECOND = Math.pow(DECAY_PER_MINUTE, 1.0/60);
    private static final int SECONDS_PER_MINUTE = 60;
    private static final long THROTTLE_BLOCK = 10;
    private static final double THROTTLE_THRESHOLD = 2.0;

    private final LagEventHandler eventHandler;
    private final LagRepository repository;
    private final MongoTemplate mongoTemplate;

    public LagService(LagEventHandler eventHandler, LagRepository repository, MongoTemplate mongoTemplate) {
        this.eventHandler = eventHandler;
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public void recordLag(Object event, Instant eventTimestamp) {
        String key = Optional.ofNullable(event).map(Object::getClass).map(Class::getCanonicalName).orElse("null");
        Optional<Instant> timestamp = Optional.empty();
        if (event instanceof WithAllocatedTokens) {
            timestamp = Optional.of((WithAllocatedTokens) event).map(WithAllocatedTokens::getOriginTimestamp);
        }
        recordLag(key, timestamp.orElse(eventTimestamp), Instant.now());
    }

    public void recordLag(String key, Instant from, Instant to) {
        long epochSecond = to.getEpochSecond();
        long epochMinute = epochSecond / SECONDS_PER_MINUTE;
        double weight = Math.pow(DECAY_PER_SECOND, epochSecond % SECONDS_PER_MINUTE);
        double value = weight * (epochSecond - from.getEpochSecond());
        Update update = Update.update("key", key).set("minute", epochMinute)
            .inc("weight", weight)
            .inc("value", value);
        Query query = Query.query(Criteria.where("key").is(key).and("minute").is(epochMinute));
        mongoTemplate.upsert(query, update, LagDocument.class);
    }

    public double getLag() {
        Iterable<LagDocument> lagDocuments = repository.findTop100ByOrderByMinuteDescKeyAsc();
        Long reference = Instant.now().getEpochSecond() / SECONDS_PER_MINUTE;
        double weight = 1.0d;
        double value = 0.0d;
        for (LagDocument lagDocument : lagDocuments) {
            double factor = Math.pow(DECAY_PER_MINUTE, lagDocument.getMinute() - reference);
            log.debug("Current lag: {}: {}: {}", reference, factor, lagDocument);
            weight += factor * lagDocument.getWeight();
            value += factor * lagDocument.getValue();
        }
        double result = value / weight;
        double decay = 1.0;
        if (result <= 0.0d) {
            result = 0.0d;
        } else {
            double idle = Duration.between(eventHandler.getLast(), Instant.now()).getSeconds();
            log.debug("Idle: {}", idle);
            if (idle > 0.0d) {
                double ratio = idle / result;
                log.debug("Ratio: {}", ratio);
                decay = Math.pow(2.0, -ratio);
            }
        }
        result *= decay;
        log.debug("Weighted average lag: ({}/{}) * {} = {}", value, weight, decay, result);
        return result;
    }

    public Throttle createThrottle() {
        return new Throttle(this, THROTTLE_BLOCK, THROTTLE_THRESHOLD) {
            @Override
            public void check() {
                getLag();
            }
        };
    }
}
