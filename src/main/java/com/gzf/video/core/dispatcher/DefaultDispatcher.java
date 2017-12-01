/*
 * Copyright (c) 2017 Six Edge.
 *
 * This Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.controller.ControllerScan;

import java.util.Map;

/**
 * Dispatch the specific {@link Action} according to request path.
 */
public class DefaultDispatcher implements Dispatcher {

    private final ActionDispatcher actionDispatcher = new ActionDispatcher();

    private static final DefaultDispatcher INSTANCE = new DefaultDispatcher();

    public static DefaultDispatcher getINSTANCE() {
        return INSTANCE;
    }


    private Action doDispatch(final String path, final boolean get_or_post) {
        return actionDispatcher.getAction(path, null, get_or_post);
    }

    @Override
    @Deprecated
    public Action doDispatch(final String path, final Map<String, String> parameters, final boolean get_or_post) {
        return this.doDispatch(path, get_or_post);
    }


    private DefaultDispatcher() {
        try {
            new ControllerScan(actionDispatcher).refresh();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
