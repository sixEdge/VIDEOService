package com.gzf.video.dao;

import com.gzf.video.core.dao.MongoProvider;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import org.bson.Document;

import static com.gzf.video.dao._Login.LoginStruct.*;

/**
 * Login & Sign up.
 */
public class _Login {

    private static final MongoProvider MONGO_PROVIDER = MongoProvider.getINSTANCE();


    public static final String LOGIN_COLLECTION = "login";
    public interface LoginStruct {
        String USER_ID      =   "uId";      // int
        String USERNAME     =   "uname";    // string
        String MAIL         =   "mail";     // string
        String PASSWORD     =   "pwd";      // string
    }


    private final MongoCollection<Document> loginCollection =
            MONGO_PROVIDER.getCollection(LOGIN_COLLECTION);


    public void _nameLogin(final String username,
                           final String password,
                           final SingleResultCallback<Document> callback) {
        Document document = new Document()
                .append(USERNAME, username)
                .append(PASSWORD, password);
        loginCollection.find(document).first(callback);
    }


    public void _mailLogin(final String mail,
                           final String password,
                           final SingleResultCallback<Document> callback) {
        Document document = new Document()
                .append(MAIL, mail)
                .append(PASSWORD, password);
        loginCollection.find(document).first(callback);
    }


    public void _signUp(final String username,
                        final String mail,
                        final String password,
                        final SingleResultCallback<Void> callback) {
        Document document = new Document()
                .append(USERNAME, username)
                .append(MAIL, mail)
                .append(PASSWORD, password);
        loginCollection.insertOne(document, callback);
    }




    public void _adminLogin(final String mail,
                            final String password,
                            final SingleResultCallback callback) {
        // TODO admin login
    }


    public void _adminSignUp(final String username,
                             final String mail,
                             final String password,
                             final SingleResultCallback callback) {
        // TODO admin sign up
    }
}
