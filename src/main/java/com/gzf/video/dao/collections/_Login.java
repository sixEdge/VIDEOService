package com.gzf.video.dao.collections;

import com.gzf.video.core.bean.Bean;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import org.bson.Document;

import static com.gzf.video.dao.collections._Login.LoginStruct.*;
import static com.gzf.video.pojo.component.enums.UserAccountState.NOT_ACTIVE;

/**
 * Login & Sign up.
 */
@Bean
public class _Login extends BaseCollection {

    private static final String COLLECTION = "login";
    public interface LoginStruct {
        String LOGIN_ID      =   "uId";     // int
        String LOGIN_NAME    =   "uname";   // string
        String MAIL          =   "mail";    // string
        String PASSWORD      =   "pwd";     // string
        String ACCOUNT_STATE =   "state";   // com.gzf.video.pojo.component.enums.UserAccountState
    }

    private static final MongoCollection<Document> loginCollection = getCollection(COLLECTION);


    public void _nameLogin(String username,
                           String password,
                           SingleResultCallback<Document> callback) {
        Document document = new Document()
                .append(LOGIN_NAME, username)
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
                .append(LOGIN_NAME, username)
                .append(MAIL, mail)
                .append(PASSWORD, password)
                .append(ACCOUNT_STATE, NOT_ACTIVE);
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
