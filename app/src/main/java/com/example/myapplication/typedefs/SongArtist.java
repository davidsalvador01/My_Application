package com.example.myapplication.typedefs;

public class SongArtist {
    private int id_song;
    private int id_artist;

    public SongArtist(int id_song, int id_artist) {
        this.id_song = id_song;
        this.id_artist = id_artist;
    }

    public int getId_song() {
        return id_song;
    }

    public void setId_song(int id_song) {
        this.id_song = id_song;
    }

    public int getId_artist() {
        return id_artist;
    }

    public void setId_artist(int id_artist) {
        this.id_artist = id_artist;
    }
}
