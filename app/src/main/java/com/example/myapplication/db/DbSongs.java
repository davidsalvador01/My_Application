package com.example.myapplication.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        SQLiteDatabase db = null;

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        try {
            db = dbHelper.getWritableDatabase();
            cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS, null);
            if(cursorSongs.moveToFirst()){
                do {
                    Song s = new Song();
                    s.setId(cursorSongs.getInt(0));
                    s.setTitle(cursorSongs.getString(1));
                    s.setUri_spotify(cursorSongs.getString(2));
                    s.setTempo(cursorSongs.getFloat(3));
                    s.setDuration_ms(cursorSongs.getInt(4));
                    s.setPopularity(cursorSongs.getInt(5));
                    s.setRelease_year(cursorSongs.getInt(6));
                    songs.add(s);
                } while(cursorSongs.moveToNext());
            }
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            dbHelper.close();
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
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return  songs;
    }

    public ArrayList<Song> getSongsByGenre(String genre){


        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.* " +
                "FROM " + TABLE_SONGS + " AS s, " +
                TABLE_SONGS_GENRES + " as s_g, " +
                TABLE_GENRES + " as g " +
                "WHERE s.id=s_g.id_song AND g.id=s_g.id_genre AND g.name='"
                + genre + "'", null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return  songs;
    }

    public ArrayList<Song> getSongsByGenreAndBpm(String genre, float tempo){
        float bpm_min = tempo - 5;
        float bpm_max = tempo + 5;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.id, s.title, s.uri_spotify, " +
                "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                "FROM " + TABLE_SONGS + " AS s, " +
                TABLE_SONGS_GENRES + " as s_g, " +
                TABLE_GENRES + " as g " +
                "WHERE s.id=s_g.id_song AND g.id=s_g.id_genre AND g.name='"
                + genre + "' AND s.tempo>" + bpm_min + " AND s.tempo<" + bpm_max, null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public Song getSongByTitle(String title){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS +
                " AS s WHERE s.title = " + title + " ORDER BY s.popularity DESC LIMIT 1", null);
        if (cursorSongs.moveToNext()) {
            Song s = new Song();
            s.setId(cursorSongs.getInt(0));
            s.setTitle(cursorSongs.getString(1));
            s.setUri_spotify(cursorSongs.getString(2));
            s.setTempo(cursorSongs.getFloat(3));
            s.setDuration_ms(cursorSongs.getInt(4));
            s.setPopularity(cursorSongs.getInt(5));
            s.setRelease_year(cursorSongs.getInt(6));
            return s;
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return null;
    }

    public ArrayList<Song> getSongsByArtist(String artist){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.* FROM " + TABLE_ARTISTS + " AS a " +
                "JOIN t_songs_artists AS s_a ON a.id = s_a.id_artist " +
                "JOIN t_songs AS s ON s.id = s_a.id_song " +
                "WHERE a.name = '" + artist + "'"
                , null);
        if (cursorSongs.moveToNext()) {
            Song s = new Song();
            s.setId(cursorSongs.getInt(0));
            s.setTitle(cursorSongs.getString(1));
            s.setUri_spotify(cursorSongs.getString(2));
            s.setTempo(cursorSongs.getFloat(3));
            s.setDuration_ms(cursorSongs.getInt(4));
            s.setPopularity(cursorSongs.getInt(5));
            s.setRelease_year(cursorSongs.getInt(6));
            songs.add(s);
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song> getSongsByArtistAndBpm(String artist, float tempo){
        float bpm_min = tempo - 5;
        float bpm_max = tempo + 5;
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.* FROM " + TABLE_ARTISTS + " AS a " +
                        "JOIN t_songs_artists AS s_a ON a.id = s_a.id_artist " +
                        "JOIN t_songs AS s ON s.id = s_a.id_song " +
                        "WHERE a.name = '" + artist + "' AND s.tempo>" + bpm_min +
                        " AND s.tempo<" + bpm_max
                , null);
        if (cursorSongs.moveToNext()) {
            Song s = new Song();
            s.setId(cursorSongs.getInt(0));
            s.setTitle(cursorSongs.getString(1));
            s.setUri_spotify(cursorSongs.getString(2));
            s.setTempo(cursorSongs.getFloat(3));
            s.setDuration_ms(cursorSongs.getInt(4));
            s.setPopularity(cursorSongs.getInt(5));
            s.setRelease_year(cursorSongs.getInt(6));
            songs.add(s);
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public Song getSongBySpotifyUri(String uri) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            Cursor cursorSongs;
            cursorSongs = db.rawQuery("SELECT * FROM " + TABLE_SONGS +
                    " AS s WHERE s.uri_spotify = '" + uri + "' ORDER BY s.popularity DESC LIMIT 1", null);
            if (cursorSongs.moveToNext()) {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                return s;
            }
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            dbHelper.close();
        }

        return null;
    }

    public ArrayList<Song> getSongsByYear(int year){

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.* " +
                "FROM " + TABLE_SONGS + " AS s " +
                "WHERE s.release_year=" + year
                , null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song> getSongsByYearAndBpm(int year, float tempo){
        float bpm_min = tempo - 5;
        float bpm_max = tempo + 5;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.* " +
                        "FROM " + TABLE_SONGS + " AS s " +
                        "WHERE s.release_year=" + year +
                        " AND s.tempo>" + bpm_min +
                        " AND s.tempo<" + bpm_max
                        , null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song> getSongsByDecadeAndBpm(int decade, float bpm){
        float bpm_min = bpm - 5;
        float bpm_max = bpm + 5;
        int decade_end = decade+10;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.id, s.title, s.uri_spotify, " +
                "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                "FROM " + TABLE_SONGS + " AS s " +
                "WHERE s.release_year>="
                + decade + " AND s.release_year<" +decade_end +
                " AND s.tempo>" + bpm_min + " AND s.tempo<" + bpm_max, null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song>  getSongsByCondition(String condition){

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        if (condition.equals("")){
            cursorSongs = db.rawQuery("SELECT * " +
                            "FROM " + TABLE_SONGS, null);
        } else {
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                            "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                            "FROM " + TABLE_SONGS + " AS s " +
                            "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                            "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                            "WHERE " + condition
                    , null);
        }

        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song>  getSongsByConditionAndBpm(String condition, float tempo){
        float bpm_min = tempo - 5;
        float bpm_max = tempo + 5;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        if (condition.equals("")){
            cursorSongs = db.rawQuery("SELECT * " +
                    "FROM " + TABLE_SONGS + " AS s "+
                    "WHERE s.tempo>" + bpm_min +
                    " AND s.tempo<" + bpm_max, null);
        } else {
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                            "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                            "FROM " + TABLE_SONGS + " AS s " +
                            "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                            "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                            "WHERE (" + condition + ") AND s.tempo>" + bpm_min +
                            " AND s.tempo<" + bpm_max
                    , null);
        }

        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }


    public ArrayList<Song>  getSongsByConditionOrderByPopularity(String condition){

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        if(condition.equals("")){
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                    "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                    "ORDER BY s.popularity DESC", null);
        } else {
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                    "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                    "WHERE " + condition +
                    " ORDER BY s.popularity DESC", null);
        }

        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song>  getSongsByConditionAndBpmOrderByPopularity(String condition, float tempo){
        float bpm_min = tempo - 5;
        float bpm_max = tempo + 5;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        if(condition.equals("")){
            cursorSongs = db.rawQuery("SELECT DISTINCT s.* " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    " WHERE s.tempo>" + bpm_min +
                    " AND s.tempo<" + bpm_max +
                    " ORDER BY s.popularity DESC", null);
        } else {
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                    "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                    "WHERE (" + condition + ") AND s.tempo>" + bpm_min +
                    " AND s.tempo<" + bpm_max +
                    " ORDER BY s.popularity DESC", null);
        }

        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song> getSongsByConditionOrderByRating(String condition){

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        if(condition.equals("")){
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_ratings AS r ON s.id = r.id_song " +
                    "ORDER BY r.rating DESC", null);
        } else {
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                    "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                    "JOIN t_ratings AS r ON s.id = r.id_song " +
                    "WHERE " + condition +
                    " ORDER BY r.rating DESC", null);
        }

        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song> getSongsByConditionAndBpmOrderByRating(String condition, float tempo){
        float bpm_min = tempo - 5;
        float bpm_max = tempo + 5;

        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        if(condition.equals("")){
            cursorSongs = db.rawQuery("SELECT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_ratings AS r ON s.id = r.id_song " +
                    " WHERE s.tempo>" + bpm_min +
                    " AND s.tempo<" + bpm_max +
                    "ORDER BY r.rating DESC", null);
        } else {
            cursorSongs = db.rawQuery("SELECT DISTINCT s.id, s.title, s.uri_spotify, " +
                    "s.tempo, s.duration_ms, s.popularity, s.release_year " +
                    "FROM " + TABLE_SONGS + " AS s " +
                    "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                    "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                    "JOIN t_ratings AS r ON s.id = r.id_song " +
                    "WHERE (" + condition + ") AND s.tempo>" + bpm_min +
                    " AND s.tempo<" + bpm_max +
                    " ORDER BY r.rating DESC", null);
        }

        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return songs;
    }

    public ArrayList<Song> favoriteSongs(){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.* FROM " + TABLE_SONGS + " AS s " +
                "JOIN t_ratings AS r ON s.id = r.id_song " +
                "WHERE r.rating=1 " +
                "ORDER BY s.id DESC", null);
        if(cursorSongs.moveToFirst()){
            do {
                Song s = new Song();
                s.setId(cursorSongs.getInt(0));
                s.setTitle(cursorSongs.getString(1));
                s.setUri_spotify(cursorSongs.getString(2));
                s.setTempo(cursorSongs.getFloat(3));
                s.setDuration_ms(cursorSongs.getInt(4));
                s.setPopularity(cursorSongs.getInt(5));
                s.setRelease_year(cursorSongs.getInt(6));
                songs.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return  songs;
    }

    public ArrayList<String> mostStreamedArtists(String location){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<String> nameArtists = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT a.name, COUNT(a.name) as num_artist_appearences " +
                "FROM t_artists AS a " +
                "JOIN t_songs_artists AS s_a ON a.id = s_a.id_artist " +
                "JOIN t_songs AS s ON s.id = s_a.id_song " +
                "JOIN t_songs_sessions AS s_s ON s.id = s_s.id_song " +
                "JOIN t_sessions AS se ON se.id = s_s.id_session " +
                "WHERE se.location= '" + location + "' " +
                "GROUP by a.name " +
                "ORDER BY num_artist_appearences DESC LIMIT 10", null);
        if(cursorSongs.moveToFirst()){
            do {
                String s = cursorSongs.getString(0);
                nameArtists.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return nameArtists;
    }

    public ArrayList<String> mostStreamedGenres(String location){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<String> nameGenres = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT g.name, COUNT(s.id) AS num_songs " +
                "FROM t_songs AS s " +
                "JOIN t_songs_genres AS s_g ON s.id = s_g.id_song " +
                "JOIN t_genres AS g ON g.id = s_g.id_genre " +
                "JOIN t_songs_sessions AS s_s ON s.id = s_s.id_song " +
                "JOIN t_sessions AS se ON se.id = s_s.id_session " +
                "WHERE se.location= '" + location + "' " +
                "GROUP BY g.name " +
                "ORDER BY num_songs DESC LIMIT 5", null);
        if(cursorSongs.moveToFirst()){
            do {
                String s = cursorSongs.getString(0);
                nameGenres.add(s);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return nameGenres;
    }

    public ArrayList<Integer> mostStreamedYears(String location){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<Integer> years = new ArrayList<>();
        Cursor cursorSongs;
        cursorSongs = db.rawQuery("SELECT s.release_year, count(s.release_year) as years_appearences " +
                "FROM t_songs AS s " +
                "JOIN t_songs_sessions AS s_s ON s.id = s_s.id_song " +
                "JOIN t_sessions AS se ON se.id = s_s.id_session " +
                "WHERE se.location= '" + location + "' " +
                "GROUP by s.release_year " +
                "ORDER BY years_appearences DESC LIMIT 3", null);
        if(cursorSongs.moveToFirst()){
            do {
                int i = cursorSongs.getInt(0);
                years.add(i);
            } while(cursorSongs.moveToNext());
        }
        cursorSongs.close();
        db.releaseReference();
        db.close();
        dbHelper.close();
        return years;
    }

}
