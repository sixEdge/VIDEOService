package com.gzf.video.core.dao.redis;

import com.gzf.video.core.ConfigManager;
import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import sun.misc.Cleaner;

import static com.gzf.video.core.ConfigManager.coreModule;

public class RedisProvider {

    private static final Config REDIS_CONFIG = ConfigManager.loadConfigFromModule(coreModule, "redisConfig");


    /**
     * <em>NOTE: Close resource when not use.</em>
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }


    private static final JedisPool jedisPool = getPool();

    private static JedisPool getPool() {
            JedisPoolConfig config = new JedisPoolConfig();

            config.setBlockWhenExhausted(REDIS_CONFIG.getBoolean("blockWhenExhausted"));
            config.setMaxTotal(REDIS_CONFIG.getInt("maxTotal"));
            config.setMaxIdle(REDIS_CONFIG.getInt("maxIdle"));
            config.setMinIdle(REDIS_CONFIG.getInt("minIdle"));
            config.setMaxWaitMillis(REDIS_CONFIG.getLong("maxWaitMillis"));

            return new JedisPool(config,
                    REDIS_CONFIG.getString("host"),
                    REDIS_CONFIG.getInt("port"));
    }

    static {
        Cleaner.create(jedisPool, jedisPool::close);
    }

    private RedisProvider() {}
}
