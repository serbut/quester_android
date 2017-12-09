package com.sergeybutorin.quester.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private AddQuestListener addQuestListener;
    private GetQuestListener getQuestListener;

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
        addQuestListener = listener;
    }

    public void setGetQuestListener(GetQuestListener listener) {
        getQuestListener = listener;
    }

    public void add(Quest quest, String token) {
        api.newQuest(token, new Quest(quest.getTitle(),
                quest.getDescription(),
                quest.getPoints())).enqueue(new Callback<Quest>() {
            @Override
            public void onResponse(@NonNull Call<Quest> call, @NonNull Response<Quest> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    addQuestListener.onAddResult(true, R.string.quest_saved, response.body());
                } else {
                    addQuestListener.onAddResult(false, R.string.error_message, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Quest> call, @NonNull Throwable t) {
                addQuestListener.onAddResult(false, R.string.error_network_message, null);
            }
        });
    }

    public void getMissing(final List<QuestBase> savedQuests) {
        api.getAllQuests().enqueue(new Callback<List<QuestBase>>() {
            @Override
            public void onResponse(@NonNull Call<List<QuestBase>> call, @NonNull Response<List<QuestBase>> response) {
                final boolean isSuccess = response.code() == 200;
                final List<QuestBase> quests = response.body();
                if (isSuccess && quests != null) {
                    Map<Integer, Integer> idVersionQuest = new HashMap<>();
                    for (QuestBase q : savedQuests) {
                        idVersionQuest.put(q.getId(), q.getVersion());
                    }
                    for (QuestBase quest : quests) {
                        int id = quest.getId();
                        if (!idVersionQuest.containsKey(id) ||
                                idVersionQuest.get(id) < quest.getVersion()) {
                            getDetails(id);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<QuestBase>> call, @NonNull Throwable t) {
                addQuestListener.onAddResult(false, R.string.error_network_message, null);
            }
        });
    }

    private void getDetails(int id) {
        api.getQuestDetails(id).enqueue(new Callback<Quest>() {
            @Override
            public void onResponse(@NonNull Call<Quest> call, @NonNull Response<Quest> response) {
                final boolean isSuccess = response.code() == 200;
                if (isSuccess) {
                    getQuestListener.onGetResult(true, response.body());
                } else {
                    getQuestListener.onGetResult(false, null);
                }

            }

            @Override
            public void onFailure(@NonNull Call<Quest> call, @NonNull Throwable t) {
                getQuestListener.onGetResult(false, null);
            }
        });
    }

    public interface AddQuestListener {
        void onAddResult(boolean success, int message, Quest quest);
    }

    public interface GetQuestListener {
        void onGetResult(boolean success, Quest quest);
    }
}
