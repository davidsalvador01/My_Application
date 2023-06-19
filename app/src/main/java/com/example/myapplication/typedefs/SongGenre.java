package com.example.myapplication.typedefs;

public class SongGenre {
    private int id_song;
    private int id_genre;

    public SongGenre(int id_song, int id_genre){
        this.id_genre = id_genre;
        this.id_song = id_song;
    }

    public int getId_song() {
        return id_song;
    }

    public void setId_song(int id_song) {
        this.id_song = id_song;
    }

    public int getId_genre() {
        return id_genre;
    }

    public void setId_genre(int id_genre) {
        this.id_genre = id_genre;
    }
}
