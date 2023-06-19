package com.example.myapplication;

import android.app.Activity;

import com.example.myapplication.typedefs.Artist;
import com.example.myapplication.typedefs.Genre;
import com.example.myapplication.typedefs.Song;
import com.example.myapplication.typedefs.SongArtist;
import com.example.myapplication.typedefs.SongGenre;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JsonHelper extends Activity {

    public ArrayList<Song> readJsonSongs(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            ArrayList<Song> canciones = new ArrayList<>();

            // Recorrer los objetos del JSONArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Obtener los valores de las claves del objeto JSON
                String uri_spotify = jsonObject.getString("Spotify ID");
                String titulo = jsonObject.getString("Track Name");
                double tempo = jsonObject.getDouble("Tempo");
                int duration = jsonObject.getInt("Duration_ms");
                int popularity = jsonObject.getInt("Popularity");
                int id = jsonObject.getInt("Id");

                Song s = new Song(id, titulo, uri_spotify, tempo, duration, popularity);
                canciones.add(s);

            }

            return canciones;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Artist> readJsonArtists(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            ArrayList<Artist> artists = new ArrayList<>();

            // Recorrer los objetos del JSONArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Obtener los valores de las claves del objeto JSON
                String name = jsonObject.getString("Name");
                int id = jsonObject.getInt("Id");

                Artist a = new Artist(id, name);
                artists.add(a);
            }

            return artists;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<SongArtist> readJsonSongAndArtists(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            ArrayList<SongArtist> song_artists = new ArrayList<>();

            // Recorrer los objetos del JSONArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Obtener los valores de las claves del objeto JSON
                int id_song = jsonObject.getInt("Id_song");
                int id_artist = jsonObject.getInt("Id_artist");

                SongArtist s_a = new SongArtist(id_song, id_artist);
                song_artists.add(s_a);
            }

            return song_artists;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Genre> readJsonGenres(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            ArrayList<Genre> genres = new ArrayList<>();

            // Recorrer los objetos del JSONArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Obtener los valores de las claves del objeto JSON
                String name = jsonObject.getString("Name");
                int id = jsonObject.getInt("Id");

                Genre g = new Genre(id, name);
                genres.add(g);
            }

            return genres;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<SongGenre> readJsonSongAndGenres(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);

            ArrayList<SongGenre> song_genres = new ArrayList<>();

            // Recorrer los objetos del JSONArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Obtener los valores de las claves del objeto JSON
                int id_song = jsonObject.getInt("Id_song");
                int id_genre = jsonObject.getInt("Id_genre");

                SongGenre s_g = new SongGenre(id_song, id_genre);
                song_genres.add(s_g);
            }

            return song_genres;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
