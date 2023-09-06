package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;


public class DbSessions extends DbHelper {
    private final Context context;

    public DbSessions(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public int setSession(String date, String id_user, String location, String init_time, String end_time) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_user", id_user);
        values.put("location", location);
        values.put("date", date);
        values.put("init_time", init_time);
        values.put("end_time", end_time);

        int id = (int) db.insert(TABLE_SESSIONS, null, values);

        db.close();
        dbHelper.close();

        return id;
    }

    public void updateEndSession(int id, String endTime){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL(("UPDATE " + TABLE_SESSIONS + " SET end_time='" + endTime +
                "' WHERE id =" + id ));
    }

    public void setSongSession(int id_song, int id_session, String time, String mode_bpm ) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_song", id_song);
        values.put("id_session", id_session);
        values.put("time", time);
        values.put("mode_bpm", mode_bpm);

        db.insert(TABLE_SONGS_SESSIONS, null, values);

        db.close();
        dbHelper.close();
    }



}
