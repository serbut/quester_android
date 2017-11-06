package com.sergeybutorin.quester.model;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public class SignupRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    public SignupRequest(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
