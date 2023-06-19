package com.example.myapplication.typedefs;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;

    public User() {
        this.id = "";
        this.email = "";
        this.photoUrl = "";
        this.name = "";
    }

    public User (String id, String name, String email, String photoUrl){
        this.email = email;
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
