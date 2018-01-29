package com.gzf.video.core.http.request;

/**
 * The request method that are supported.
 */
public enum HttpMethod {
    GET, POST;

    /**
     * Convert netty http-method to our own http-method.
     * @param method
     * @return
     */
    public static HttpMethod convert(io.netty.handler.codec.http.HttpMethod method) {
        if (method.equals(io.netty.handler.codec.http.HttpMethod.GET)) {
            return GET;
        } else if (method.equals(io.netty.handler.codec.http.HttpMethod.POST)) {
            return POST;
        } else {
            return null;
        }
    }
}
