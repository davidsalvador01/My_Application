package com.example.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class DbRatings extends DbHelper{
    private final Context context;

    public DbRatings(Context context) {
        super(context);
        this.context = context;
    }

    public void setRating(boolean like, int id_song, String id_user) {
        int rating;
        if (like){
            rating = 1;
        } else {
            rating = 0;
        }
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int existingRating = getRating(id_song, id_user);
        if (existingRating == -1){
            db.execSQL(("INSERT INTO " + TABLE_RATINGS + "(rating, id_song, id_user)" +
                    " VALUES ("
                    + rating + ", " + id_song + ", '" + id_user +"')"));
        } else {
            db.execSQL(("UPDATE " + TABLE_RATINGS + " SET rating=" + rating +
                    " WHERE id_song =" + id_song + " AND id_user='" + id_user + "'"));
        }
        db.releaseReference();
        db.close();
        dbHelper.close();
    }
    public int getRating(int id_song, String id_user){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT r.rating FROM " + TABLE_RATINGS +
                " AS r WHERE r.id_song = " + id_song + " AND r.id_user='" + id_user +
                "' LIMIT 1", null);
        if (cursorSongs.moveToNext()) {
            int rating;
            rating = cursorSongs.getInt(0);
            return rating;
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return -1;
    }

}
