package com.gzf.video.core.session;

import com.gzf.video.core.dao.redis.RedisProvider;
import redis.clients.jedis.Jedis;

public class RedisSession extends SessionManager {

    private final RedisProvider redisProvider = RedisProvider.getINSTANCE();
    private final String LOGIN_MAP = RedisProvider.LOGIN_MAP;

    @Override
    public void createLoginCache(final String sessionId, final String userId) {
        try (Jedis jedis = redisProvider.getJedis()) {
            jedis.hset(LOGIN_MAP, sessionId, userId);
        }
    }

    @Override
    public void destroyLoginCache(final String sessionId) {
        try (Jedis jedis = redisProvider.getJedis()) {
            jedis.hdel(LOGIN_MAP, sessionId);
        }
    }

    @Override
    public String getLoginUserIdCache(final String sessionId) {
        try (Jedis jedis = redisProvider.getJedis()) {
            return jedis.hget(LOGIN_MAP, sessionId);
        }
    }

    @Override
    public boolean containLoginCache(final String sessionId) {
        try (Jedis jedis = redisProvider.getJedis()) {
            return jedis.hexists(LOGIN_MAP, sessionId);
        }
    }
}
