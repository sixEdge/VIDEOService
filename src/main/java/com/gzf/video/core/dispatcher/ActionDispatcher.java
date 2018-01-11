/*
 * Copyright (c) 2017  Six Edge.
 *
 * This Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.ControllerScan;
import com.gzf.video.core.controller.action.Action;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;

/**
 * Dispatch.
 */
public class ActionDispatcher implements Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ActionDispatcher.class);

    private final ActionMapper GET_MAPPER = new ActionMapper();
    private final ActionMapper POST_MAPPER = new ActionMapper();


    @Override
    public Action doDispatch(final String path, final boolean get_or_post) {
        return (get_or_post ? GET_MAPPER : POST_MAPPER).get(path);
    }

    @Override
    public void init() {
        try {
            new ControllerScan(INSTANCE).refresh();
        } catch (Exception e) {
            throw new Error(e);
        }
    }


    /**
     * Set action with corresponding path.
     *
     * @param path     the corresponding path maps to the action
     * @param action   the action
     * @param method   GET or POST
     */
    public void setAction(final String path, final Action action, final HttpMethod method) {
        ActionMapper mapper;
        try {
            mapper = chooseMapper(method);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }

        if (mapper.isConflicting(path))
            logger.warn("Conflicting action, " +
                    "two actions are both mapped to the same path {}.", path);

        mapper.put(path, action);
    }

    private ActionMapper chooseMapper(final HttpMethod method) throws Exception {
        ActionMapper mapper;

        if (GET.equals(method)) {
            mapper = GET_MAPPER;
        } else if (POST.equals(method)) {
            mapper = POST_MAPPER;
        } else {
            throw new Exception("Request method not support: {}" + method);
        }

        return mapper;
    }


    private static final ActionDispatcher INSTANCE = new ActionDispatcher();

    public static ActionDispatcher getINSTANCE() {
        return INSTANCE;
    }

    private ActionDispatcher() {}
}
