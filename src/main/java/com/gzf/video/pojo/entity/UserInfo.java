package com.gzf.video.pojo.entity;

import com.gzf.video.pojo.component.enums.UserAccountState;
import com.gzf.video.pojo.component.enums.Level;
import com.gzf.video.pojo.component.enums.Sex;
import org.bson.codecs.pojo.annotations.BsonProperty;

import static com.gzf.video.dao.collections._User.UserStruct.*;

public class UserInfo {

    /**
     * User id.
     */
    @BsonProperty(USER_ID)
    private int userId;

    /**
     * Username.
     */
    @BsonProperty(USERNAME)
    private String userName;

    /**
     * Mail.
     */
    @BsonProperty(MAIL)
    private String mail;

    /**
     * Sex.
     */
    @BsonProperty(SEX)
    private Sex sex;

    /**
     * Level.
     */
    @BsonProperty(LEVEL)
    private Level level;

    /**
     * Url to face.
     */
    @BsonProperty(FACE_URL)
    private String face;

    /**
     * Money.
     */
    @BsonProperty(MONEY)
    private long money;

    /**
     * User account state.
     */
    @BsonProperty(ACCOUNT_STATE)
    private UserAccountState state;


    public UserInfo() {}

    public UserInfo(final int userId,
                    final String userName,
                    final String mail,
                    final Sex sex,
                    final Level level,
                    final String face,
                    final int money,
                    final UserAccountState state) {
        this.userId = userId;
        this.userName = userName;
        this.mail = mail;
        this.sex = sex;
        this.level = level;
        this.face = face;
        this.money = money;
        this.state = state;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(final String mail) {
        this.mail = mail;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(final Sex sex) {
        this.sex = sex;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(final Level level) {
        this.level = level;
    }

    public String getFace() {
        return face;
    }

    public void setFace(final String face) {
        this.face = face;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(final long money) {
        this.money = money;
    }

    public UserAccountState getState() {
        return state;
    }

    public void setState(final UserAccountState state) {
        this.state = state;
    }
}
