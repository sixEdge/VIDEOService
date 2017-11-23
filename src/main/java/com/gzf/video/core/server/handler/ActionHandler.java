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
import com.gzf.video.core.request.GetRequest;
import com.gzf.video.core.dispatcher.DefaultDispatcher;
import com.gzf.video.core.dispatcher.Dispatcher;
import com.gzf.video.core.request.PostRequest;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.SessionStorage;
import com.gzf.video.util.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.gzf.video.core.dispatcher.ActionDispatcher.PRE_INTERCEPT_PATH;
import static com.gzf.video.core.request.PathAndParametersUtil.decodeComponent;
import static com.gzf.video.core.request.PathAndParametersUtil.findPathEndIndex;
import static com.gzf.video.core.session.SessionStorage.SESSION_ID;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class ActionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Dispatcher DISPATCHER = DefaultDispatcher.getINSTANCE();
    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    public static void init() {}

    // auto release
    public ActionHandler() {
        super(true);
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
            if (cookieSessionId != null

                    // this sessionId has not been used by other client
                    && SESSION_STORAGE.getSession(cookieSessionId, false) == null

                    // a user has been remembered
                    && (userId = SESSION_STORAGE.getLoginUserIdCache(cookieSessionId)) != null) {

                // auto login
                sessionId = cookieSessionId;
                session = SESSION_STORAGE.createSession(sessionId, userId);
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

                    // find session
                    // if session is null, then intercept request and send a forbidden response
                    || (session = UserInterceptor.doIntercept(cookieSessionId)) == null) {
                sendError(ctx, FORBIDDEN);
                return;
            }
        }


        // dispatch

        String path = decodeComponent(uri, 0, findPathEndIndex(uri));

        Action action = DISPATCHER.doDispatch(path, null, getOrPost);
        if (action == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }


        // action

        FullHttpResponse response = action.doAction(
                getOrPost
                        ? new GetRequest(ctx, req, cookies, session)
                        : new PostRequest(ctx, req, cookies, session)
        );


        // send

        if (response == null) {
            return;
        }

        if (HttpUtil.isKeepAlive(req)) {
            ctx.writeAndFlush(response);
        } else {
            response.headers().set(CONNECTION, CLOSE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        if (sessionId != null) {
            SESSION_STORAGE.destroySession(sessionId);
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


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }


    private static void sendError(final ChannelHandlerContext ctx,
                                  final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}

