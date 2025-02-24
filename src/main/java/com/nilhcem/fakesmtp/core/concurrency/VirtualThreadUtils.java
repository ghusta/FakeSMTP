package com.nilhcem.fakesmtp.core.concurrency;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NullMarked
public class VirtualThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadUtils.class);

    private static final @Nullable ExecutorService VIRTUAL_THREAD_EXECUTOR = createVirtualThreadExecutor();

    private VirtualThreadUtils() {
    }

    /**
     * Return a virtual thread executor if available.
     *
     * @return Virtual thread executor.
     */
    public static Optional<ExecutorService> getVirtualThreadExecutor() {
        return Optional.ofNullable(VIRTUAL_THREAD_EXECUTOR);
    }

    private static @Nullable ExecutorService createVirtualThreadExecutor() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle mh = lookup.findStatic(
                    Executors.class,
                    "newVirtualThreadPerTaskExecutor",
                    MethodType.methodType(ExecutorService.class)
            );
            log.info("Virtual Threads (JEP 444) available");
            return (ExecutorService) mh.invoke();
        } catch (Throwable e) {
            return null;
        }
    }

    private static ExecutorService createFixedThreadPool() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

}
