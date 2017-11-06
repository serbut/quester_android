package com.sergeybutorin.quester.model;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public class LoginRequest {
    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
