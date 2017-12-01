package com.gzf.video.core.http.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Response extends DefaultFullHttpResponse {

    public Response(final HttpResponseStatus status) {
        super(HTTP_1_1, status, Unpooled.EMPTY_BUFFER);
    }

    public Response(final HttpResponseStatus status, final ByteBuf content) {
        super(HTTP_1_1, status, content);
    }

    public Response(final HttpResponseStatus status, final ByteBuf content, final HttpHeaders headers, final HttpHeaders trailingHeaders) {
        super(HTTP_1_1, status, content, headers, trailingHeaders);
    }
}
