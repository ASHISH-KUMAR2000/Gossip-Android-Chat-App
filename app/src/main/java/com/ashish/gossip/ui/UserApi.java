package com.ashish.gossip.ui;

import com.ashish.gossip.model.User;

public class UserApi {

    private String userId, userName, dpImgUrl, phoneNumber;

    private static UserApi instance;

    public static UserApi getInstance() {
        if (instance == null)
            instance = new UserApi();

        return instance ;
    }
    public UserApi() { }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDpImgUrl() {
        return dpImgUrl;
    }

    public void setDpImgUrl(String dpImgUrl) {
        this.dpImgUrl = dpImgUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
