package com.sergeybutorin.quester.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.Quest;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sergeybutorin on 18/12/2017.
 */

public class QuestDetailFragment extends Fragment {

    public static final String QUEST_ARG = "QUEST_ARG";

    @BindView(R.id.quest_detail_title_tw)
    TextView titleTw;
    @BindView(R.id.quest_detail_description_tw)
    TextView descriptionTw;

    Quest quest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, view);

        Bundle bundle = this.getArguments();
        if (bundle == null) {
            throw new IllegalArgumentException();
        }
        quest = bundle.getParcelable(QUEST_ARG);

        if (quest == null) {
            throw new NullPointerException("Unexpectedly found null while reading quest");
        }
        titleTw.setText(quest.getTitle());
        descriptionTw.setText(quest.getDescription());
        Log.d("TAG", "Description: " + quest.getDescription());
        return view;
    }
}
