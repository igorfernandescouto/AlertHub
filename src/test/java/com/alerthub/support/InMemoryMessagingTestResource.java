package com.alerthub.support;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

public class InMemoryMessagingTestResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> properties = new HashMap<>();
        properties.putAll(InMemoryConnector.switchIncomingChannelsToInMemory("events-in"));
        properties.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory("events-out"));
        return properties;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
