package com.example.myapplication;
public class Message {
    private String username; // Username to display
    private String messageText; // The actual message text
    private String messageid;
    private long timestamp; // Timestamp of the message

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String username, String messageText,String messageid, long timestamp) {
        this.username = username;
        this.messageText = messageText;
        this.messageid = messageid;
        this.timestamp = timestamp;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getMessageid() {
        return messageid;
    }
    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }
}