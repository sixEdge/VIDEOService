package com.gzf.video.core.server.handler;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.dispatcher.DefaultDispatcher;
import com.gzf.video.core.dispatcher.Dispatcher;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.request.GetRequest;
import com.gzf.video.core.http.request.HttpMethod;
import com.gzf.video.core.http.request.PostRequest;
import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
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

import java.util.List;
import java.util.Set;

import static com.gzf.video.core.http.request.HttpMethod.GET;
import static com.gzf.video.core.http.request.HttpMethod.UNSUPPORTED;
import static com.gzf.video.core.tool.PathAndParametersUtil.decodeComponent;
import static com.gzf.video.core.tool.PathAndParametersUtil.findPathEndIndex;
import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID;
import static com.gzf.video.core.tool.CookieFunctions.decodeCookies;
import static com.gzf.video.core.tool.CookieFunctions.getFromCookies;
import static io.netty.channel.ChannelHandler.Sharable;
import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final List<String> PRE_INTERCEPT_PATH = ConfigManager.getInterceptorConf().getStringList("preInterceptPaths");


    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();


    private static final Dispatcher DISPATCHER = new DefaultDispatcher();


    public static void init() {
        DISPATCHER.init();
    }


    // auto release
    private HttpServerHandler() {
        super(true);
    }


    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest req) {

        if (!req.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        HttpMethod method = HttpMethod.convert(req.method());
        if (method == UNSUPPORTED) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }


        String cookieSessionId;
        Set<Cookie> cookies = null;
        Session session = null;


        // intercept

        String uri = req.uri();

        if (PRE_INTERCEPT_PATH.stream().anyMatch(uri::startsWith)) {

            // sync session id
            cookies = decodeCookies(req.headers().get(COOKIE));
            cookieSessionId = getFromCookies(cookies, SESSION_ID);

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

            if (session == null || session.getUserId() == null) {
                sendError(ctx, FORBIDDEN);
                return;
            }
        }


        // dispatch

        String path = decodeComponent(uri, 0, findPathEndIndex(uri));

        Action action = DISPATCHER.doDispatch(path, method);
        if (action == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }


        // construct request & http-exchange

        HttpExchange ex = new HttpExchange(ctx, constructRequest(method, req, cookies), session);


        // do action

        Response response = action.doAction(ex);


        // send if need

        if (response != null) {
            ex.writeResponse(response);
        }
    }


    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        logger.error("Dispatcher-Handler exceptionCaught", cause);
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
        ctx.close();
    }


    private static void sendError(final ChannelHandlerContext ctx, final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static Request constructRequest(final HttpMethod method,
                                            final FullHttpRequest req,
                                            final Set<Cookie> cookies) {
        return method == GET
                ? new GetRequest(req, cookies)
                : new PostRequest(req, cookies);
    }

    private static final HttpServerHandler INSTANCE = new HttpServerHandler();

    public static HttpServerHandler getINSTANCE() {
        return INSTANCE;
    }
}
