package com.sergeybutorin.quester.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;
import com.sergeybutorin.quester.utils.GetQuestListTask;
import com.sergeybutorin.quester.utils.QuesterDbHelper;
import com.sergeybutorin.quester.utils.QuestsGetTask;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sergeybutorin on 28/11/2017.
 */

public class QuestController {

    private static QuestController instance;
    private final Api api;
    private AddQuestListener addQuestResult;
    private GetQuestListener getQuestResult;

    private QuestController() {
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

    public static synchronized QuestController getInstance() {
        if (instance == null) {
            instance = new QuestController();
        }
        return instance;
    }

    public void setAddQuestListener(AddQuestListener listener) {
        addQuestResult = listener;
    }

    public void setGetQuestListener(GetQuestListener listener) {
        getQuestResult = listener;
    }

    public void add(Quest quest, String token) {
        api.newQuest(token, new Quest(quest.getTitle(), quest.getPoints())).enqueue(new Callback<Quest>() {
            @Override
            public void onResponse(@NonNull Call<Quest> call, @NonNull Response<Quest> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    addQuestResult.onAddResult(true, R.string.quest_server_ok, response.body());
                } else {
                    addQuestResult.onAddResult(false, R.string.error_message, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Quest> call, @NonNull Throwable t) {
                addQuestResult.onAddResult(false, R.string.error_network_message, null);
            }
        });
    }

    public void getMissing(final List<QuestBase> savedQuests) {
        api.getAllQuests().enqueue(new Callback<List<QuestBase>>() {
            @Override
            public void onResponse(@NonNull Call<List<QuestBase>> call, @NonNull Response<List<QuestBase>> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    for (QuestBase quest : response.body()) {
                        Log.d("QUEST", "id = " + quest.getId() + " version = " + quest.getVersion());
                        int id = quest.getId();
                        if (!savedQuests.contains(id)) {
                            getDetails(id);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<QuestBase>> call, Throwable t) {
                addQuestResult.onAddResult(false, R.string.error_network_message, null);
            }
        });
    }

    private void getDetails(int id) {
        api.getQuestDetails(id).enqueue(new Callback<Quest>() {
            @Override
            public void onResponse(@NonNull Call<Quest> call, @NonNull Response<Quest> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    getQuestResult.onGetResult(true, R.string.quest_received, response.body());
                } else {
                    addQuestResult.onAddResult(false, R.string.error_message, null);
                }
            }

            @Override
            public void onFailure(Call<Quest> call, Throwable t) {
                getQuestResult.onGetResult(false, R.string.error_network_message, null);
            }
        });
    }

    public interface AddQuestListener {
        void onAddResult(boolean success, int message, Quest quest);
    }

    public interface GetQuestListener {
        void onGetResult(boolean success, int message, Quest quest);
    }
}
