package com.ashish.gossip.model;

import java.sql.Timestamp;

public class ChatMessage {
    public String textMessage, userName, userId;
    Timestamp timeAdded;

    public ChatMessage() { }

    public ChatMessage(String textMessage, String userName, Timestamp timeAdded, String userId) {
        this.textMessage = textMessage;
        this.userName = userName;
        this.timeAdded = timeAdded;
        this.userId = userId;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
