package com.laxman.foodgramdelivery.models;

public class RegisterResponse {
    private String message;
    private int userId;
    private String role;
    private String token; // 🔹 add this

    public String getMessage() { return message; }
    public int getUserId() { return userId; }
    public String getRole() { return role; }
    public String getToken() { return token; } // 🔹 getter for token
}