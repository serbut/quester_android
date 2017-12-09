package com.sergeybutorin.quester.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.sergeybutorin.quester.fragment.QMapFragment;
import com.sergeybutorin.quester.model.Quest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sergeybutorin on 14/11/2017.
 */

public class QuestsGetTask extends AsyncTask<Void, Quest, Void> {
    private final QuesterDbHelper dbHelper;
    private final QMapFragment fragment;

    public QuestsGetTask(QuesterDbHelper questerDbHelper, QMapFragment fragment) {
        this.dbHelper = questerDbHelper;
        this.fragment = fragment;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] questProjection = {
                QuesterDbHelper.QuestEntry._ID,
                QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE,
                QuesterDbHelper.QuestEntry.COLUMN_NAME_DESCRIPTION
        };
        String sortOrder =
                QuesterDbHelper.QuestEntry._ID;
        Cursor cursor = db.query(
                QuesterDbHelper.QuestEntry.TABLE_NAME,
                questProjection, null,null,
                null,null, sortOrder);

        List<Quest> quests = new LinkedList<>();
        while(cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry._ID));
            int version = cursor.getInt(cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry.COLUMN_NAME_VERSION));
            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE));
            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry.COLUMN_NAME_DESCRIPTION));
            quests.add(new Quest(id, version, title, description));
        }
        cursor.close();

        String[] pointProjection = {
                QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER,
                QuesterDbHelper.PointEntry.COLUMN_NAME_X,
                QuesterDbHelper.PointEntry.COLUMN_NAME_Y
        };
        String selection = QuesterDbHelper.PointEntry.COLUMN_NAME_QUEST + " = ?";
        sortOrder = QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER;
        for (Quest quest : quests) {
            String[] selectionArgs = { String.valueOf(quest.getId()) };

            cursor = db.query(
                    QuesterDbHelper.PointEntry.TABLE_NAME,
                    pointProjection, selection,
                    selectionArgs,
                    null,null, sortOrder);
            LinkedList<LatLng> coordinates = new LinkedList<>();
            while(cursor.moveToNext()) {
                double x = cursor.getDouble(cursor.getColumnIndexOrThrow(QuesterDbHelper.PointEntry.COLUMN_NAME_X));
                double y = cursor.getDouble(cursor.getColumnIndexOrThrow(QuesterDbHelper.PointEntry.COLUMN_NAME_Y));
                LatLng ll = new LatLng(x, y);
                coordinates.add(ll);
            }
            quest.setPoints(coordinates);
            if (isCancelled()) return null;
            publishProgress(quest);
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Quest... values) {
        super.onProgressUpdate(values);
        for (Quest q : values) {
            fragment.addQuest(q);
            Log.d("QuestsGetTask", "Квест "+ q.getId() + " " + q.getPoints());
        }
    }
}
