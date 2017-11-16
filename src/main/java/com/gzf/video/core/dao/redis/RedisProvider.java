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




    private static final Config jedisPoolConfig =
            ConfigManager.loadConfigFromModule(daoModule, "redisConfig");

    public static final String LOGIN_MAP =
            StringUtil.notNullOrEmpty(jedisPoolConfig.getString("loginMap"));



    private final JedisPool jedisPool = getPool();

    private JedisPool getPool() {
            JedisPoolConfig config = new JedisPoolConfig();

            config.setBlockWhenExhausted(jedisPoolConfig.getBoolean("blockWhenExhausted"));
            config.setMaxTotal(jedisPoolConfig.getInt("maxTotal"));
            config.setMaxIdle(jedisPoolConfig.getInt("maxIdle"));
            config.setMinIdle(jedisPoolConfig.getInt("minIdle"));
            config.setMaxWaitMillis(jedisPoolConfig.getLong("maxWaitMillis"));

            return new JedisPool(config,
                    jedisPoolConfig.getString("host"),
                    jedisPoolConfig.getInt("port"));
    }

    public static RedisProvider getINSTANCE() {
        return INSTANCE;
    }


    private RedisProvider() {}
}
