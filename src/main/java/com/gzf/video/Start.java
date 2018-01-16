/*
 * Copyright (c) 2017 Six Edge.
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

package com.gzf.video;

import com.gzf.video.core.server.HttpServer;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;

public class Start {
    private static final Logger logger = LoggerFactory.getLogger(Start.class);

    private static final HttpServer server = new HttpServer();

    public static void main(String[] args) {
        // start server
        try {
            server.startServer();
        } catch (Exception e) {
            logger.error("Server start failed", e);
        }
    }

    static {
        Signal signal = new Signal(PlatformDependent.isWindows() ? "INT" : "USR2");
        Signal.handle(signal,
                s ->  Runtime.getRuntime().addShutdownHook(
                    new Thread(server::closeServer, "Thread-ShutdownHook")
                )
        );
    }
}
