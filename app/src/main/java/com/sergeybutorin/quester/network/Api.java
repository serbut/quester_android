package com.sergeybutorin.quester.network;

import com.sergeybutorin.quester.model.LoginRequest;
import com.sergeybutorin.quester.model.SignupRequest;
import com.sergeybutorin.quester.model.UserProfile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by sergeybutorin on 05/11/2017.
 */

public interface Api {
    @POST("user/login")
    Call<UserProfile> login(@Body LoginRequest request);

    @POST("user/signup")
    Call<Void> signup(@Body SignupRequest request);
}
