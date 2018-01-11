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
package com.gzf.video.core.server;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.ProjectDependent;
import com.typesafe.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_REUSEADDR;

public class HttpServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Config serverConf = ConfigManager.getServerConf();

    private int port = serverConf.getInt("port");
    private int sslPort = serverConf.getInt("sslPort");

    private boolean SSL;
    private int PORT;

    private Channel bindChannel;

    public HttpServer() {
        SSL = System.getProperty("ssl") != null;
        PORT = Integer.parseInt(System.getProperty("port", "" + (SSL ? sslPort : port)));
    }

    public void startServer() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = ProjectDependent.newEventLoopGroup(serverConf.getInt("bossGroupSize"));
        EventLoopGroup workerGroup = ProjectDependent.newEventLoopGroup(serverConf.getInt("workerGroupSize"));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .option(SO_BACKLOG, 1024)
                    .option(SO_REUSEADDR, true)
                    .childOption(SO_REUSEADDR, true)
                    .channel(ProjectDependent.serverSocketChannelClass())
//                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new HttpServerInitializer(sslCtx));

            ChannelFuture f = b.bind(PORT).sync();

            logger.info("Server start at " +
                    "http" + (SSL ? "s" : "") + "://localhost:" + (SSL ? sslPort : port));

            bindChannel = f.channel();
            bindChannel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("Server shutdown gracefully.");
        }
    }

    public ChannelFuture closeServer() {
        try {
            return bindChannel.close().sync();
        } catch (InterruptedException e) {
            logger.error("Server shutdown interrupted.", e);
        }
        return null;
    }
}
