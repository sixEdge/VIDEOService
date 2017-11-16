package com.gzf.video.core.dao.redis;

import redis.clients.jedis.Jedis;

import java.util.concurrent.CompletableFuture;

/**
 * No need to be async.
 *
 * @param <R> result
 */
@Deprecated
@FunctionalInterface
public interface JedisCallback<R> {
    CompletableFuture<R> execute(Jedis jedis);
}
