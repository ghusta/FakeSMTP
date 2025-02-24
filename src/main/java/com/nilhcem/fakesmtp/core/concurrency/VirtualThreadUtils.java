package com.nilhcem.fakesmtp.core.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class VirtualThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadUtils.class);

    private static final Optional<ExecutorService> EXECUTOR = createVirtualThreadExecutor();

    public static ExecutorService getExecutor(Supplier<ExecutorService> fallbackExecutorService) {
        return EXECUTOR.orElseGet(fallbackExecutorService);
    }

    private static Optional<ExecutorService> createVirtualThreadExecutor() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle mh = lookup.findStatic(
                    Executors.class,
                    "newVirtualThreadPerTaskExecutor",
                    MethodType.methodType(ExecutorService.class)
            );
            log.info("Virtual Threads (JEP 444) available");
            return Optional.of((ExecutorService) mh.invoke());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    private static ExecutorService createFixedThreadPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

}
