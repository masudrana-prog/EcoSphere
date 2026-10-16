package com.ecosphere.dto;
public record RegisterRequest(String name, String email, String phone, String location, String password, String confirm, String role,
    boolean termsAccepted, String organizationName, String registrationNo) {}
