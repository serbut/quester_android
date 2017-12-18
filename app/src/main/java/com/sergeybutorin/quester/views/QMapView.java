package com.sergeybutorin.quester.views;

import com.google.android.gms.maps.model.LatLng;
import com.sergeybutorin.quester.model.Quest;

import java.util.List;

/**
 * Created by sergeybutorin on 18/12/2017.
 */

public interface QMapView {
    enum QuestState {DISPLAY, ADD}

    void showQuest(Quest quest);
    void showQuests(List<Quest> quests);
    void switchState(QuestState state);
    void addMarketToMap(LatLng coordinates);
    void showNoPointsInQuest();
    void onPointsAdded(Quest quest);
    void onQuestSelected(Quest quest);
    void showQuestDetail(Quest quest);
    void setDefaultLocation(LatLng location);
    void openDetailView();
    void closeDetailView();
}
