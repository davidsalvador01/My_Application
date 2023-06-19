package com.example.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.myapplication.JsonHelper;
import com.example.myapplication.typedefs.Artist;
import com.example.myapplication.typedefs.Genre;
import com.example.myapplication.typedefs.Song;
import com.example.myapplication.typedefs.SongArtist;
import com.example.myapplication.typedefs.SongGenre;

import java.io.InputStream;
import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AppDatabase";
    public static final String TABLE_SONGS = "t_songs";
    public static final String TABLE_ARTISTS = "t_artists";
    public static final String TABLE_SONGS_ARTISTS = "t_songs_artists";
    public static final String TABLE_GENRES = "t_genres";
    public static final String TABLE_SONGS_GENRES = "t_songs_genres";
    public static final String TABLE_RATINGS = "t_ratings";
    public static final String TABLE_USERS = "t_users";



    private InputStream inputStreamSongs;
    private InputStream inputStreamArtists;
    private InputStream inputStreamSongArtists;
    private InputStream inputStreamGenres;
    private InputStream inputStreamSongGenres;


    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DbHelper(@Nullable Context context, InputStream inputStreamSongs,
                    InputStream inputStreamArtists, InputStream inputStreamSongArtists,
                    InputStream inputStreamGenres, InputStream inputStreamSongGenres) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.inputStreamSongs = inputStreamSongs;
        this.inputStreamArtists = inputStreamArtists;
        this.inputStreamSongArtists = inputStreamSongArtists;
        this.inputStreamGenres = inputStreamGenres;
        this.inputStreamSongGenres = inputStreamSongGenres;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SONGS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "titulo TEXT NOT NULL," +
                "uri_spotify TEXT NOT NULL," +
                "tempo FLOAT," +
                "duration_ms INTEGER," +
                "popularity INTEGER)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_ARTISTS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SONGS_ARTISTS + "(" +
                "id_song INTEGER REFERENCES " + TABLE_SONGS + ","+
                "id_artist INTEGER REFERENCES " + TABLE_ARTISTS + ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_GENRES + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SONGS_GENRES + "(" +
                "id_song INTEGER REFERENCES " + TABLE_SONGS + ","+
                "id_genre INTEGER REFERENCES " + TABLE_GENRES + ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_RATINGS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "rating INTEGER,"+
                "id_user INTEGER REFERENCES " + TABLE_USERS + ","+
                "id_song INTEGER REFERENCES " + TABLE_SONGS + ")");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                "id TEXT PRIMARY KEY,"+
                "name TEXT, "+
                "email TEXT," +
                "photo_url TEXT)");

        insert_songs(sqLiteDatabase);
        insert_artists(sqLiteDatabase);
        insert_song_artists(sqLiteDatabase);
        insert_genres(sqLiteDatabase);
        insert_song_genres(sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE " + TABLE_SONGS);
        onCreate(sqLiteDatabase);
    }

    private void insert_songs(SQLiteDatabase sqLiteDatabase){
        ArrayList<Song> songs;
        JsonHelper jsonHelper = new JsonHelper();
        songs = jsonHelper.readJsonSongs(inputStreamSongs);
        if (songs != null) {
            for (Song s : songs) {
                String title;
                if (s.getTitulo().contains("'")){
                    title = s.getTitulo().replace("'", "''");
                } else {
                    title = s.getTitulo();
                }
                String sql = "INSERT INTO t_songs VALUES (" + s.getId() + ", "
                        + "'" + title + "', '" + s.getUri_spotify()
                        + "', " + s.getTempo() + ", " + s.getDuration_ms() + ", "
                        + s.getPopularity() + ")";
                sqLiteDatabase.execSQL(sql);
            }
        }

    }
    private void insert_artists(SQLiteDatabase sqLiteDatabase){
        ArrayList<Artist> artists;
        JsonHelper jsonHelper = new JsonHelper();
        artists = jsonHelper.readJsonArtists(inputStreamArtists);
        if (artists != null) {
            for (Artist a : artists) {
                String name;
                if (a.getName().contains("'")){
                    name = a.getName().replace("'", "''");
                } else {
                    name = a.getName();
                }
                String sql = "INSERT INTO t_artists VALUES (" + a.getId() + ", '"
                        + name + "')";
                sqLiteDatabase.execSQL(sql);
            }
        }

    }

    private void insert_song_artists(SQLiteDatabase sqLiteDatabase){
        ArrayList<SongArtist> song_artists;
        JsonHelper jsonHelper = new JsonHelper();
        song_artists = jsonHelper.readJsonSongAndArtists(inputStreamSongArtists);
        if (song_artists != null) {
            for (SongArtist s_a : song_artists) {
                String sql = "INSERT INTO t_songs_artists VALUES (" + s_a.getId_song()
                        + ", " + s_a.getId_artist() + ")";
                sqLiteDatabase.execSQL(sql);
            }
        }

    }

    private void insert_genres(SQLiteDatabase sqLiteDatabase){
        ArrayList<Genre> genres;
        JsonHelper jsonHelper = new JsonHelper();
        genres = jsonHelper.readJsonGenres(inputStreamGenres);
        if (genres != null) {
            for (Genre g : genres) {
                String name;
                if (g.getName().contains("'")){
                    name = g.getName().replace("'", "''");
                } else {
                    name = g.getName();
                }
                String sql = "INSERT INTO t_genres VALUES (" + g.getId() + ", '"
                        + name + "')";
                sqLiteDatabase.execSQL(sql);
            }
        }

    }

    private void insert_song_genres(SQLiteDatabase sqLiteDatabase){
        ArrayList<SongGenre> songs_genres;
        JsonHelper jsonHelper = new JsonHelper();
        songs_genres = jsonHelper.readJsonSongAndGenres(inputStreamSongGenres);
        if (songs_genres != null) {
            for (SongGenre s_g : songs_genres) {
                String sql = "INSERT INTO t_songs_genres VALUES (" + s_g.getId_song()
                        + ", " + s_g.getId_genre() + ")";
                sqLiteDatabase.execSQL(sql);
            }
        }

    }
}