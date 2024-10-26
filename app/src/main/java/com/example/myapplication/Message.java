package com.example.myapplication;
public class Message {
    private String username;
    private String messageText;
    private String messageId; // Unique message ID for each message
    private String replyTo;  // ID of the message this message is replying to (optional)

    public Message() {}

    public Message(String username, String messageText, String messageId, String replyTo) {
        this.username = username;
        this.messageText = messageText;
        this.messageId = messageId;
        this.replyTo = replyTo;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
}
