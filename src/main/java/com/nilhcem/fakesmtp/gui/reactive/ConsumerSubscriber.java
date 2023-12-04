package com.nilhcem.fakesmtp.gui.reactive;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * {@link java.util.concurrent.Flow.Subscriber Subscriber} using a {@link Consumer}.
 *
 * @param <T> Data type.
 */
@Slf4j
public class ConsumerSubscriber<T> implements Flow.Subscriber<T> {

    private Flow.Subscription subscription;
    private final Consumer<T> consumer;

    public ConsumerSubscriber(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        consumer.accept(item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(throwable.toString());
    }

    @Override
    public void onComplete() {
        // nothing to do
    }
}
