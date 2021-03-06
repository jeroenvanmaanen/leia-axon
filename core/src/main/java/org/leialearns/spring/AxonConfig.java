package org.leialearns.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mongodb.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGatewayFactory;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.*;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.annotation.AnnotationCommandHandlerBeanPostProcessor;
import org.leialearns.axon.StackCommandGateway;
import org.leialearns.axon.model.node.process.ModelNodeEventHandler;
import org.leialearns.axon.once.TriggerCommandOnceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class AxonConfig {
    private static final String TRACKING_TOKENS_COLLECTION = "axon-tracking-tokens";
    private static final String SAGAS_COLLECTION = "axon-sagas";

    @Bean
    @Primary
    public ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        configurePOJOBuilder(objectMapper);
        log.info("Default object mapper: {}", objectMapper);
        return objectMapper;
    }

    @Bean
    public ObjectMapper yamlObjectMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
        configurePOJOBuilder(objectMapper);
        log.info("YAML object mapper: {}", objectMapper);
        return objectMapper;
    }

    private void configurePOJOBuilder(ObjectMapper objectMapper) {
        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public JsonPOJOBuilder.Value findPOJOBuilderConfig(AnnotatedClass annotatedClass) {
                if (annotatedClass.hasAnnotation(JsonPOJOBuilder.class)) {
                    return super.findPOJOBuilderConfig(annotatedClass);
                }
                // If no annotation present use default as empty prefix
                return new JsonPOJOBuilder.Value("build", "");
            }
        });
    }

    @Bean
    public Serializer eventSerializer(ObjectMapper objectMapper) {
        return JacksonSerializer.builder().objectMapper(objectMapper).build();
    }

    @Bean
    public MongoTemplate axonMongoTemplate(MongoClient mongoClient) {
        return DefaultMongoTemplate.builder()
            .mongoDatabase(mongoClient)
            .trackingTokensCollectionName(TRACKING_TOKENS_COLLECTION)
            .sagasCollectionName(SAGAS_COLLECTION)
            .build();
    }

    @Bean
    public TokenStore tokenStore(MongoTemplate mongoTemplate, Serializer serializer) {
        return MongoTokenStore.builder()
            .mongoTemplate(mongoTemplate)
            .serializer(serializer)
            .build();
    }

    @Bean
    public CommandGatewayFactory commandGatewayFactory(CommandBus commandBus) {
        return CommandGatewayFactory.builder()
            .commandBus(commandBus)
            .build();
    }

    @Bean
    public StackCommandGateway stackCommandGateway(CommandGatewayFactory commandGatewayFactory) {
        return commandGatewayFactory.createGateway(StackCommandGateway.class);
    }

    @Bean
    public AnnotationCommandHandlerBeanPostProcessor annotationCommandHandlerBeanPostProcessor() {
        return new AnnotationCommandHandlerBeanPostProcessor();
    }

    @Bean
    public TriggerCommandOnceService onceService(AggregateLifecycleBean aggregateLifecycle) {
        return new TriggerCommandOnceService(aggregateLifecycle);
    }

    @Autowired
    public void configure(EventProcessingConfigurer configurator, TokenStore tokenStore, TransactionManager transactionManager) {
        TrackingEventProcessorConfiguration trackerConfiguration =
            TrackingEventProcessorConfiguration.forParallelProcessing(4).andBatchSize(1000);
        EventProcessingConfigurer.EventProcessorBuilder eventProcessorBuilder =
            createEventProcessorBuilder(trackerConfiguration, tokenStore, transactionManager);
        configurator.registerEventProcessor(ModelNodeEventHandler.PROCESSOR_NAME, eventProcessorBuilder);
    }

    protected EventProcessingConfigurer.EventProcessorBuilder createEventProcessorBuilder(
        TrackingEventProcessorConfiguration trackerConfiguration, TokenStore tokenStore,
        TransactionManager transactionManager
    ) {
        return new StackEventProcessorBuilder(trackerConfiguration, tokenStore, transactionManager);
    }

    protected static class StackEventProcessorBuilder implements EventProcessingConfigurer.EventProcessorBuilder {
        private final TrackingEventProcessorConfiguration trackerConfiguration;
        private final TokenStore tokenStore;
        private final TransactionManager transactionManager;

        protected StackEventProcessorBuilder(
            TrackingEventProcessorConfiguration trackerConfiguration, TokenStore tokenStore,
            TransactionManager transactionManager
        ) {
            this.trackerConfiguration = trackerConfiguration;
            this.tokenStore = tokenStore;
            this.transactionManager = transactionManager;
        }

        @Override
        public EventProcessor build(String name, org.axonframework.config.Configuration configuration, EventHandlerInvoker invoker) {
            TrackingEventProcessor result = TrackingEventProcessor.builder()
                .name("STACK/" + name)
                .eventHandlerInvoker(invoker)
                .messageSource(configuration.eventStore())
                .tokenStore(tokenStore)
                .trackingEventProcessorConfiguration(trackerConfiguration)
                .transactionManager(transactionManager)
                .build();
            log.debug("Tracking event processor: {}: {}", name, result);
            return result;
        }
    }
}
