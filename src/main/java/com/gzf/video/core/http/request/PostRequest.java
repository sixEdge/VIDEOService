package com.gzf.video.core.http.request;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class PostRequest extends Request {
    private static final Logger logger = LoggerFactory.getLogger(PostRequest.class);

    private Map<String, byte[]> fileContents;


    public PostRequest(final FullHttpRequest req, final Set<Cookie> cookies) {
        super(req, cookies);
        decode(req);
    }


    @Override
    public Map<String, String> parameters() {
        return parameters;
    }

    public Map<String, byte[]> fileContents() {
        return fileContents;
    }


    private void decode(final FullHttpRequest request) {
        HttpPostRequestDecoder postRequestDecoder = new HttpPostRequestDecoder(request);
        parameters = new HashMap<>();
        fileContents = new HashMap<>();

        try {
            for (InterfaceHttpData data : postRequestDecoder.getBodyHttpDatas()) {
                switch (data.getHttpDataType()) {
                case Attribute:
                    {
                        MixedAttribute attr = (MixedAttribute) data;
                        parameters.putIfAbsent(attr.getName(), attr.getValue());
                    }
                    break;
                case FileUpload:
                    {
                        MixedFileUpload fileUpload = (MixedFileUpload) data;
                        fileContents.putIfAbsent(fileUpload.getFilename(), fileUpload.get());
                    }
                    break;
                case InternalAttribute:
                    // noting to do at present
                    // continue
                }
            }
        } catch (IOException e) {
            logger.error("Post-request decode failed.", e);
        } finally {
            postRequestDecoder.destroy();
        }
    }
}
