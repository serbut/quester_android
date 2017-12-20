package com.sergeybutorin.quester.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.sergeybutorin.quester.model.UserProfile;

/**
 * Created by sergeybutorin on 28/11/2017.
 */

public class SPHelper {

    enum USER_KEYS {
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

    private static SPHelper instance;
    private static SharedPreferences sp;

    private SPHelper(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SPHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SPHelper(context);
        }
        return instance;
    }

    public void setUserData(UserProfile user) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(USER_KEYS.EMAIL.getValue(), user.getEmail());
        editor.putString(USER_KEYS.FIRSTNAME.getValue(), user.getFirstName());
        editor.putString(USER_KEYS.LASTNAME.getValue(), user.getLastName());
        editor.putString(USER_KEYS.TOKEN.getValue(), user.getToken());
        editor.apply();
    }

    public void removeUserData() {
        SharedPreferences.Editor editor = sp.edit();
        for (USER_KEYS key : USER_KEYS.values()) {
            editor.remove(key.getValue());
        }
        editor.apply();
    }

    public boolean isUserSet() {
        for (USER_KEYS key : USER_KEYS.values()) {
            if (!sp.contains(key.getValue())) {
                return false;
            }
        }
        return true;
    }

    public @Nullable UserProfile getCurrentUser() {
        if (!isUserSet()) { return null; }
        String firstname = sp.getString(USER_KEYS.FIRSTNAME.getValue(), null);
        String lastname = sp.getString(USER_KEYS.LASTNAME.getValue(), null);
        String email = sp.getString(USER_KEYS.EMAIL.getValue(), null);
        return new UserProfile(firstname, lastname, email);
    }

    public @Nullable String getUserToken() {
        if (!isUserSet()) { return null; }
        return sp.getString(USER_KEYS.TOKEN.getValue(), null);
    }
}
