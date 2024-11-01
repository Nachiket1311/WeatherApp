package com.example.myapplication;
public class Message {
    private String username;
    private String messageText;
    private String messageId;

    // Required empty constructor for Firebase
    public Message() {
    }

    // Constructor with parameters
    public Message(String username, String messageText, String messageId) {
        this.username = username;
        this.messageText = messageText;
        this.messageId = messageId;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
