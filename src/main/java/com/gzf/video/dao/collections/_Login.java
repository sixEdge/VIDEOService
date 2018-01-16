package com.gzf.video.dao.collections;

import com.gzf.video.core.bean.Bean;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import org.bson.Document;

import static com.gzf.video.dao.collections._Login.LoginStruct.*;

/**
 * Login & Sign up.
 */
@Bean
public class _Login extends BaseCollection {

    public static final String COLLECTION = "login";
    public interface LoginStruct {
        String USER_ID      =   "uId";    // int
        String USERNAME     =   "uname";  // string
        String MAIL         =   "mail";   // string
        String PASSWORD     =   "pwd";    // string
    }

    private static final MongoCollection<Document> loginCollection = getCollection(COLLECTION);


    public void _nameLogin(String username,
                           String password,
                           SingleResultCallback<Document> callback) {
        Document document = new Document()
                .append(USERNAME, username)
                .append(PASSWORD, password);
        loginCollection.find(document).first(callback);
    }


    public void _mailLogin(String mail,
                           String password,
                           SingleResultCallback<Document> callback) {
        Document document = new Document()
                .append(MAIL, mail)
                .append(PASSWORD, password);
        loginCollection.find(document).first(callback);
    }


    public void _signUp(String username,
                        String mail,
                        String password,
                        SingleResultCallback<Void> callback) {
        Document document = new Document()
                .append(USERNAME, username)
                .append(MAIL, mail)
                .append(PASSWORD, password);
        loginCollection.insertOne(document, callback);
    }


   /* public void _adminLogin(String mail,
                            String password,
                            SingleResultCallback callback) {
        // TODO admin login
    }


    public void _adminSignUp(String username,
                             String mail,
                             String password,
                             SingleResultCallback callback) {
        // TODO admin sign up
    }*/
}
