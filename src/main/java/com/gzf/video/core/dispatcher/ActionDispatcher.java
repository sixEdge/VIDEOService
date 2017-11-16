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

import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.ConfigManager;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.gzf.video.core.dispatcher.CustomParametersParser.CustomParameter;

/**
 * Intercept, then dispatch.
 */
public class ActionDispatcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Config dispatcherConfig = ConfigManager.getDispatcherConf();
    public static final boolean notUseCustomParameter =
            dispatcherConfig.getBoolean("notUseCustomParameter");
    public static final String PRE_INTERCEPT_PATH = dispatcherConfig.getString("preInterceptPath");


    private final ActionMapper GET_MAPPER = new ActionMapper(notUseCustomParameter);
    private final ActionMapper POST_MAPPER = new ActionMapper(notUseCustomParameter);




    public Action getAction(final String path,
                            final Map<String, List<String>> parameters,
                            final boolean get_or_post) {
        return (get_or_post ? GET_MAPPER : POST_MAPPER).get(path, parameters);
    }


    /**
     * Set action with specify {@link CustomParameter}.
     *
     * @param cp    the {@link CustomParameter} map to the action
     * @param action the action
     * @param get_or_post true for GET, false for POST
     */
    public void setAction(final CustomParameter cp, final Action action, final boolean get_or_post) {
        setAction0(cp, action, get_or_post ? GET_MAPPER : POST_MAPPER);
    }

    private void setAction0(final CustomParameter cp, final Action action, final ActionMapper actionMapper) {
        if (actionMapper.isConflicting(cp))
            logger.warn("Conflicting action, " +
                            "two action are both mapped to the same CustomParameter {}.", cp);

        actionMapper.put(cp, action);
    }


    ActionDispatcher() {}
}
