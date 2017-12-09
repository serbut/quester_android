package com.sergeybutorin.quester.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.sergeybutorin.quester.fragment.QMapFragment;
import com.sergeybutorin.quester.model.QuestBase;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sergeybutorin on 09/12/2017.
 */

public class GetQuestListTask extends AsyncTask<Void, Void, List<QuestBase>> {
    private final QuesterDbHelper dbHelper;
    private final QMapFragment fragment;

    public GetQuestListTask(QuesterDbHelper questerDbHelper, QMapFragment fragment) {
        this.dbHelper = questerDbHelper;
        this.fragment = fragment;
    }

    @Override
    protected List<QuestBase> doInBackground(Void... voids) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] questProjection = {
                QuesterDbHelper.QuestEntry._ID,
                QuesterDbHelper.QuestEntry.COLUMN_NAME_VERSION
        };
        String sortOrder =
                QuesterDbHelper.QuestEntry._ID;
        Cursor cursor = db.query(
                QuesterDbHelper.QuestEntry.TABLE_NAME,
                questProjection, null,null,
                null,null, sortOrder);

        List<QuestBase> quests = new LinkedList<>();
        while(cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry._ID));
            int version = cursor.getInt(
                    cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry.COLUMN_NAME_VERSION));
            quests.add(new QuestBase(id, version));
        }
        cursor.close();
        return quests;
    }

    @Override
    protected void onPostExecute(List<QuestBase> savedQuests) {
        super.onPostExecute(savedQuests);
        fragment.getNewQuests(savedQuests);
    }
}
