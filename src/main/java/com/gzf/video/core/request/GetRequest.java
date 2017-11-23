package com.gzf.video.core.request;

import com.gzf.video.core.session.Session;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;
import java.util.Set;

import static com.gzf.video.core.request.PathAndParametersUtil.decodeParams;
import static com.gzf.video.core.request.PathAndParametersUtil.findPathEndIndex;

public class GetRequest extends Request {

    private String uri;

    public GetRequest(final ChannelHandlerContext ctx,
                      final FullHttpRequest req,
                      final Set<Cookie> cookies,
                      final Session session) {
        super(ctx, req.headers(), cookies, session);
        uri = req.uri();
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> parameters() {
        if (parameters == null) {
            parameters = decodeParams(uri, findPathEndIndex(uri));
        }
        return parameters;
    }
}
