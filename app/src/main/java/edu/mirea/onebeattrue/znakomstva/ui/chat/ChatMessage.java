package edu.mirea.onebeattrue.znakomstva.ui.chat;

import java.util.Date;

public class ChatMessage {
    private String avatarUrl;
    private String messageText;
    private String messageUser;
    private long messageTime;
    private String messageUserId;

    public ChatMessage(String messageText, String messageUser, String messageUserId) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageUserId = messageUserId;

        // Set the current time when the message is created
        messageTime = new Date().getTime();
    }

    public ChatMessage() {
        // Needed for Firebase
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(String messageUserId) {
        this.messageUserId = messageUserId;
    }
}