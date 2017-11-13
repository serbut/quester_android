package com.sergeybutorin.quester.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.sergeybutorin.quester.Constants;

/**
 * Created by sergeybutorin on 13/11/2017.
 */

public class QuesterDbHelper extends SQLiteOpenHelper {
    private static QuesterDbHelper instance;

    private QuesterDbHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    public static QuesterDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new QuesterDbHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_QUEST_TABLE);
        db.execSQL(SQL_CREATE_POINT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_POINT_TABLE);
        db.execSQL(SQL_DELETE_QUEST_TABLE);
        onCreate(db);
    }

    public static class QuestEntry implements BaseColumns {
        static final String TABLE_NAME = "quest";
        static final String COLUMN_NAME_USER = "user";
        static final String COLUMN_NAME_TITLE = "title";
    }

    public static class PointEntry implements BaseColumns {
        static final String TABLE_NAME = "point";
        static final String COLUMN_NAME_QUEST = "quest_id";
        static final String COLUMN_NAME_ORDER = "order";
        static final String COLUMN_NAME_X = "x";
        static final String COLUMN_NAME_Y = "y";
    }

    private static final String SQL_CREATE_QUEST_TABLE =
            "CREATE TABLE " + QuestEntry.TABLE_NAME + " (" +
                    QuestEntry._ID + " INTEGER PRIMARY KEY," +
                    QuestEntry.COLUMN_NAME_USER + " VARCHAR(100) NOT NULL)," +
                    QuestEntry.COLUMN_NAME_TITLE + " VARCHAR(100) NOT NULL)";

    private static final String SQL_CREATE_POINT_TABLE =
            "CREATE TABLE " + PointEntry.TABLE_NAME + " (" +
                    PointEntry._ID + " INTEGER PRIMARY KEY," +
                    PointEntry.COLUMN_NAME_QUEST + " INT REFERENCES " + QuestEntry.TABLE_NAME + "(" + QuestEntry._ID + ") NOT NULL)," +
                    PointEntry.COLUMN_NAME_ORDER + " INT NOT NULL)," +
                    PointEntry.COLUMN_NAME_X + " REAL NOT NULL)," +
                    PointEntry.COLUMN_NAME_Y + " REAL NOT NULL)";

    private static final String SQL_DELETE_QUEST_TABLE =
            "DROP TABLE IF EXISTS " + QuestEntry.TABLE_NAME;

    private static final String SQL_DELETE_POINT_TABLE =
            "DROP TABLE IF EXISTS " + PointEntry.TABLE_NAME;
}
