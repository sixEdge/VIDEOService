package com.gzf.video.core.request;

import com.gzf.video.core.session.Session;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class PostRequest extends Request {
    private static final Logger logger = LoggerFactory.getLogger(PostRequest.class);

    private HttpPostRequestDecoder postRequestDecoder;

    private Map<String, byte[]> fileContents;

    public PostRequest(final ChannelHandlerContext ctx,
                       final FullHttpRequest req,
                       final Set<Cookie> cookies,
                       final Session session) {
        super(ctx, req.headers(), cookies, session);
        checkAndDecode(req);
    }


    @Override
    public void release() {
        if (postRequestDecoder != null) {
            postRequestDecoder.destroy();
        }
    }

    @Override
    public Map<String, String> parameters() {
        return parameters;
    }

    public Map<String, byte[]> fileContents() {
        return fileContents;
    }


    private void checkAndDecode(final FullHttpRequest request) {
        postRequestDecoder = new HttpPostRequestDecoder(request);
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
                    // continue
                    // noting to do at present
                }
            }
        } catch (IOException e) {
            logger.error("Post-request decode failed.", e);
        }
    }
}
