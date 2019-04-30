package org.leialearns.axon.lag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class LagService {
    private static final double DECAY = 1.0;
    private static final double DECAY_PER_MINUTE = 1.0 + DECAY; // relevance per minute
    private static final double DECAY_PER_SECOND = Math.pow(DECAY_PER_MINUTE, 1.0/60);
    private static final int SECONDS_PER_MINUTE = 60;
    private static final long THROTTLE_BLOCK = 100;
    private static final double THROTTLE_THRESHOLD = 2.0;

    private final LagRepository repository;
    private final MongoTemplate mongoTemplate;

    public LagService(LagRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
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
        Iterable<LagDocument> lagDocuments = repository.findTop10ByOrderByMinuteDescKeyAsc();
        Long reference = null;
        double weight = 0.0d;
        double value = 0.0d;
        for (LagDocument lagDocument : lagDocuments) {
            if (reference == null) {
                reference = lagDocument.getMinute();
            }
            double factor = Math.pow(DECAY_PER_MINUTE, lagDocument.getMinute() - reference);
            log.debug("Current lag: {}: {}: {}", reference, factor, lagDocument);
            weight += factor * lagDocument.getWeight();
            value += factor * lagDocument.getValue();
        }
        double result = value / weight;
        log.debug("Weighted average lag: {}/{} = {}", value, weight, result);
        return result;
    }

    public Throttle createThrottle() {
        return new Throttle(this, THROTTLE_BLOCK, THROTTLE_THRESHOLD);
    }
}
