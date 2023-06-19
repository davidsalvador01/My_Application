package com.example.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.myapplication.typedefs.Song;

import java.util.ArrayList;

public class DbSongs extends DbHelper{
    private final Context context;

    public DbSongs(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public ArrayList<Song> getSongs(){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS, null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitulo(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        return  songs;
    }

    public ArrayList<Song> getSongsByBpm(float bpm){
        float bpm_min = bpm - 5;
        float bpm_max = bpm + 5;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS + " AS s WHERE s.tempo >= "
                + bpm_min + " AND s.tempo <= " + bpm_max, null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitulo(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        return  songs;
    }

    public ArrayList<Song> getSongsByGenre(String genre){


        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.id, s.titulo, s.uri_spotify, " +
                "s.tempo, s.duration_ms, s.popularity " +
                "FROM " + TABLE_SONGS + " AS s, " +
                TABLE_SONGS_GENRES + " as s_g, " +
                TABLE_GENRES + " as g " +
                "WHERE s.id=s_g.id_song AND g.id=s_g.id_genre AND g.name='"
                + genre + "'", null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitulo(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        return  songs;
    }

    public Song getSongByTitle(String title){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS +
                " AS s WHERE s.titulo = " + title + " ORDER BY s.popularity DESC LIMIT 1", null);
        if (cursorSongs.moveToNext()) {
            Song s = new Song();
            s.setId(cursorSongs.getInt(0));
            s.setTitulo(cursorSongs.getString(1));
            s.setUri_spotify(cursorSongs.getString(2));
            s.setTempo(cursorSongs.getFloat(3));
            s.setDuration_ms(cursorSongs.getInt(4));
            s.setPopularity(cursorSongs.getInt(5));
            return s;
        }
        return null;
    }

    public Song getSongBySpotifyUri(String uri){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS +
                " AS s WHERE s.uri_spotify = '" + uri + "' ORDER BY s.popularity DESC LIMIT 1", null);
        if (cursorSongs.moveToNext()) {
            Song s = new Song();
            s.setId(cursorSongs.getInt(0));
            s.setTitulo(cursorSongs.getString(1));
            s.setUri_spotify(cursorSongs.getString(2));
            s.setTempo(cursorSongs.getFloat(3));
            s.setDuration_ms(cursorSongs.getInt(4));
            s.setPopularity(cursorSongs.getInt(5));
            return s;
        }
        return null;
    }
}
