package com.sergeybutorin.quester.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.network.AuthController;


/**
 * Created by frozenfoot on 13.11.17.
 */

public class QuestDetailsFragment extends Fragment {

    public static final String RATING_KEY = "rating";
    public static final String DESCRIPTION_KEY = "description";
    public static final String NAME_KEY = "name";

    public TextView questName;
    public TextView questRating;
    public TextView questDescription;
    public Button mark;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest_details, container, false);

        questName = view.findViewById(R.id.quest_details_quest_name);
        questRating = view.findViewById(R.id.quest_details_quest_rating_view);

        questDescription = view.findViewById(R.id.quest_details_quest_description_view);

        mark = view.findViewById(R.id.quest_details_mark_quest_button);

        questName.setText(getArguments().getString(NAME_KEY));
        questRating.setText(String.valueOf(getArguments().getDouble(RATING_KEY)));
        questDescription.setText(getArguments().getString(DESCRIPTION_KEY));

        return view;
    }
}
