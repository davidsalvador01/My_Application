package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.myapplication.typedefs.BpmMeasurement;

import java.util.ArrayList;

public class DbBpmMeasurement extends DbHelper {
    private final Context context;

    public DbBpmMeasurement(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public void setBpm(String time, int id_session, Float heart_rate ) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("heart_rate", heart_rate);
        values.put("id_session", id_session);

        db.insert(TABLE_BPM_MEASUREMENT, null, values);

        db.close();
        dbHelper.close();
    }

    public ArrayList<BpmMeasurement> getBpmsFromDate(String date){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<BpmMeasurement> bpms = new ArrayList<>();
        Cursor cursorBpms;
        cursorBpms = db.rawQuery("SELECT bpm.* from " + TABLE_SESSIONS +
                        " AS ss, " + TABLE_BPM_MEASUREMENT + " AS bpm " +
                        "WHERE ss.id = bpm.id_session AND ss.date='" + date + "'"
                , null);
        if (cursorBpms.moveToNext()) {
            do {
                BpmMeasurement bpm_measurement = new BpmMeasurement();
                bpm_measurement.setId(cursorBpms.getInt(0));
                bpm_measurement.setId_session(cursorBpms.getInt(1));
                bpm_measurement.setTime(cursorBpms.getString(2));
                bpm_measurement.setHeart_rate(cursorBpms.getFloat(3));
                bpms.add(bpm_measurement);
                Log.d("DbBpm", ""+bpm_measurement.getHeart_rate()+"," + date);
            }while(cursorBpms.moveToNext());
        }
        cursorBpms.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return bpms;
    }
}
