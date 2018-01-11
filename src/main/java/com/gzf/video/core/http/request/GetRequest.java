package com.gzf.video.core.http.request;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;
import java.util.Set;

import static com.gzf.video.util.PathAndParametersUtil.decodeParams;
import static com.gzf.video.util.PathAndParametersUtil.findPathEndIndex;

public class GetRequest extends Request {

    public GetRequest(final FullHttpRequest req, final Set<Cookie> cookies) {
        super(req, cookies);
    }

    @Override
    public Map<String, String> parameters() {
        if (parameters == null) {
            parameters = decodeParams(uri, findPathEndIndex(uri));
        }
        return parameters;
    }
}
