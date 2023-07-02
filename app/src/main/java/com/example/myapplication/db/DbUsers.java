package com.example.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;


import com.example.myapplication.typedefs.User;



public class DbUsers extends DbHelper{
    private final Context context;

    public DbUsers(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public void setUser(String id, String name, String email, String photoUrl) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(("INSERT INTO " + TABLE_USERS + " VALUES ('"+ id + "', '"
                        + name + "', '" + email + "', '" + photoUrl +"')"));

        db.releaseReference();
        db.close();
        dbHelper.close();
    }

    public User searchUserByEmail(String email) {
        DbHelper dbHelper = new DbHelper(context);
        Cursor cursorUser;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cursorUser = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email='" + email +"'"
                , null);

        if(cursorUser.moveToFirst()){
            do {
                User user = new User();
                user.setId(cursorUser.getString(0));
                user.setName(cursorUser.getString(1));
                user.setEmail(cursorUser.getString(2));
                user.setPhotoUrl(cursorUser.getString(3));
                return user;
            } while(cursorUser.moveToNext());
        }
        cursorUser.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return null;

    }
}
