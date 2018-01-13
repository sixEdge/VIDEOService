package com.gzf.video.dao.collections;

import com.gzf.video.core.dao.MongoProvider;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;

abstract class BaseCollection {

    private static final MongoDatabase DEFAULT_DB = MongoProvider.getDefaultDatabase();

    static MongoCollection<Document> getCollection(String collection) {
        return DEFAULT_DB.getCollection(collection);
    }

    static <T> MongoCollection<T> getCollection(String collection, Class<T> clazz) {
        return DEFAULT_DB.getCollection(collection, clazz);
    }
}
