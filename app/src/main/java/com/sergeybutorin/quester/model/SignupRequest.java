package com.sergeybutorin.quester.model;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public class SignupRequest {
    private final String email;
    private final String password;
    private final String firstname;
    private final String lastname;

    public SignupRequest(String email, String password, String firstname, String lastname) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
    }
}
