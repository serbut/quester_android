package com.sergeybutorin.quester.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.LoginRequest;
import com.sergeybutorin.quester.model.SignupRequest;
import com.sergeybutorin.quester.model.UserProfile;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public class AuthController {
    private static AuthController instance;
    private final Api api;
    private LoginListener loginResult;
    private SignupListener signupResult;

    public AuthController(Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(Constants.BACKEND_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit = builder.client(httpClient.build()).build();
        api = retrofit.create(Api.class);
    }

    public static synchronized AuthController getInstance(Context context) {
        if (instance == null) {
            instance = new AuthController(context);
        }
        return instance;
    }

    public static boolean isAuthorized(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        for (Constants.USER_KEYS key : Constants.USER_KEYS.values()) {
            if (!sp.contains(key.getValue())) {
                return false;
            }
        }
        return true;
    }

    public static void logout(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        for (Constants.USER_KEYS key : Constants.USER_KEYS.values()) {
            editor.remove(key.getValue());
        }
        editor.apply();
    }

    public void setLoginResultListener(LoginListener listener) {
        loginResult = listener;
    }

    public void setSignupResultListener(SignupListener listener) {
        signupResult = listener;
    }

    public void login(String email, String password) {
        api.login(new LoginRequest(email, password)).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    loginResult.onResult(true, R.string.auth_ok, response.body());
                    return;
                }
                int message;
                switch (response.code()) {
                    case 400:
                        message = R.string.error_wrong_data;
                        break;
                    case 403:
                        message = R.string.error_wrong_email_password;
                        break;
                    default:
                        message = R.string.error_message;
                        break;
                }
                if (loginResult != null) {
                    loginResult.onResult(isSuccess, message, null);
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                loginResult.onResult(false, R.string.error_network_message, null);
            }
        });
    }

    public void signup(String email, String password, String firstName, String lastName) {
        api.signup(new SignupRequest(email, password, firstName, lastName)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    // success
                    return;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                signupResult.onResult(false, R.string.error_network_message, null);
            }
        });
    }

    public interface LoginListener {
        void onResult(boolean success, int message, UserProfile user);
    }

    public interface SignupListener {
        void onResult(boolean success, int message, UserProfile user);
    }
}