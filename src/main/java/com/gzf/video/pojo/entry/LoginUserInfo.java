package com.gzf.video.pojo.entry;

import com.gzf.video.pojo.component.enums.Level;
import com.gzf.video.pojo.component.enums.Sex;

public class LoginUserInfo {

    /**
     * User id.
     */
    private int userId;

    /**
     * User name.
     */
    private String userName;

    /**
     * Sex.
     */
    private Sex sex;

    /**
     * Level.
     */
    private Level level;

    /**
     * Url to face.
     */
    private String face;

    /**
     * Money.
     */
    private int money;

    public LoginUserInfo() {}

    public LoginUserInfo(final int userId,
                         final String userName,
                         final Sex sex,
                         final Level level,
                         final String face,
                         final int money) {
        this.userId = userId;
        this.userName = userName;
        this.sex = sex;
        this.level = level;
        this.face = face;
        this.money = money;
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

    public int getMoney() {
        return money;
    }

    public void setMoney(final int money) {
        this.money = money;
    }
}
