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

    private HttpPostRequestDecoder a;
    private FullHttpRequest request;

    private Map<String, byte[]> fileContents;

    public PostRequest(final ChannelHandlerContext ctx,
                       final FullHttpRequest req,
                       final Set<Cookie> cookies,
                       final Session session) {
        super(ctx, req.headers(), cookies, session);
        this.request = req;
    }


    @Override
    public void release() {
        if (a != null) {
            a.destroy();
        }
    }

    @Override
    public Map<String, String> parameters() {
        checkAndDecode();
        return parameters;
    }

    public Map<String, byte[]> getFileContents() {
        checkAndDecode();
        return fileContents;
    }


    private void checkAndDecode() {
        if (a != null) {
            return;
        }

        a = new HttpPostRequestDecoder(request);
        parameters = new HashMap<>();
        fileContents = new HashMap<>();

        try {
            for (InterfaceHttpData data : a.getBodyHttpDatas()) {
                switch (data.getHttpDataType()) {
                    case Attribute: {
                        MixedAttribute attr = (MixedAttribute) data;
                        parameters.putIfAbsent(attr.getName(), attr.getValue());
                        System.out.println(attr.getName() + " : " + attr.getValue());
                        break;
                    }
                    case FileUpload: {
                        MixedFileUpload fileUpload = (MixedFileUpload) data;
                        fileContents.putIfAbsent(fileUpload.getFilename(), fileUpload.get());
                        System.out.println(fileUpload.getFilename() + " : " + new String(fileUpload.get()));
                        break;
                    }
                    default:
                        // continue
                }
            }
        } catch (IOException e) {
            logger.error("HttpPostRequestDecoder", e);
        }
    }
}
