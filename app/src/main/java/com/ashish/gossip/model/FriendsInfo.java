package com.ashish.gossip.model;

import java.sql.Timestamp;

public class FriendsInfo {
    private String friendUserId, friendUserName, friendDpUrl;
    private String lastMessage;
    private Timestamp timeAdded;


    public FriendsInfo() { }

    public FriendsInfo(String friendUserId, String friendUserName, String friendDpUrl) {
        this.friendUserId = friendUserId;
        this.friendUserName = friendUserName;
        this.friendDpUrl = friendDpUrl;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendUserName() {
        return friendUserName;
    }

    public void setFriendUserName(String friendUserName) {
        this.friendUserName = friendUserName;
    }

    public String getFriendDpUrl() {
        return friendDpUrl;
    }

    public void setFriendDpUrl(String friendDpUrl) {
        this.friendDpUrl = friendDpUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }
}
