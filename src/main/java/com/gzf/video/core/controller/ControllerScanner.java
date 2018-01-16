package com.gzf.video.core.controller;

import com.gzf.video.core.Scanner;
import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.controller.action.ActionGenerator;
import com.gzf.video.core.controller.action.method.Get;
import com.gzf.video.core.controller.action.method.Post;
import com.gzf.video.core.dispatcher.ActionDispatcher;
import com.gzf.video.core.dispatcher.ActionPathParser;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ControllerScanner extends Scanner {
    private final ActionDispatcher actionDispatcher;

    // will have more usage in future
    private final List<Object> controllers = new LinkedList<>();

    private ControllerScanner(final ActionDispatcher actionDispatcher) {
        this.actionDispatcher = actionDispatcher;
    }

    /**
     * Scan controllers and process actions.
     * @param actionDispatcher actionDispatcher
     */
    public static List<Object> scanControllers(final ActionDispatcher actionDispatcher, final String pkg)
            throws Exception {
        ControllerScanner scanner = new ControllerScanner(actionDispatcher);
        scanner.scan(pkg);
        return scanner.controllers;
    }

    @Override
    protected boolean isThis(final Class<?> clazz) {
        return !clazz.isInterface()
            && !Modifier.isAbstract(clazz.getModifiers())
            && clazz.getDeclaredAnnotation(Controller.class) != null;
    }

    @Override
    protected void process(final Class<?> clazz) {
        try {
            scanActions(clazz);
        } catch ( IllegalAccessException
                | InvocationTargetException
                | InstantiationException
                | NoSuchMethodException e) {
            logger.error("ControllerScanner", e);
        }
    }

    private void scanActions(final Class<?> controllerClass)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Method[] ms = controllerClass.getDeclaredMethods();

        if (ms.length < 1)
            return;

        Constructor constructor = controllerClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object controllerObj = constructor.newInstance();

        // add to controller list
        controllers.add(controllerObj);

        for (Method m : ms) {
            m.setAccessible(true);

            Get get;
            Post post;
            HttpMethod method;
            String url;

            if ((get = m.getDeclaredAnnotation(Get.class)) != null) {
                url = get.value();
                method = get.method;
            } else if ((post = m.getDeclaredAnnotation(Post.class)) != null) {
                url = post.value();
                method = post.method;
            } else {
                continue;
            }

            Action action = ActionGenerator.newAction(controllerClass, controllerObj, m);

            // corresponding action with request method and path
            actionDispatcher.setAction(method, ActionPathParser.parsePath(m, url), action);
        }
    }
}