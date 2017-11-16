package com.gzf.video.core.async;

import com.gzf.video.core.ConfigManager;
import com.typesafe.config.Config;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class AsyncTask {

    private static final Config ASYNC_CONFIG =
            ConfigManager.loadConfigFromModule(ConfigManager.coreModule, "asyncConfig");
    private static final Config SERVICE_POOL_CONFIG = ASYNC_CONFIG.getConfig("servicePool");





    private static final ForkJoinPool SERVICE_POOL = new ForkJoinPool(
            SERVICE_POOL_CONFIG.getInt("parallelism"),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            SERVICE_POOL_CONFIG.getBoolean("asyncMode")
    );





    public static <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) {
        return SERVICE_POOL.invokeAll(tasks);
    }

    public static <T> T invoke(final ForkJoinTask<T> task) {
        return SERVICE_POOL.invoke(task);
    }

    public static void execute(final ForkJoinTask<?> task) {
        SERVICE_POOL.execute(task);
    }

    public static void execute(final Runnable task) {
        SERVICE_POOL.execute(task);
    }

    public static <T> ForkJoinTask<T> submit(final ForkJoinTask<T> task) {
        return SERVICE_POOL.submit(task);
    }

    public static <T> ForkJoinTask<T> submit(final Callable<T> task) {
        return SERVICE_POOL.submit(task);
    }

    public static <T> ForkJoinTask<T> submit(final Runnable task, final T result) {
        return SERVICE_POOL.submit(task, result);
    }
}
