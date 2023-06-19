package com.example.myapplication.typedefs;

public class Song {
    private int id;
    private String titulo;
    private String uri_spotify;
    private double tempo;
    private int duration_ms;
    private int popularity;

    public Song(int id, String titulo, String uri_spotify, double tempo, int duration_ms, int popularity) {
        this.id = id;
        this.titulo = titulo;
        this.uri_spotify = uri_spotify;
        this.tempo = tempo;
        this.duration_ms = duration_ms;
        this.popularity = popularity;
    }
    public Song() {
        this.id = 0;
        this.titulo = "";
        this.uri_spotify = "";
        this.tempo = 0;
        this.duration_ms = 0;
        this.popularity = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDuration_ms() {
        return duration_ms;
    }

    public void setDuration_ms(int duration_ms) {
        this.duration_ms = duration_ms;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUri_spotify() {
        return uri_spotify;
    }

    public void setUri_spotify(String uri_spotify) {
        this.uri_spotify = uri_spotify;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public void printSong(){
        System.out.printf("%d %s %s %f %d %d \n", this.id,
                this.titulo, this.uri_spotify, this.tempo, this.duration_ms, this.popularity);
    }
}
