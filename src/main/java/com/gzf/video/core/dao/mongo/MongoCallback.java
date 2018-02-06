package com.gzf.video.core.dao.mongo;

import com.mongodb.async.SingleResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface MongoCallback<T> extends SingleResultCallback<T> {
    Logger LOGGER = LoggerFactory.getLogger(MongoCallback.class);

    @Override
    default void onResult(T result, Throwable t) {
        if (t != null) {
            LOGGER.error("MongoCallback", t);
        }
        callback(result);
    }

    void callback(T result);
}
