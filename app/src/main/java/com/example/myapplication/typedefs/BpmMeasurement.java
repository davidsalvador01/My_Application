package com.example.myapplication.typedefs;

public class BpmMeasurement {
    private int id;
    private int id_session;
    private String time;
    private float heart_rate;

    public BpmMeasurement(int id, int id_session, String time, float heart_rate) {
        this.id = id;
        this.id_session = id_session;
        this.time = time;
        this.heart_rate = heart_rate;
    }
    public BpmMeasurement() {
        this.id = 0;
        this.id_session = 0;
        this.time = "";
        this.heart_rate = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_session() {
        return id_session;
    }

    public void setId_session(int id_session) {
        this.id_session = id_session;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getHeart_rate() {
        return heart_rate;
    }

    public void setHeart_rate(float heart_rate) {
        this.heart_rate = heart_rate;
    }
}
