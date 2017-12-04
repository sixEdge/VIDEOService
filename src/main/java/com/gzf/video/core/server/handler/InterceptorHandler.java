package com.gzf.video.core.server.handler;

import com.gzf.video.core.http.request.GetRequest;
import com.gzf.video.core.http.request.PostRequest;
import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.gzf.video.util.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.gzf.video.core.dispatcher.ActionDispatcher.PRE_INTERCEPT_PATH;
import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID;
import static io.netty.channel.ChannelHandler.Sharable;
import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Sharable
public class InterceptorHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();


    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) {

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


        String cookieSessionId;
        Set<Cookie> cookies;
        Session session = null;


        // sync session id

        cookies = StringUtil.decodeCookies(req.headers().get(COOKIE));
        cookieSessionId = StringUtil.getFromCookies(cookies, SESSION_ID);

        String userId;
        if (StringUtil.isNotNullOrEmpty(cookieSessionId)) {

            // a sessionId has been used by current client
            if ((session = SESSION_STORAGE.getSession(cookieSessionId, false)) == null) {

                // remembered user
                if ((userId = SESSION_STORAGE.getLoginUserIdCache(cookieSessionId)) != null) {

                    // auto login
                    session = SESSION_STORAGE.createSession(cookieSessionId);
                    session.setUserId(userId);
                }
            }
        }


        // intercept

        String uri = req.uri();

        if (uri.startsWith(PRE_INTERCEPT_PATH)) {
            if (session == null || session.getUserId() == null) {
                sendError(ctx, FORBIDDEN);
                return;
            }
        }


        // construct request

        Request request = getOrPost
                ? new GetRequest(ctx, req, cookies, session)
                : new PostRequest(ctx, req, cookies, session);


        ctx.fireChannelRead(request);
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        logger.error("Interceptor-Handler exceptionCaught", cause);
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


    private static final InterceptorHandler INSTANCE = new InterceptorHandler();

    public static InterceptorHandler getINSTANCE() {
        return INSTANCE;
    }

    private InterceptorHandler() {}
}
