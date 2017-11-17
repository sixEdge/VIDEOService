package com.gzf.video.core.dao.redis;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.util.StringUtil;
import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static com.gzf.video.core.ConfigManager.daoModule;

public class RedisProvider {

    private static final RedisProvider INSTANCE = new RedisProvider();




    /**
     * <em>NOTE: Close resource when not use.</em>
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }




    private static final Config REDIS_CONFIG =
            ConfigManager.loadConfigFromModule(daoModule, "redisConfig");

    public static final String LOGIN_MAP =
            StringUtil.notNullOrEmpty(REDIS_CONFIG.getString("loginMap"));



    private final JedisPool jedisPool = getPool();

    private JedisPool getPool() {
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

    public static RedisProvider getINSTANCE() {
        return INSTANCE;
    }


    private RedisProvider() {}
}
