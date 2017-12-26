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
