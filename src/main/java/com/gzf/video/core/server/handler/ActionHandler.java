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
package com.gzf.video.core.server.handler;

import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.dispatcher.DefaultDispatcher;
import com.gzf.video.core.dispatcher.Dispatcher;
import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gzf.video.core.http.request.PathAndParametersUtil.decodeComponent;
import static com.gzf.video.core.http.request.PathAndParametersUtil.findPathEndIndex;
import static com.gzf.video.util.ControllerFunctions.encodeCookie;
import static io.netty.channel.ChannelHandler.Sharable;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

@Sharable
public class ActionHandler extends SimpleChannelInboundHandler<Request> {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final Dispatcher DISPATCHER = DefaultDispatcher.getINSTANCE();


    public static void init() {}

    // auto release
    private ActionHandler() {
        super(false);
    }


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Request req) {

        boolean getOrPost;
        if (req.getMethod() == GET) getOrPost = true;
        else if (req.getMethod() == POST) getOrPost = false;
        else {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }


        String uri = req.getUri();



        // dispatch

        String path = decodeComponent(uri, 0, findPathEndIndex(uri));

        Action action = DISPATCHER.doDispatch(path, null, getOrPost);
        if (action == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }



        // do action

        Response response = action.doAction(req);



        // send

        if (response == null) {
            return;
        }

        if (req.isNewSessionId()) {
            req.getHeaders().add(SET_COOKIE,
                    encodeCookie(Request.cookieSessionId(req.sessionId())));
        }

        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(req.getHeader(CONNECTION))) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush(response);
        }
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        logger.error("Action-Handler exceptionCaught", cause);
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
        ctx.close();
    }


    private static void sendError(final ChannelHandlerContext ctx,
                                  final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    private static final ActionHandler INSTANCE = new ActionHandler();

    public static ActionHandler getINSTANCE() {
        return INSTANCE;
    }
}
