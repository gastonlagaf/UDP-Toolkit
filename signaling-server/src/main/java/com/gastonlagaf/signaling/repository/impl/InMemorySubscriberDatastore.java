package com.gastonlagaf.signaling.repository.impl;

import com.gastonlagaf.signaling.model.SignalingSubscriber;
import com.gastonlagaf.signaling.repository.SubscriberDatastore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;

public class InMemorySubscriberDatastore implements SubscriberDatastore {

    private final Cache<String, SignalingSubscriber> store = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .build();

    @Override
    public Optional<SignalingSubscriber> findById(String id) {
        return Optional.ofNullable(store.getIfPresent(id));
    }

    @Override
    public SignalingSubscriber save(SignalingSubscriber subscriber) {
        store.put(subscriber.getId(), subscriber);
        return subscriber;
    }

    @Override
    public Optional<SignalingSubscriber> remove(SignalingSubscriber subscriber) {
        store.invalidate(subscriber.getId());
        return Optional.of(subscriber);
    }

}
