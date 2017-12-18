package com.sergeybutorin.quester.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.sergeybutorin.quester.model.Point;
import com.sergeybutorin.quester.model.Quest;

/**
 * Created by sergeybutorin on 14/11/2017.
 */

public class QuestAddTask extends AsyncTask<Quest, Void, Void> {
    private final QuesterDbHelper dbHelper;

    public QuestAddTask(QuesterDbHelper questerDbHelper) {
        this.dbHelper = questerDbHelper;
    }

    @Override
    protected Void doInBackground(Quest... quests) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues questValues = new ContentValues();
        questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_UUID, Common.uuidToBytes(quests[0].getUuid()));
        questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE, quests[0].getTitle());
        questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_SYNCED, quests[0].isSynced());
        questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_USER, "todo"); // TODO: get real email
        long newRowId = db.insert(QuesterDbHelper.QuestEntry.TABLE_NAME, null, questValues);
        int order = 0;
        for (Point point : quests[0].getPoints()) {
            ContentValues pointValues = new ContentValues();
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_UUID, Common.uuidToBytes(point.getUuid()));
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_QUEST, newRowId);
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER, order++);
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_X, point.getCoordinates().latitude);
            pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_Y, point.getCoordinates().longitude);
            db.insert(QuesterDbHelper.PointEntry.TABLE_NAME, null, pointValues);
        }
        return null;
    }
}
