package com.ashish.gossip.model;

public class User {

    String userId, userName, dpImgUrl, phoneNumber;

    public User() { }

    public User(String userId, String userName, String dpImgUrl, String phoneNumber) {
        this.userId = userId;
        this.userName = userName;
        this.dpImgUrl = dpImgUrl;
        this.phoneNumber = phoneNumber;
    }

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
