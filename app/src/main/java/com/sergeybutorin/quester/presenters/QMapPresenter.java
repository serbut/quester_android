package com.sergeybutorin.quester.presenters;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sergeybutorin.quester.QuesterApplication;
import com.sergeybutorin.quester.model.Point;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;
import com.sergeybutorin.quester.network.QuestController;
import com.sergeybutorin.quester.utils.GetQuestListTask;
import com.sergeybutorin.quester.utils.QuestAddTask;
import com.sergeybutorin.quester.utils.QuestUpdateTask;
import com.sergeybutorin.quester.utils.QuesterDbHelper;
import com.sergeybutorin.quester.utils.QuestsGetTask;
import com.sergeybutorin.quester.views.QMapView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sergeybutorin on 18/12/2017.
 */

public class QMapPresenter extends BasePresenter<QMapView>
        implements IQMapPresenter, QuestController.AddQuestListener,
        QuestController.GetQuestListener {

    private static final String TAG = QMapPresenter.class.getSimpleName();

    private boolean isSynchronized = false;
    private boolean isLoadingData = false;
    private QMapView.QuestState viewState = QMapView.QuestState.DISPLAY;

    private Quest addedQuest;
    private final List<Quest> quests = new LinkedList<>();
    private final LinkedList<Marker> newQuestMarkers = new LinkedList<>();

    private Quest questToAdd = new Quest(UUID.randomUUID());

    private final Map<Marker, Quest> mapper = new HashMap<>();

    private final QuestController controller;

    public QMapPresenter() {
        controller = QuestController.getInstance();
        controller.setAddQuestListener(this);
        controller.setGetQuestListener(this);

        QuesterDbHelper dbHelper = QuesterApplication.getDb();
        GetQuestListTask getQuestListTask = new GetQuestListTask(dbHelper, this);
        getQuestListTask.execute();
    }


    private void loadData() {
        isLoadingData = true;
        QuestsGetTask questsGetTask = new QuestsGetTask(QuesterApplication.getDb(), this);
        questsGetTask.execute();
    }


    public void addQuest(Quest quest) {
        isSynchronized = true;
        isLoadingData = false;
        quests.add(quest);
        if (view() != null) {
            view().showQuest(quest);
        }
    }

    private void saveQuest(Quest quest) {
        QuestAddTask questAddTask = new QuestAddTask(QuesterApplication.getDb());
        questAddTask.execute(quest);
        addQuest(quest);
    }

    private void updateQuest(Quest quest) {
        QuestUpdateTask questUpdateTask = new QuestUpdateTask(QuesterApplication.getDb());
        questUpdateTask.execute(quest);
    }

    @Override
    public void onMapClicked(LatLng coordinates) {
        switch (viewState) {
            case DISPLAY:
                if (view() != null) {
                    view().showQuests(quests);
                }
                break;
            case ADD:
                if (view() != null) {
                    view().addMarketToMap(coordinates);
                }
                questToAdd.addPoint(new Point(UUID.randomUUID(), coordinates));
                break;
        }
    }

    @Override
    public void onMarkerClicked(Marker marker) {
        Quest quest = mapper.get(marker);
        if (quest != null) {
            if (view() != null) {
                view().showQuestDetail(quest);
            }
        }
    }

    @Override
    public void onViewReady() {
        if (addedQuest != null) {
            String token = QuesterApplication.getSp().getUserToken();
            saveQuest(addedQuest);
            controller.add(addedQuest, token);
            if (view() != null) {
                view().setDefaultLocation(addedQuest.getPoints().getFirst().getCoordinates());
            }
        }
        switchViewState();

        // Let's not reload data if it's already here
        if (!isSynchronized && !isLoadingData) {
            // view().showLoading() you can show progress bar in that method if you want
            loadData();
        }
        else if (isSynchronized) {
            if (view() != null) {
                view().showQuests(this.quests);
            }
        }
    }


    @Override
    public void newMarkerAdded(Marker marker) {
        newQuestMarkers.add(marker);
        LatLng position = marker.getPosition();
    }

    @Override
    public void mapQuestWithMarker(Quest quest, Marker marker) {
        mapper.put(marker, quest);
    }

    @Override
    public void onAddButtonClicked() {
        viewState = QMapView.QuestState.ADD;
        switchViewState();
    }

    @Override
    public void onClearButtonClicked() {
        if (view() != null) {
            viewState = QMapView.QuestState.DISPLAY;
            switchViewState();
            for (Marker marker : newQuestMarkers) {
                marker.remove();
            }
            questToAdd.clear();
            view().showQuests(quests);
        }
    }

    @Override
    public void onDoneButtonClicked() {
        if (questToAdd.getPoints().size() < 1) {
            if (view() != null) {
                view().showNoPointsInQuest();
            }
        } else {
            viewState = QMapView.QuestState.DISPLAY;
            if (view() != null) {
                view().onPointsAdded(questToAdd);
            }
        }
    }

    @Override
    public void getNewQuests(List<QuestBase> existingQuests) {
        controller.getMissing(existingQuests);
    }

    public void syncQuest(Quest quest) {
        controller.add(quest, QuesterApplication.getSp().getUserToken());
    }


    @Override
    public void onAddResult(boolean success, int message, Quest quest) {
        if (quest != null) {
            updateQuest(quest);
        }
    }

    @Override
    public void onGetResult(boolean success, Quest quest) {
        if (quest != null) {
            saveQuest(quest);
        }
    }

    @Override
    public boolean isQuestAdded() {
        return addedQuest != null;
    }

    @Override
    public boolean isLoggedIn() {
        return QuesterApplication.getSp().isUserSet();
    }

    private void switchViewState() {
        if (view() != null) {
            view().switchState(viewState);
        }
    }

    @Override
    public void setAddedQuest(Quest quest) {
        this.addedQuest = quest;
    }
}
