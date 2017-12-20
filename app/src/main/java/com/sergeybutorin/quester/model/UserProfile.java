package com.sergeybutorin.quester.model;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public class UserProfile {
    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private String token;

    private UserProfile() {
    }

    public UserProfile(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getToken() {
        return token;
    }
}
