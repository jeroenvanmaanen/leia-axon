package org.leialearns.axon.unique.process;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.unique.command.AddUniqueKeyCommand;
import org.leialearns.axon.unique.command.CleanExistingKeysCommand;
import org.leialearns.axon.unique.command.CreateUniqueBucketCommand;
import org.leialearns.axon.unique.command.UniqueBucketLogStatisticsCommand;
import org.leialearns.axon.unique.query.UniqueByHashQuery;
import org.leialearns.axon.unique.query.UniqueRootExists;
import org.leialearns.model.UniqueBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class UniqueKeyService {
    public static final String UNIQUE_ROOT_ID = "unique-root";
    private final StackCommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final HashingMethod hashingMethod;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    @Autowired
    public UniqueKeyService(StackCommandGateway commandGateway, QueryGateway queryGateway, HashingMethod hashingMethod) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.hashingMethod = hashingMethod;
    }

    public void assertUnique(Object domain, String key) {
        if (!isInitialized.getAndSet(true)) {
            initialize();
        }
        String hash = hashingMethod.createHash(domain, key);
        log.trace("Assert unique: {}: {}: {}", domain, key, hash);
        String bucketId;
        try {
            bucketId = queryGateway.query(UniqueByHashQuery.builder().hashCode(hash).build(), String.class).get();
        } catch (Exception e) {
            log.warn("Exception while getting bucket for hash: {}", hash, e);
            bucketId = UNIQUE_ROOT_ID;
        }
        String result = AddUniqueKeyCommand.builder()
            .id(bucketId)
            .domain(domain)
            .key(key)
            .hash(hash)
            .build()
            .sendAndWait(commandGateway);
        if (StringUtils.isEmpty(result)) {
            log.debug("Key already exists: {}: {}", domain, key);
            throw new IllegalStateException("Key already exists: " + domain + ": " + key);
        }
    }

    private void initialize() {
        boolean rootExists;
        try {
            rootExists = queryGateway.query(new UniqueRootExists(), Boolean.class).get();
        } catch (Exception exception) {
            rootExists = false;
        }
        if (rootExists) {
            log.info("Root bucket exists");
        } else {
            try {
                log.info("Initializing: {}", UNIQUE_ROOT_ID);
                CreateUniqueBucketCommand.builder()
                    .id(UNIQUE_ROOT_ID)
                    .maxKeys(100)
                    .childKeyPrefixLength(2)
                    .build()
                    .sendAndWait(commandGateway);
            } catch (Exception ignore) {
                log.info("Unique bucket root already exists");
            }
        }
        UniqueBucketLogStatisticsCommand.builder().id(UNIQUE_ROOT_ID).build().sendAndWait(commandGateway);
    }

    public void cleanUniqueKeys() {
        CleanExistingKeysCommand.builder().id(UNIQUE_ROOT_ID).fullPrefix("").build().sendAndWait(commandGateway);
    }

    public Collection<UniqueBucket> describeUniqueBuckets() {
        return Collections.emptyList();
    }
}
