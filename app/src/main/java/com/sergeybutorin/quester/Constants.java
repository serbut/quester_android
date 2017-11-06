package com.sergeybutorin.quester;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public class Constants {
    public static final String BACKEND_URL = "https://quester-backend.herokuapp.com/api/";
    public static final String EMAIL_REGEX = "[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+";
    public enum USER_KEYS {
        TOKEN("token"),
        EMAIL("email"),
        FIRSTNAME("firstname"),
        LASTNAME("lastname");

        private final String value;

        USER_KEYS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
