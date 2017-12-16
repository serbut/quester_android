package com.sergeybutorin.quester.utils;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.sergeybutorin.quester.model.Quest;

/**
 * Created by sergeybutorin on 09/12/2017.
 */

public class QuestUpdateTask extends AsyncTask<Quest, Void, Void> {

    private final QuesterDbHelper dbHelper;

    public QuestUpdateTask(QuesterDbHelper questerDbHelper) {
        this.dbHelper = questerDbHelper;
    }

    @Override
    protected Void doInBackground(Quest... quests) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + QuesterDbHelper.QuestEntry.TABLE_NAME +
                        " SET " + QuesterDbHelper.QuestEntry.COLUMN_NAME_SYNCED + " = 1, " +
                        QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE + " = ?, " +
                        QuesterDbHelper.QuestEntry.COLUMN_NAME_DESCRIPTION + " = ? " +
                        " WHERE " + QuesterDbHelper.QuestEntry.COLUMN_NAME_UUID + " = ?;",
                    new Object [] {  quests[0].getTitle(),
                        quests[0].getDescription(),
                        Common.uuidToBytes(quests[0].getUuid()) } );
        return null;
    }
}
