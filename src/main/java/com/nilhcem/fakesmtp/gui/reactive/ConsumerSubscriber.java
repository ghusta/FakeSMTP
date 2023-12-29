package com.nilhcem.fakesmtp.gui.reactive;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Executor;
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
    private Executor executor;

    public ConsumerSubscriber(Consumer<T> consumer) {
        Objects.requireNonNull(consumer);
        this.consumer = consumer;
    }

    public ConsumerSubscriber(Consumer<T> consumer, Executor executor) {
        this(consumer);
        this.executor = executor;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        if (executor != null) {
            executor.execute(() -> consumer.accept(item));
        } else {
            consumer.accept(item);
        }
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

    /**
     * Calls {@link Flow.Subscription#cancel()}.
     */
    public void cancelSubscription() {
        subscription.cancel();
    }

}
