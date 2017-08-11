package com.timego.harbin.timego.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Harbin on 8/1/17.
 */

public class RecordDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "record.db";

    private static final int DATABASE_VERSION = 1;

    public RecordDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_RECORD_TABLE = " CREATE TABLE " + RecordContract.RecordEntry.TABLE_NAME + " (" +
                RecordContract.RecordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RecordContract.RecordEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_DURATION + " INTEGER NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_EFFICIENT + " INTEGER NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_STARTTIME + " TEXT NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_YEAR + " INTEGER NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_MONTH + " INTEGER NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_DAY + " INTEGER NOT NULL, " +
                RecordContract.RecordEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";

        db.execSQL(SQL_CREATE_RECORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecordContract.RecordEntry.TABLE_NAME);
        onCreate(db);
    }



}