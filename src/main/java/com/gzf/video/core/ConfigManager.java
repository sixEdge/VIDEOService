package com.gzf.video.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigManager {

    /** Application config */
    private static final Config appConf = ConfigFactory.defaultApplication();

    public static final String APP_NAME = appConf.getString("appName");


    public static final Config coreModule =
            ConfigFactory.load(appConf.getConfig("core").getString("configModule"));

    public static final Config daoModule =
            ConfigFactory.load(appConf.getConfig("dao").getString("configModule"));

    public static final Config serviceModule =
            ConfigFactory.load(appConf.getConfig("service").getString("configModule"));


    private static final Config controllerConf = appConf.getConfig("controller");

    private static final Config serverConf = appConf.getConfig("server");

    private static final Config interceptorConf = appConf.getConfig("interceptor");


    public static Config getControllerConf() {
        return controllerConf;
    }

    public static Config getServerConf() {
        return serverConf;
    }

    public static Config getInterceptorConf() {
        return interceptorConf;
    }


    public static Config loadConfigFromModule(final Config moduleConfig, final String childConfigPath) {
        return ConfigFactory.load(moduleConfig.getString(childConfigPath));
    }


    private ConfigManager() {}
}
