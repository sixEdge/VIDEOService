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
import com.gzf.video.core.controller.action.RequestWrapper;
import com.gzf.video.core.dispatcher.DefaultDispatcher;
import com.gzf.video.core.dispatcher.Dispatcher;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.SessionManager;
import com.gzf.video.util.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

import static com.gzf.video.core.dispatcher.ActionDispatcher.PRE_INTERCEPT_PATH;
import static com.gzf.video.core.session.SessionManager.SESSION_ID;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class ActionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Dispatcher DISPATCHER = DefaultDispatcher.getINSTANCE();
    private static final SessionManager SESSION_MANAGER = SessionManager.getINSTANCE();

    public static void init() {}

    // not auto release
    public ActionHandler() {
        super(false);
    }




    private String sessionId;




    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        if (!req.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        boolean getOrPost;
        if (req.method() == GET) getOrPost = true;
        else if (req.method() == POST) getOrPost = false;
        else {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }


        String cookieSessionId = null;
        Set<Cookie> cookies = null;
        Session session = null;


        // sync session id

        if (sessionId == null) {
            cookies = StringUtil.decodeCookies(req.headers().get(COOKIE));
            cookieSessionId = StringUtil.getFromCookies(cookies, SESSION_ID);

            String userId;
            if (cookieSessionId == null

                    // has login
                    || SESSION_MANAGER.getSession(cookieSessionId, false) != null

                    // remembered-user
                    || (userId = SESSION_MANAGER.getLoginUserIdCache(cookieSessionId)) == null) {
                sessionId = UUID.randomUUID().toString();
            } else {
                // auto login
                sessionId = cookieSessionId;
                session = SESSION_MANAGER.createSession(sessionId, userId);
            }
        }


        // intercept

        String uri = req.uri();

        if (uri.startsWith(PRE_INTERCEPT_PATH)) {
            // haven't been resolved (is not the first request)
            if (cookies == null) {
                cookies = StringUtil.decodeCookies(req.headers().get(COOKIE));
                cookieSessionId = StringUtil.getFromCookies(cookies, SESSION_ID);
            }

            if (cookieSessionId == null
                    || (session = UserInterceptor.doIntercept(cookieSessionId)) == null) {
                sendError(ctx, FORBIDDEN);
                return;
            }
        }


        // dispatch

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);

        Action action = DISPATCHER.doDispatch(queryStringDecoder.path(), null, getOrPost);
        if (action == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }


        // action

        FullHttpResponse response =
                action.doAction(
                        new RequestWrapper(
                                ctx, queryStringDecoder, req,
                                cookies, session, sessionId));


        // send

        if (response == null) {
            return;
        }
        if (HttpUtil.isKeepAlive(req)) {
            response.headers().add(CONNECTION, KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        if (sessionId != null) {
            SESSION_MANAGER.destroySession(sessionId);
        }
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        logger.error("exceptionCaught", cause);
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
}

