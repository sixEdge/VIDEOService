package com.gzf.video.core.http.request;

/**
 * Our own-http-method.
 */
public enum HttpMethod {
    GET, POST, UNSUPPORTED;

    /**
     * Convert netty-http-method to our own-http-method.
     * @param method request method declared in {@link io.netty.handler.codec.http.HttpMethod}
     * @return our own request method
     */
    public static HttpMethod convert(io.netty.handler.codec.http.HttpMethod method) {
        if (method.equals(io.netty.handler.codec.http.HttpMethod.GET)) {
            return GET;
        } else if (method.equals(io.netty.handler.codec.http.HttpMethod.POST)) {
            return POST;
        } else {
            return UNSUPPORTED;
        }
    }
}
