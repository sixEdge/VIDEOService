package com.gzf.video.dao.collections;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.pojo.entity.UserInfo;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import org.bson.Document;

import static com.gzf.video.dao.collections._User.UserStruct.MAIL;
import static com.gzf.video.dao.collections._User.UserStruct.USERNAME;
import static com.gzf.video.dao.collections._User.UserStruct.USER_ID;

/**
 * User info.
 */
@Bean
public class _User extends BaseCollection {

    private static final String COLLECTION = "user";
    public interface UserStruct {
        String USER_ID       =   "uId";    // int
        String USERNAME      =   "uname";  // string
        String MAIL          =   "mail";   // string
        String SEX           =   "sex";    // com.gzf.video.pojo.component.enums.Sex
        String LEVEL         =   "level";  // com.gzf.video.pojo.component.enums.Level
        String FACE_URL      =   "face";   // String
        String MONEY         =   "money";  // long
        String ACCOUNT_STATE =   "state";  // com.gzf.video.pojo.component.enums.UserAccountState
    }

    private static final MongoCollection<Document> userCollection = getCollection(COLLECTION);

    // will have more usages in the future
    private static final MongoCollection<UserInfo> userCollection1 = getCollection(COLLECTION, UserInfo.class);


    public void _userInfo(int userId, SingleResultCallback<Document> callback) {
        Document document = new Document(USER_ID, userId);
        userCollection.find(document).first(callback);
    }


    public void _findUserInfoByUserName(String username, SingleResultCallback<Document> callback) {
        Document document = new Document(USERNAME, username);
        userCollection.find(document).first(callback);
    }


    public void _findUserInfoByMail(String mail, SingleResultCallback<Document> callback) {
        Document document = new Document(MAIL, mail);
        userCollection.find(document).first(callback);
    }
}
