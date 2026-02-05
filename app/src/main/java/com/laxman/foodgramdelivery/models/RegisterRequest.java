package com.laxman.foodgramdelivery.models;


public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;

    public RegisterRequest(String fullName, String email, String password, String phone, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }
}