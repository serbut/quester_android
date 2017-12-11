package com.sergeybutorin.quester.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.activity.MainActivity;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.network.QuestController;
import com.sergeybutorin.quester.utils.SPHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sergeybutorin on 10/12/2017.
 */

public class QuestAddFragment extends Fragment implements QuestController.AddQuestListener {

    public static final String QUEST_ARG = "QUEST_ARG";
    private QuestController controller;
    QuestSavedListener questSavedListener;

    Quest quest;

    @BindView(R.id.quest_title_et)
    EditText titleEditText;
    @BindView(R.id.quest_description_et)
    EditText descriptionEditText;

    public interface QuestSavedListener {
        void onQuestSaved(Quest quest);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_quest, container, false);

        ButterKnife.bind(this, view);
        questSavedListener = (QuestSavedListener) getActivity();

        Bundle bundle = this.getArguments();
        if (bundle == null) {
            throw new IllegalArgumentException();
        }

        controller = QuestController.getInstance();
        controller.setAddQuestListener(this);

        quest = (Quest) bundle.getSerializable(QUEST_ARG);
        Log.d("QUEST_ADD", "Points: " + quest.getPoints().toString());
        return view;
    }

    @OnClick(R.id.button_quest_create_done)
    void onDoneButtonClick() {
        // TODO: Add progress bar
        ((MainActivity)getActivity()).hideSoftKeyboard();

        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), R.string.error_empty_strings, Toast.LENGTH_LONG).show();
            return;
        }
        String token = SPHelper.getInstance(getContext()).getUserToken();
        quest.setTitle(title);
        quest.setDescription(description);
        controller.add(quest, token);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.setAddQuestListener(null);
    }

    @OnClick(R.id.button_quest_create_cancel)
    void onCancelButtonClick() {
        ((MainActivity)getActivity()).hideSoftKeyboard();
        questSavedListener.onQuestSaved(null);
    }

    @Override
    public void onAddResult(boolean success, int message, Quest quest) {
        if (!success) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        } else if (quest != null) {
            Answers.getInstance().logCustom(new CustomEvent("Quest Add"));
            questSavedListener.onQuestSaved(quest);
        }
    }
}
