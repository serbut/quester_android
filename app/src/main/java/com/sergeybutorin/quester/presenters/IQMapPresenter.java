package com.sergeybutorin.quester.presenters;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;

import java.util.List;

/**
 * Created by sergeybutorin on 18/12/2017.
 */

public interface IQMapPresenter {
    void onAddButtonClicked();
    void onClearButtonClicked();
    void onDoneButtonClicked();
    void onMapClicked(LatLng coordinates);
    void onMarkerClicked(Marker marker);
    void getNewQuests(List<QuestBase> existingQuests);
    void syncQuest(Quest quest);
    void addQuest(Quest quest);
    void newMarkerAdded(Marker marker);
    void mapQuestWithMarker(Quest quest, Marker marker);
    void onViewReady();
    boolean isQuestAdded();
    boolean isLoggedIn();
    void setAddedQuest(Quest quest);
}