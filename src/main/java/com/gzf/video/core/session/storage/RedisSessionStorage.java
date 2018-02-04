package com.gzf.video.core.session.storage;

import com.gzf.video.core.dao.redis.RedisProvider;
import redis.clients.jedis.Jedis;

public class RedisSessionStorage extends SessionStorage {

    private static final String LOGIN_MAP = "login";

    @Override
    public void createLoginCache(final String sessionId, final String userId) {
        try (Jedis jedis = RedisProvider.getJedis()) {
            jedis.hset(LOGIN_MAP, sessionId, userId);
        }
    }

    @Override
    public void destroyLoginCache(final String sessionId) {
        try (Jedis jedis = RedisProvider.getJedis()) {
            jedis.hdel(LOGIN_MAP, sessionId);
        }
    }

    @Override
    public String getLoginUserIdCache(final String sessionId) {
        try (Jedis jedis = RedisProvider.getJedis()) {
            return jedis.hget(LOGIN_MAP, sessionId);
        }
    }

    @Override
    public boolean containLoginCache(final String sessionId) {
        try (Jedis jedis = RedisProvider.getJedis()) {
            return jedis.hexists(LOGIN_MAP, sessionId);
        }
    }
}
