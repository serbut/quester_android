package com.sergeybutorin.quester.network;

import android.support.annotation.NonNull;

import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;
import com.sergeybutorin.quester.network.api.QuestAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final QuestAPI api;
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
        api = retrofit.create(QuestAPI.class);
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
        api.newQuest(token, new Quest(quest.getUuid(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getPoints())).enqueue(new Callback<Quest>() {
            @Override
            public void onResponse(@NonNull Call<Quest> call, @NonNull Response<Quest> response) {
                final boolean isSuccess = response.code() == 200;
                Quest quest = response.body();
                if (isSuccess && quest != null) {
                    quest.setSynced(true);
                    addQuestListener.onAddResult(true, R.string.quest_saved, quest);
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
                    Map<UUID, Integer> idVersionQuest = new HashMap<>();
                    for (QuestBase q : savedQuests) {
                        idVersionQuest.put(q.getUuid(), q.getVersion());
                    }
                    for (QuestBase quest : quests) {
                        UUID uuid = quest.getUuid();
                        if (!idVersionQuest.containsKey(uuid) ||
                                idVersionQuest.get(uuid) < quest.getVersion()) {
                            getDetails(uuid);
                        }
                    }

                    // TODO: get is saved version less then version on server
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<QuestBase>> call, @NonNull Throwable t) {

                addQuestListener.onAddResult(false, R.string.error_network_message, null);
            }
        });
    }

    private void getDetails(UUID uuid) {
        api.getQuestDetails(uuid).enqueue(new Callback<Quest>() {
            @Override
            public void onResponse(@NonNull Call<Quest> call, @NonNull Response<Quest> response) {
                final boolean isSuccess = response.code() == 200;
                Quest quest = response.body();
                if (isSuccess && quest != null && !quest.getPoints().isEmpty()) {
                    quest.setSynced(true);
                    getQuestListener.onGetResult(true, quest);
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
