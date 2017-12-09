package com.sergeybutorin.quester.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.fragment.QMapFragment;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.UserProfile;

/**
 * Created by sergeybutorin on 14/11/2017.
 */

public class QuestAddTask extends AsyncTask<Quest, Void, Void> {
    private final QuesterDbHelper dbHelper;
    private final QMapFragment fragment;

    public QuestAddTask(QuesterDbHelper questerDbHelper, QMapFragment fragment) {
        this.dbHelper = questerDbHelper;
        this.fragment = fragment;
    }

    @Override
    protected Void doInBackground(Quest... quests) {
        UserProfile user = SPHelper.getInstance(fragment.getContext()).getCurrentUser(); // TODO: remove, check that user is not null
        if (user == null) {
            return null;
        }
        String email = user.getEmail();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues questValues = new ContentValues();
        questValues.put(QuesterDbHelper.QuestEntry._ID, quests[0].getId());
        questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE, quests[0].getTitle());
        questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_USER, email);
        long newRowId = db.insert(QuesterDbHelper.QuestEntry.TABLE_NAME, null, questValues);
        int order = 0;
        for (LatLng point : quests[0].getPoints()) {
            ContentValues pointValues = new ContentValues();
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_QUEST, newRowId);
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER, order++);
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_X, point.latitude);
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_Y, point.longitude);
            db.insert(QuesterDbHelper.PointEntry.TABLE_NAME, null, pointValues);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(fragment.getContext(), R.string.quest_saved, Toast.LENGTH_LONG).show();
    }
}
