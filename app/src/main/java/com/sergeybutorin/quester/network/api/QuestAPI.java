package com.sergeybutorin.quester.network.api;

import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by sergeybutorin on 10/12/2017.
 */

public interface QuestAPI {
    @POST("quest/new")
    Call<Quest> newQuest(@Header("User-Token") String userToken, @Body Quest quest);

    @POST("quest/get_all")
    Call<List<QuestBase>> getAllQuests();

    @POST("quest/{uuid}/details")
    Call<Quest> getQuestDetails(@Path("uuid") UUID questUuid);
}