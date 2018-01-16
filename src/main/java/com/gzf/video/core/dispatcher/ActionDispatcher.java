package com.gzf.video.core.dispatcher;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.bean.inject.AutomaticInjector;
import com.gzf.video.core.controller.ControllerScanner;
import com.gzf.video.core.controller.action.Action;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;

public class ActionDispatcher implements Dispatcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CONTROLLER_PACKAGE = ConfigManager.getControllerConf().getString("package");

    private final ActionMapper GET_MAPPER = new ActionMapper();
    private final ActionMapper POST_MAPPER = new ActionMapper();

    @Override
    public Action doDispatch(final String path, final HttpMethod method) {
        return chooseMapper(method).get(path);
    }

    @Override
    public void init() {
        try {
            List<Object> controllerList = ControllerScanner.scanControllers(this, CONTROLLER_PACKAGE);
            logger.info("{} controllers has benn scanned.", controllerList.size());

            // controller auto-inject
            controllerList.forEach(c -> AutomaticInjector.autoInject(c, c.getClass()));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Set action with corresponding path.
     *
     * @param method   GET or POST
     * @param path     the corresponding path maps to the action
     * @param action   the action
     */
    public void setAction(final HttpMethod method, final String path, final Action action) {
        try {
            chooseMapper(method).put(path, action);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private ActionMapper chooseMapper(final HttpMethod method) {
        if (GET.equals(method)) {
            return GET_MAPPER;
        } else if (POST.equals(method)) {
            return POST_MAPPER;
        } else {
            throw new RuntimeException("Request method not support: {}" + method);
        }
    }
}
