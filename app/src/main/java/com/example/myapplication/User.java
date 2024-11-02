package com.example.myapplication;

public class User {
    private String username;
    private String email;
    private String password;

    // Default constructor (required for Firebase)
    public User() {
    }

    // Constructor to initialize the User object
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password; // Note: it's not recommended to store passwords
    }

    // Getters and Setters (optional but recommended)
    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
